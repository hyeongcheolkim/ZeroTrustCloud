package zerotrust.cloud.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerotrust.cloud.domain.auth.service.AuthService;
import zerotrust.cloud.domain.user_server.entity.User;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    
    @PostMapping("/login-sync")
    public ResponseEntity<Map<String, Object>> loginSync(@AuthenticationPrincipal Jwt jwt) {
        log.info("Login sync requested for token subject: {}", jwt.getSubject());

        User syncedUser = authService.syncUserWithJwt(jwt);

        return ResponseEntity.ok(Map.of(
                "message", "User synced successfully",
                "userId", syncedUser.getId(),
                "username", syncedUser.getUsername()));
    }
}
