package zerotrust.cloud.domain.user_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerotrust.cloud.domain.k8s.service.KubernetesService;
import zerotrust.cloud.domain.user_server.entity.ServerInstance;
import zerotrust.cloud.domain.user_server.repository.ServerInstanceRepository;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerProvisioningService {

    private final KubernetesService kubernetesService;
    private final ServerInstanceRepository serverInstanceRepository;

    @Transactional
    public ServerInstance provisionServer(String name, String description) {

        String podName = "terminal-pod-" + UUID.randomUUID().toString().substring(0, 8);


        ServerInstance serverInstance = ServerInstance.builder()
                .name(name)
                .podName(podName)
                .namespace("default")
                .status(ServerInstance.ServerStatus.PENDING)
                .description(description)
                .build();

        serverInstance = serverInstanceRepository.save(serverInstance);


        try {
            kubernetesService.createTerminalPod(podName,
                    Map.of("app", "ztna-terminal", "instance-id", serverInstance.getId().toString()));
            log.info("Successfully requested K8s Pod creation: {}", podName);
        } catch (Exception e) {
            log.error("Failed to create K8s Pod: {}", podName, e);
            serverInstance.updateStatus(ServerInstance.ServerStatus.FAILED);
            throw new RuntimeException("K8s Pod creation failed", e);
        }

        return serverInstance;
    }

    @Transactional
    public void deleteServer(Long serverId) {
        ServerInstance instance = serverInstanceRepository.findById(serverId)
                .orElseThrow(() -> new IllegalArgumentException("Server not found"));


        try {
            kubernetesService.deletePod(instance.getPodName());
        } catch (Exception e) {
            log.error("Failed to delete K8s Pod: {}", instance.getPodName(), e);

        }


        instance.updateStatus(ServerInstance.ServerStatus.TERMINATED);
    }
}
