package com.example.vertexspace_server.dto;

public class WaitlistJoinDTO {
    private Long resourceId;
    private String startUtc;
    private String endUtc;


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
