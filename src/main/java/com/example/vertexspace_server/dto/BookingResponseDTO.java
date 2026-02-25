package com.example.vertexspace_server.dto;

import java.time.Instant;

public class BookingResponseDTO {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private Instant startUtc;
    private Instant endUtc;
    private String status;
    private String recurrenceGroupId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getRecurrenceGroupId() {
        return recurrenceGroupId;
    }

    public void setRecurrenceGroupId(String recurrenceGroupId) {
        this.recurrenceGroupId = recurrenceGroupId;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Instant getStartUtc() {
        return startUtc;
    }

    public void setStartUtc(Instant startUtc) {
        this.startUtc = startUtc;
    }

    public Instant getEndUtc() {
        return endUtc;
    }

    public void setEndUtc(Instant endUtc) {
        this.endUtc = endUtc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
