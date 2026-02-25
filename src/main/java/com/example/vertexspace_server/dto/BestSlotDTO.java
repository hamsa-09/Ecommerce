package com.example.vertexspace_server.dto;

import java.time.Instant;

public class BestSlotDTO {
    private Instant startUtc;
    private Instant endUtc;

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
}
