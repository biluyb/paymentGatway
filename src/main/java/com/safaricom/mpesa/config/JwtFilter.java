package com.safaricom.mpesa.config;

import com.safaricom.mpesa.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JwtFilter
 *
 * <p>Spring Security servlet filter executing once per incoming HTTP request.
 * Inspects the {@code Authorization} header for a valid {@code Bearer <token>},
 * cryptographically validates its HMAC-SHA signature, extracts the subject username,
 * and populates the {@link SecurityContextHolder} with an authenticated principal.</p>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Injects the cryptographic token verification service.
     *
     * @param jwtService JWT processing service
     */
    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Intercepts HTTP requests, validates JWT claims, and registers authenticated security context.
     *
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @param filterChain downstream servlet filter chain
     * @throws ServletException if an execution filter exception occurs
     * @throws IOException if an I/O transport error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Skip token extraction if Bearer header is omitted (public routes handled by SecurityFilterChain)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            username = jwtService.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.validateToken(jwt, username)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, Collections.emptyList()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Unverified or expired tokens fall through to SecurityFilterChain where 403/401 is asserted
        }

        filterChain.doFilter(request, response);
    }
}
