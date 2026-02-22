package com.example.vertexspace_server.dto;

public class RecommendationDTO {
    private Long resourceId;
    private String resourceName;
    private int bookingCount;

    public RecommendationDTO(Long resourceId, String resourceName, int bookingCount) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.bookingCount = bookingCount;
    }

    public RecommendationDTO() {

    }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public int getBookingCount() { return bookingCount; }
    public void setBookingCount(int bookingCount) { this.bookingCount = bookingCount; }
}
