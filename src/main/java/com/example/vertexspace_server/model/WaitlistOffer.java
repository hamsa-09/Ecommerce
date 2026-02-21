package com.example.vertexspace_server.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class WaitlistOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "waitlist_entry_id")
    private WaitlistEntry waitlistEntry;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant offeredAtUtc;

    @Column(nullable = false)
    private Instant expiresAtUtc;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public WaitlistEntry getWaitlistEntry() { return waitlistEntry; }
    public void setWaitlistEntry(WaitlistEntry waitlistEntry) { this.waitlistEntry = waitlistEntry; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getOfferedAtUtc() { return offeredAtUtc; }
    public void setOfferedAtUtc(Instant offeredAtUtc) { this.offeredAtUtc = offeredAtUtc; }
    public Instant getExpiresAtUtc() { return expiresAtUtc; }
    public void setExpiresAtUtc(Instant expiresAtUtc) { this.expiresAtUtc = expiresAtUtc; }
}
