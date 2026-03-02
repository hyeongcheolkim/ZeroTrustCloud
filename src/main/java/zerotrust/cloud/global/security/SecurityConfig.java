package zerotrust.cloud.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/", "/index.html", "/favicon.ico", "/css/**", "/js/**",
                                                                "/error", "/images/**", "/webjars/**",
                                                                "/ws-terminal/**")
                                                .permitAll()
                                                .requestMatchers("/api/public/**", "/actuator/health").permitAll()

                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                keycloakJwtAuthenticationConverter)));

                return http.build();
        }
}
