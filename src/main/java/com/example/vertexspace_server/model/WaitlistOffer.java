package com.example.vertexspace_server.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        indexes = {
                @Index(name="idx_offer_slot",
                        columnList="status,expiresAtUtc")
        }
)
public class WaitlistOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "waitlist_entry_id")
    private WaitlistEntry waitlistEntry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    @Column(nullable = false)
    private Instant offeredAtUtc;

    @Column(nullable = false)
    private Instant expiresAtUtc;

    // Provisional booking link
    @OneToOne
    @JoinColumn(name = "provisional_booking_id")
    private Booking provisionalBooking;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WaitlistEntry getWaitlistEntry() {
        return waitlistEntry;
    }

    public void setWaitlistEntry(WaitlistEntry waitlistEntry) {
        this.waitlistEntry = waitlistEntry;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
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

    public Booking getProvisionalBooking() {
        return provisionalBooking;
    }

    public void setProvisionalBooking(Booking provisionalBooking) {
        this.provisionalBooking = provisionalBooking;
    }
}