package com.example.vertexspace_server.model;

import jakarta.persistence.*;

@Entity
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // ROOM, DESK, PARKING

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(nullable = false)
    private String mode; // ASSIGNED, HOT DESK

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
