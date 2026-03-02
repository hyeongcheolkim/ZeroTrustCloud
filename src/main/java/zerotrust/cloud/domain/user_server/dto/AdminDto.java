package zerotrust.cloud.domain.user_server.dto;

import lombok.Data;

public class AdminDto {

    @Data
    public static class ProvisionServerRequest {
        private String name;
        private String description;
    }

    @Data
    public static class GrantAccessRequest {
        private Long serverInstanceId;
        private String username;
        private String accessType;
    }

    @Data
    public static class ServerResponse {
        private Long id;
        private String name;
        private String podName;
        private String namespace;
        private String status;
        private String description;
    }
}
