package com.example.vertexspace_server.security;

import com.example.vertexspace_server.exception.JwtAuthenticationException;
import com.example.vertexspace_server.model.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {
    private final String jwtSecret = "vertexspaceSecretKeyvertexspaceSecretKeyvertexspaceSecretKey";
    private final long jwtExpirationMs = 3600000; // 1 hour
    private final Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    public String generateToken(String username, List<String> roles, Map<String, Object> permissions) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("Token expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException("Unsupported JWT token", e);
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException("Malformed JWT token", e);
        } catch (SignatureException e) {
            throw new JwtAuthenticationException("Invalid JWT signature", e);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException("JWT claims string is empty", e);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtAuthenticationException e) {
            return false;
        }
    }

    public static UserAccount getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserAccount();
        }
        throw new IllegalStateException("No authenticated user found");
    }
}
