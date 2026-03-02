package zerotrust.cloud.domain.user_server.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_server_accesses", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "server_instance_id" }) })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserServerAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_instance_id", nullable = false)
    private ServerInstance serverInstance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessType accessType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime grantedAt;

    @Builder
    public UserServerAccess(User user, ServerInstance serverInstance, AccessType accessType) {
        this.user = user;
        this.serverInstance = serverInstance;
        this.accessType = accessType;
    }

    public void updateAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public enum AccessType {
        READ_ONLY, FULL_ACCESS
    }
}
