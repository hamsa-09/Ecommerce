package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.BuildingDTO;
import java.util.List;

public interface BuildingService {
    BuildingDTO createBuilding(BuildingDTO buildingDTO);
    List<BuildingDTO> listBuildings();
}

