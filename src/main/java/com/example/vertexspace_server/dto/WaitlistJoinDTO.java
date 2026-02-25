package com.example.vertexspace_server.dto;

public class WaitlistJoinDTO {
    private String resourceName;
    private String startUtc;
    private String endUtc;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
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
