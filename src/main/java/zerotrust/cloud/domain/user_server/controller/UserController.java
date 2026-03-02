package zerotrust.cloud.domain.user_server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerotrust.cloud.domain.user_server.dto.UserDto;
import zerotrust.cloud.domain.user_server.entity.User;
import zerotrust.cloud.domain.user_server.entity.UserServerAccess;
import zerotrust.cloud.domain.user_server.repository.UserRepository;
import zerotrust.cloud.domain.user_server.repository.UserServerAccessRepository;
import zerotrust.cloud.domain.user_server.repository.ServerInstanceRepository;
import zerotrust.cloud.domain.k8s.service.KubernetesService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class UserController {

        private final UserRepository userRepository;
        private final UserServerAccessRepository userServerAccessRepository;
        private final ServerInstanceRepository serverInstanceRepository;
        private final KubernetesService kubernetesService;

        @GetMapping("/servers")
        public ResponseEntity<List<UserDto.AccessibleServerResponse>> getMyServers() {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String currentUsername = authentication.getName();

                log.info("Fetching accessible servers for user: {}", currentUsername);

                User currentUser = userRepository.findByUsername(currentUsername)
                                .orElseThrow(() -> new IllegalArgumentException("User not synced with DB"));

                List<UserServerAccess> accesses = userServerAccessRepository.findByUserId(currentUser.getId());

                List<UserDto.AccessibleServerResponse> responseList = accesses.stream()
                                .map(access -> {
                                        var server = access.getServerInstance();


                                        if (server.getStatus() == zerotrust.cloud.domain.user_server.entity.ServerInstance.ServerStatus.PENDING) {
                                                String realStatus = kubernetesService.getPodStatus(server.getPodName());
                                                if ("Running".equalsIgnoreCase(realStatus)) {
                                                        server.updateStatus(
                                                                        zerotrust.cloud.domain.user_server.entity.ServerInstance.ServerStatus.RUNNING);
                                                        serverInstanceRepository.save(server);
                                                } else if ("Failed".equalsIgnoreCase(realStatus)) {
                                                        server.updateStatus(
                                                                        zerotrust.cloud.domain.user_server.entity.ServerInstance.ServerStatus.FAILED);
                                                        serverInstanceRepository.save(server);
                                                }
                                        }

                                        return UserDto.AccessibleServerResponse.builder()
                                                        .serverId(server.getId())
                                                        .serverName(server.getName())
                                                        .description(server.getDescription())
                                                        .status(server.getStatus().name())
                                                        .accessType(access.getAccessType().name())
                                                        .build();
                                })
                                .toList();

                return ResponseEntity.ok(responseList);
        }
}
