package com.example.vertexspace_server.dto;

import java.util.Set;

public class ResourceRequestDTO {
    private String name;
    private String type;
    private Long departmentId;
    private Long floorId;
    private Integer capacity;
    private Set<String> features;
    private String deskMode; // Only for DESK

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getFloorId() { return floorId; }
    public void setFloorId(Long floorId) { this.floorId = floorId; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Set<String> getFeatures() { return features; }
    public void setFeatures(Set<String> features) { this.features = features; }
    public String getDeskMode() { return deskMode; }
    public void setDeskMode(String deskMode) { this.deskMode = deskMode; }
}
