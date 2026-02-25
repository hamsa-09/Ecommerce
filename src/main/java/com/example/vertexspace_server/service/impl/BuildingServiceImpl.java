package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.dto.BuildingDTO;
import com.example.vertexspace_server.model.Building;
import com.example.vertexspace_server.repository.BuildingRepository;
import com.example.vertexspace_server.service.BuildingService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BuildingServiceImpl implements BuildingService {
    private final BuildingRepository buildingRepository;

    public BuildingServiceImpl(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @Override
    public BuildingDTO createBuilding(BuildingDTO buildingDTO) {
        Building building = new Building();
        building.setName(buildingDTO.getName());
        Building saved = buildingRepository.save(building);
        BuildingDTO response = new BuildingDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        return response;
    }

    @Override
    public List<BuildingDTO> listBuildings() {
        return buildingRepository.findAll().stream().map(b -> {
            BuildingDTO dto = new BuildingDTO();
            dto.setId(b.getId());
            dto.setName(b.getName());
            return dto;
        }).toList();
    }
}

