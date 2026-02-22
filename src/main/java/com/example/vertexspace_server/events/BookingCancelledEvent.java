package com.example.vertexspace_server.events;

import java.time.Instant;

public class BookingCancelledEvent {

    private final Long resourceId;
    private final Instant startUtc;
    private final Instant endUtc;

    public BookingCancelledEvent(Long resourceId, Instant startUtc, Instant endUtc) {
        this.resourceId = resourceId;
        this.startUtc = startUtc;
        this.endUtc = endUtc;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public Instant getStartUtc() {
        return startUtc;
    }

    public Instant getEndUtc() {
        return endUtc;
    }
}