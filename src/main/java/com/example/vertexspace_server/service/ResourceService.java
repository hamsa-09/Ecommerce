package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.ResourceRequestDTO;
import com.example.vertexspace_server.dto.ResourceResponseDTO;
import java.util.List;

public interface ResourceService {
    String createResource(ResourceRequestDTO resourceRequestDTO);
    String updateResource(Long id, ResourceRequestDTO resourceRequestDTO);
    void deleteResource(Long id);
    ResourceResponseDTO getResourceById(Long id);
    List<ResourceResponseDTO> searchResources(String type, Long floorId, Integer capacity, String departmentId);
}
