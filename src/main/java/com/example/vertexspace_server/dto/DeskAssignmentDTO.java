package com.example.vertexspace_server.dto;

public class DeskAssignmentDTO {
    private Long userId;
    private Long resourceId;
    private String startUtc;
    private String endUtc;

    public DeskAssignmentDTO() {
    }

    public DeskAssignmentDTO(Long userId, Long resourceId, String startUtc, String endUtc) {
        this.userId = userId;
        this.resourceId = resourceId;
        this.startUtc = startUtc;
        this.endUtc = endUtc;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getStartUtc() {
        return startUtc;
    }

    public void setStartUtc(String startUtc) {
        this.startUtc = startUtc;
    }

    public String getEndUtc() {
        return endUtc;
    }

    public void setEndUtc(String endUtc) {
        this.endUtc = endUtc;
    }
}
