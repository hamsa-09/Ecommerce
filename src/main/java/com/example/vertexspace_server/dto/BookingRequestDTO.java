package com.example.vertexspace_server.dto;

import java.time.Instant;

public class BookingRequestDTO {
    private Long resourceId;
    private Instant startUtc;
    private Instant endUtc;

    // Getters and setters
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public Instant getStartUtc() { return startUtc; }
    public void setStartUtc(Instant startUtc) { this.startUtc = startUtc; }
    public Instant getEndUtc() { return endUtc; }
    public void setEndUtc(Instant endUtc) { this.endUtc = endUtc; }
}
