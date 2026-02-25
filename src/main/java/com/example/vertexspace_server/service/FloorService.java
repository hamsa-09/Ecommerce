package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.FloorDTO;
import java.util.List;

public interface FloorService {
    FloorDTO createFloor(FloorDTO floorDTO);
    List<FloorDTO> listFloors();
}

