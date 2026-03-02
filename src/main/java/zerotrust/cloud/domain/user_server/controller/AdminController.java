package zerotrust.cloud.domain.user_server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import zerotrust.cloud.domain.user_server.dto.AdminDto;
import zerotrust.cloud.domain.user_server.entity.ServerInstance;
import zerotrust.cloud.domain.user_server.entity.User;
import zerotrust.cloud.domain.user_server.entity.UserServerAccess;
import zerotrust.cloud.domain.user_server.repository.ServerInstanceRepository;
import zerotrust.cloud.domain.user_server.repository.UserRepository;
import zerotrust.cloud.domain.user_server.repository.UserServerAccessRepository;
import zerotrust.cloud.domain.user_server.service.ServerProvisioningService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

        private final ServerProvisioningService serverProvisioningService;
        private final ServerInstanceRepository serverInstanceRepository;
        private final UserRepository userRepository;
        private final UserServerAccessRepository userServerAccessRepository;

        @PostMapping("/servers")
        public ResponseEntity<AdminDto.ServerResponse> provisionServer(
                        @RequestBody AdminDto.ProvisionServerRequest request) {
                log.info("Admin requesting new server provisioning: {}", request.getName());
                ServerInstance instance = serverProvisioningService.provisionServer(request.getName(),
                                request.getDescription());


                JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext()
                                .getAuthentication();
                String currentUsername = authentication.getToken().getClaimAsString("preferred_username");

                userRepository.findByUsername(currentUsername).ifPresent(user -> {
                        UserServerAccess access = UserServerAccess.builder()
                                        .user(user)
                                        .serverInstance(instance)
                                        .accessType(UserServerAccess.AccessType.FULL_ACCESS)
                                        .build();
                        userServerAccessRepository.save(access);
                        log.info("Automatically granted FULL_ACCESS to creator: {}", currentUsername);
                });

                return ResponseEntity.ok(mapToServerResponse(instance));
        }

        @DeleteMapping("/servers/{serverId}")
        public ResponseEntity<Void> deleteServer(@PathVariable Long serverId) {
                log.info("Admin requesting server deletion for ID: {}", serverId);
                serverProvisioningService.deleteServer(serverId);
                return ResponseEntity.noContent().build();
        }

        @GetMapping("/users")
        public ResponseEntity<List<String>> listAllUsers() {
                List<String> usernames = userRepository.findAll().stream()
                                .map(User::getUsername)
                                .toList();
                return ResponseEntity.ok(usernames);
        }

        @GetMapping("/servers")
        public ResponseEntity<List<AdminDto.ServerResponse>> listAllServers() {
                List<AdminDto.ServerResponse> servers = serverInstanceRepository.findAll().stream()
                                .map(this::mapToServerResponse)
                                .toList();
                return ResponseEntity.ok(servers);
        }

        @PostMapping("/access/grant")
        public ResponseEntity<Void> grantAccess(@RequestBody AdminDto.GrantAccessRequest request) {
                log.info("Admin granting access {} to user {} for server id {}",
                                request.getAccessType(), request.getUsername(), request.getServerInstanceId());

                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                ServerInstance server = serverInstanceRepository.findById(request.getServerInstanceId())
                                .orElseThrow(() -> new IllegalArgumentException("Server not found"));

                UserServerAccess access = UserServerAccess.builder()
                                .user(user)
                                .serverInstance(server)
                                .accessType(UserServerAccess.AccessType.valueOf(request.getAccessType()))
                                .build();

                userServerAccessRepository.save(access);
                return ResponseEntity.ok().build();
        }

        private AdminDto.ServerResponse mapToServerResponse(ServerInstance instance) {
                AdminDto.ServerResponse response = new AdminDto.ServerResponse();
                response.setId(instance.getId());
                response.setName(instance.getName());
                response.setPodName(instance.getPodName());
                response.setNamespace(instance.getNamespace());
                response.setStatus(instance.getStatus().name());
                response.setDescription(instance.getDescription());
                return response;
        }
}
