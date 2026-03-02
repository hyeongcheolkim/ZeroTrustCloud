package zerotrust.cloud.domain.user_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import zerotrust.cloud.domain.user_server.entity.ServerInstance;

public interface ServerInstanceRepository extends JpaRepository<ServerInstance, Long> {
    Optional<ServerInstance> findByName(String name);

    Optional<ServerInstance> findByPodNameAndNamespace(String podName, String namespace);
}
