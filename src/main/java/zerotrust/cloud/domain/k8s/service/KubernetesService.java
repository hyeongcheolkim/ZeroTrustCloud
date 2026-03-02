package zerotrust.cloud.domain.k8s.service;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KubernetesService {

    private final KubernetesClient kubernetesClient;

    @Value("${kubernetes.config.namespace:default}")
    private String namespace;

    @PostConstruct
    public void cleanupTestPodsOnStartup() {
        log.info("Application starting up... Cleaning up all existing terminal pods in namespace: {}", namespace);
        try {
            kubernetesClient.pods()
                    .inNamespace(namespace)
                    .withLabel("app", "ztna-terminal")
                    .delete();
            log.info("Cleanup complete.");
        } catch (Exception e) {
            log.error("Failed to clean up old pods on startup", e);
        }
    }

    
    public Pod createTerminalPod(String podName, Map<String, String> labels) {
        log.info("Creating Pod: {} in namespace: {}", podName, namespace);

        Pod pod = new PodBuilder()
                .withNewMetadata()
                .withName(podName)
                .withLabels(labels)
                .endMetadata()
                .withNewSpec()
                .withContainers(new ContainerBuilder()
                        .withName("terminal-container")
                        .withImage("ubuntu:22.04")

                        .withCommand("tail", "-f", "/dev/null")
                        .withTty(true)
                        .withStdin(true)
                        .build())
                .withRestartPolicy("Never")
                .endSpec()
                .build();

        return kubernetesClient.pods().inNamespace(namespace).resource(pod).create();
    }

    
    public boolean deletePod(String podName) {
        log.info("Deleting Pod: {} in namespace: {}", podName, namespace);
        return !kubernetesClient.pods()
                .inNamespace(namespace)
                .withName(podName)
                .delete()
                .isEmpty();
    }

    
    public String getPodStatus(String podName) {
        Pod pod = kubernetesClient.pods().inNamespace(namespace).withName(podName).get();
        if (pod == null || pod.getStatus() == null) {
            return "NOT_FOUND";
        }
        return pod.getStatus().getPhase();
    }
}
