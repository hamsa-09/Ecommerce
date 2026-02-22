package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.RecommendationDTO;
import java.util.List;

public interface DashboardService {
    List<RecommendationDTO> getTopResources();
}
