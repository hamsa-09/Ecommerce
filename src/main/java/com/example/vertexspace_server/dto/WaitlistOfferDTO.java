package com.example.vertexspace_server.dto;

import java.time.Instant;

public class WaitlistOfferDTO {
    private Long id;
    private Long waitlistEntryId;
    private String status;
    private Instant offeredAtUtc;
    private Instant expiresAtUtc;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWaitlistEntryId() {
        return waitlistEntryId;
    }

    public void setWaitlistEntryId(Long waitlistEntryId) {
        this.waitlistEntryId = waitlistEntryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getOfferedAtUtc() {
        return offeredAtUtc;
    }

    public void setOfferedAtUtc(Instant offeredAtUtc) {
        this.offeredAtUtc = offeredAtUtc;
    }

    public Instant getExpiresAtUtc() {
        return expiresAtUtc;
    }

    public void setExpiresAtUtc(Instant expiresAtUtc) {
        this.expiresAtUtc = expiresAtUtc;
    }
}
