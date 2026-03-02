package zerotrust.cloud.domain.user_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import zerotrust.cloud.domain.user_server.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByUsername(String username);
}
