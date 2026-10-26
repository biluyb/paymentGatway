package com.safaricom.mpesa.controller;

import com.safaricom.mpesa.dto.AuthRequest;
import com.safaricom.mpesa.dto.AuthResponse;
import com.safaricom.mpesa.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController
 *
 * <p>Authentication endpoint issuing signed JSON Web Tokens (JWT) to accredited
 * fintech partners, payment service providers (PSPs), and core banking clients.</p>
 *
 * <ul>
 *     <li>POST /api/v1/auth/login - Issue signed Bearer token upon credentials verification</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Authenticates client credentials and produces a cryptographically signed HMAC-SHA256 JWT token.
     *
     * @param request credentials payload containing username and password
     * @return 200 OK with AuthResponse containing token and TTL if valid; 401 UNAUTHORIZED otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        // In real enterprise M-PESA integration, validate credentials against partner DB, LDAP, or OAuth2 IdP
        if ("safari_partner".equals(request.getUsername()) && "partner@123".equals(request.getPassword())) {
            String token = jwtService.generateToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, jwtService.getExpirationMs()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
