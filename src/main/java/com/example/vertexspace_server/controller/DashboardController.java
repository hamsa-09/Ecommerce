package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.RecommendationDTO;
import com.example.vertexspace_server.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/recommendations")
    public List<RecommendationDTO> getTopResources() {
        return dashboardService.getTopResources();
    }
}
