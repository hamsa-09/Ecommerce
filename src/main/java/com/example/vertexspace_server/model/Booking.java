package com.example.vertexspace_server.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_booking_resource_time",
                        columnList = "resource_id,startUtc,endUtc")
        }
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(nullable = false)
    private Instant startUtc;

    @Column(nullable = false)
    private Instant endUtc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column
    private UUID recurrenceGroupId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public UUID getRecurrenceGroupId() {
        return recurrenceGroupId;
    }

    public void setRecurrenceGroupId(UUID recurrenceGroupId) {
        this.recurrenceGroupId = recurrenceGroupId;
    }
}