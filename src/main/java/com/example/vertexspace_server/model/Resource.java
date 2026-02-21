package com.example.vertexspace_server.model;

import jakarta.persistence.*;
import java.util.Map;
import java.util.Set;

@Entity
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // ROOM, DESK, PARKING

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @Column(nullable = false)
    private Integer capacity;

    @ElementCollection
    @CollectionTable(
            name = "resource_features",
            joinColumns = @JoinColumn(name = "resource_id")
    )
    @Column(name = "feature")
    private Set<String> features;

    @Enumerated(EnumType.STRING)
    private DeskMode deskMode; // Only for DESK

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Floor getFloor() { return floor; }
    public void setFloor(Floor floor) { this.floor = floor; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Set<String> getFeatures() { return features; }
    public void setFeatures(Set<String> features) { this.features = features; }
    public DeskMode getDeskMode() { return deskMode; }
    public void setDeskMode(DeskMode deskMode) { this.deskMode = deskMode; }
}
