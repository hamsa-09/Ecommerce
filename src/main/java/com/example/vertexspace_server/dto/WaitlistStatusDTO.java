package com.example.vertexspace_server.dto;

import java.time.Instant;

public class WaitlistStatusDTO extends  WaitlistEntryDTO {
    private int queuePosition;
    private String offerStatus;
    private Instant OfferExpiresAt;

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public String getOfferStatus() {
        return offerStatus;
    }

    public void setOfferStatus(String offerStatus) {
        this.offerStatus = offerStatus;
    }

    public Instant getOfferExpiresAt() {
        return OfferExpiresAt;
    }

    public void setOfferExpiresAt(Instant offerExpiresAt) {
        OfferExpiresAt = offerExpiresAt;
    }
}
