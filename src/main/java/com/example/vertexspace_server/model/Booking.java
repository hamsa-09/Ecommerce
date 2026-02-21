package com.example.vertexspace_server.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resource_id", referencedColumnName = "id")
    private Resource resource;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserAccount user;

    @Column(nullable = false)
    private Instant startUtc;

    @Column(nullable = false)
    private Instant endUtc;

    @Column(nullable = false)
    private String status;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public Instant getStartUtc() { return startUtc; }
    public void setStartUtc(Instant startUtc) { this.startUtc = startUtc; }
    public Instant getEndUtc() { return endUtc; }
    public void setEndUtc(Instant endUtc) { this.endUtc = endUtc; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
