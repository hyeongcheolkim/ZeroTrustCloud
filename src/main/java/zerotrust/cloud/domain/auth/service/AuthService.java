package zerotrust.cloud.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerotrust.cloud.domain.user_server.entity.User;
import zerotrust.cloud.domain.user_server.repository.UserRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    @Transactional
    public User syncUserWithJwt(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");

        if (username == null) {
            username = keycloakId;
        }

        Optional<User> existingUser = userRepository.findByKeycloakId(keycloakId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            user.updateInfo(username, email);
            return user;
        } else {

            log.info("New user detected from JWT: sync to local DB. username: {}", username);
            User newUser = User.builder()
                    .keycloakId(keycloakId)
                    .username(username)
                    .email(email)
                    .build();
            return userRepository.save(newUser);
        }
    }
}
