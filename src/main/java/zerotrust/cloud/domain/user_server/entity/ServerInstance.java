package zerotrust.cloud.domain.user_server.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "server_instances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String podName;

    @Column(nullable = false, length = 100)
    private String namespace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServerStatus status;

    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ServerInstance(String name, String podName, String namespace, ServerStatus status, String description) {
        this.name = name;
        this.podName = podName;
        this.namespace = namespace;
        this.status = status;
        this.description = description;
    }

    public void updateStatus(ServerStatus status) {
        this.status = status;
    }

    public enum ServerStatus {
        PENDING, RUNNING, STOPPED, TERMINATED, FAILED
    }
}
