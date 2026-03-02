package zerotrust.cloud.domain.terminal.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import zerotrust.cloud.domain.user_server.entity.ServerInstance;
import zerotrust.cloud.domain.user_server.repository.ServerInstanceRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.connection.MessageListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerminalExecService {

    private final KubernetesClient kubernetesClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final ServerInstanceRepository serverInstanceRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer redisListenerContainer;

    @Value("${kubernetes.config.namespace:default}")
    private String namespace;



    private final Map<Long, ExecWatch> activeSessions = new ConcurrentHashMap<>();


    private final Map<Long, MessageListener> activeListeners = new ConcurrentHashMap<>();

    private static final String REDIS_INPUT_TOPIC_PREFIX = "terminal.input.";

    
    public void startTerminalSession(Long serverId) {
        if (activeSessions.containsKey(serverId)) {
            return;
        }

        ServerInstance instance = serverInstanceRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found"));

        if (instance.getStatus() != ServerInstance.ServerStatus.RUNNING) {
            log.warn("Cannot start terminal: Server {} is not in RUNNING state", serverId);
            return;
        }

        String podName = instance.getPodName();

        ExecWatch watch = kubernetesClient.pods().inNamespace(namespace).withName(podName)
                .redirectingInput()
                .redirectingOutput()
                .redirectingError()
                .withTTY()
                .usingListener(new ExecListener() {
                    @Override
                    public void onOpen() {
                        log.info("K8s Exec Terminal opened for pod: {}", podName);
                    }

                    @Override
                    public void onFailure(Throwable t, Response failureResponse) {
                        log.error("K8s Exec Terminal failed for pod: {}", podName, t);
                        closeSession(serverId);
                    }

                    @Override
                    public void onClose(int code, String reason) {
                        log.info("K8s Exec Terminal closed for pod: {} with code: {}", podName, code);
                        closeSession(serverId);
                    }
                })
                .exec("/bin/bash");

        activeSessions.put(serverId, watch);



        Thread.ofVirtual().name("vt-k8s-exec-reader-" + serverId).start(() -> {
            try {
                InputStream out = watch.getOutput();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = out.read(buffer)) != -1) {
                    String text = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);


                    messagingTemplate.convertAndSend("/topic/terminal/" + serverId, Map.of("output", text));
                }
            } catch (IOException e) {
                log.error("Error reading from K8s Exec output stream", e);
            } finally {
                closeSession(serverId);
            }
        });


        String topicName = REDIS_INPUT_TOPIC_PREFIX + serverId;
        MessageListener listener = (message, pattern) -> {
            try {
                String input = new String(message.getBody(), StandardCharsets.UTF_8);

                if (input.startsWith("\"") && input.endsWith("\"")) {
                    input = input.substring(1, input.length() - 1);
                }

                OutputStream in = watch.getInput();
                in.write(input.getBytes(StandardCharsets.UTF_8));
                in.flush();
            } catch (IOException e) {
                log.error("Error writing to K8s Exec input stream from Redis for serverId: {}", serverId, e);
            }
        };

        redisListenerContainer.addMessageListener(listener, new ChannelTopic(topicName));
        activeListeners.put(serverId, listener);
    }

    
    public void sendInput(Long serverId, String input) {


        String topicName = REDIS_INPUT_TOPIC_PREFIX + serverId;
        redisTemplate.convertAndSend(topicName, input);
    }

    
    public void closeSession(Long serverId) {

        MessageListener listener = activeListeners.remove(serverId);
        if (listener != null) {
            redisListenerContainer.removeMessageListener(listener,
                    new ChannelTopic(REDIS_INPUT_TOPIC_PREFIX + serverId));
        }


        ExecWatch watch = activeSessions.remove(serverId);
        if (watch != null) {
            watch.close();
            log.info("Closed terminal session for serverId: {}", serverId);
        }
    }
}
