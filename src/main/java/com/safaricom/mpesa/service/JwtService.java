package com.safaricom.mpesa.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtService
 *
 * <p>Cryptographic token management service utilizing JJWT (Java JWT 0.12+) library.
 * Generates, parses, and validates tamper-proof HMAC-SHA tokens for API authentication.</p>
 * <p>Special methods:</p>
 * <ul>
 *     <li>{@link #getSigningKey()}: Decodes Base64 secret key into standard cryptographic SecretKey.</li>
 *     <li>{@link #generateToken(String)}: Emits signed token with expiration and subject claims.</li>
 *     <li>{@link #validateToken(String, String)}: Confirms token signature and subject match without expiration.</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @since 1.0.0
 */
@Service
public class JwtService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${jwt.expirationMs:86400000}")
    private long expirationMs;

    /**
     * Resolves the cryptographic HMAC secret key from configured Base64 string.
     *
     * @return SecretKey instance for HMAC-SHA signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Issues a new signed JWT Bearer token for the given subject principal.
     *
     * @param username user or client identifier
     * @return compact URL-safe JWT string
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts subject username from signed token claims.
     *
     * @param token compact JWT string
     * @return subject username string
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Validates that the token signature matches and has not expired.
     *
     * @param token compact JWT string
     * @param username expected username
     * @return true if valid and unexpired; false otherwise
     */
    public boolean validateToken(String token, String username) {
        final String extractedUser = extractUsername(token);
        return (extractedUser.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
