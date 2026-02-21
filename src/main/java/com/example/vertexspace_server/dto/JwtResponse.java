package com.example.vertexspace_server.dto;

import java.util.List;

public class JwtResponse {
    private String token;
    private String username;
    private List<String> roles;
    private Long expiresAt;

    public JwtResponse(String token, String username, List<String> roles, Long expiresAt) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expiresAt = expiresAt;
    }

    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
}
