package com.example.vertexspace_server.dto;

import java.time.Instant;

public class BookingResponseDTO {
    private Long id;
    private Long resourceId;
    private Long userId;
    private Instant startUtc;
    private Instant endUtc;
    private String status;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Instant getStartUtc() { return startUtc; }
    public void setStartUtc(Instant startUtc) { this.startUtc = startUtc; }
    public Instant getEndUtc() { return endUtc; }
    public void setEndUtc(Instant endUtc) { this.endUtc = endUtc; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
