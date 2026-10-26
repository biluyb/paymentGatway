package com.safaricom.mpesa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 *
 * <p>Spring Security 6+ enterprise configuration establishing a stateless, zero-trust perimeter.</p>
 * <p>Key architectural safeguards:</p>
 * <ul>
 *     <li>CSRF disabled: Appropriate for stateless REST APIs using cryptographic bearer tokens</li>
 *     <li>Stateless Session Management: No HTTP sessions created or persisted in server memory</li>
 *     <li>Fine-grained Authorization Rules: Public whitelisting for {@code /api/v1/auth/**}, strict JWT authentication for all other endpoints</li>
 *     <li>Custom Filter Ordering: Inserts {@link JwtFilter} immediately before standard UsernamePasswordAuthenticationFilter</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Defines the HTTP security filter chain and authorization policies.
     *
     * @param http HttpSecurity security builder
     * @return constructed SecurityFilterChain bean
     * @throws Exception if an error occurs during configuration assembly
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**", "/h2-console/**", "/error", "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
