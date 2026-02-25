package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.dto.FloorDTO;
import com.example.vertexspace_server.model.Building;
import com.example.vertexspace_server.model.Floor;
import com.example.vertexspace_server.repository.BuildingRepository;
import com.example.vertexspace_server.repository.FloorRepository;
import com.example.vertexspace_server.service.FloorService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FloorServiceImpl implements FloorService {
    private final FloorRepository floorRepository;
    private final BuildingRepository buildingRepository;

    public FloorServiceImpl(FloorRepository floorRepository, BuildingRepository buildingRepository) {
        this.floorRepository = floorRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    public FloorDTO createFloor(FloorDTO floorDTO) {
        Floor floor = new Floor();
        floor.setName(floorDTO.getName());
        if (floorDTO.getBuildingName() == null || floorDTO.getBuildingName().isBlank()) {
            throw new IllegalArgumentException("buildingName is required");
        }
        Building building = buildingRepository.findByNameIgnoreCase(floorDTO.getBuildingName());
        if (building == null) {
            throw new IllegalArgumentException("Invalid buildingName");
        }
        floor.setBuilding(building);
        Floor saved = floorRepository.save(floor);
        FloorDTO response = new FloorDTO();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setBuildingId(building.getId());
        response.setBuildingName(building.getName());
        return response;
    }

    @Override
    public List<FloorDTO> listFloors() {
        return floorRepository.findAll().stream().map(f -> {
            FloorDTO dto = new FloorDTO();
            dto.setId(f.getId());
            dto.setName(f.getName());
            dto.setBuildingId(f.getBuilding() != null ? f.getBuilding().getId() : null);
            dto.setBuildingName(f.getBuilding() != null ? f.getBuilding().getName() : null);
            return dto;
        }).toList();
    }
}
