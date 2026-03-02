package zerotrust.cloud.domain.user_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import zerotrust.cloud.domain.user_server.entity.UserServerAccess;

public interface UserServerAccessRepository extends JpaRepository<UserServerAccess, Long> {
    List<UserServerAccess> findByUserId(Long userId);

    List<UserServerAccess> findByServerInstanceId(Long serverInstanceId);

    Optional<UserServerAccess> findByUserIdAndServerInstanceId(Long userId, Long serverInstanceId);
}
