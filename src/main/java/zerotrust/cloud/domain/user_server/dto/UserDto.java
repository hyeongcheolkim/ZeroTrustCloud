package zerotrust.cloud.domain.user_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessibleServerResponse {
        private Long serverId;
        private String serverName;
        private String description;
        private String status;
        private String accessType;
    }
}
