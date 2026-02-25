package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.dto.ResourceRequestDTO;
import com.example.vertexspace_server.dto.ResourceResponseDTO;
import com.example.vertexspace_server.exception.ResourceNotFoundException;
import com.example.vertexspace_server.model.Resource;
import com.example.vertexspace_server.model.UserAccount;
import com.example.vertexspace_server.model.DeskMode;
import com.example.vertexspace_server.repository.ResourceRepository;
import com.example.vertexspace_server.service.ResourceService;
import com.example.vertexspace_server.security.JwtUtil;
import com.example.vertexspace_server.model.Department;
import com.example.vertexspace_server.model.Floor;
import com.example.vertexspace_server.repository.DepartmentRepository;
import com.example.vertexspace_server.repository.FloorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
public class ResourceServiceImpl implements ResourceService {
    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private FloorRepository floorRepository;

    private Resource toEntity(ResourceRequestDTO dto) {
        Resource resource = new Resource();
        resource.setName(dto.getName());
        resource.setType(dto.getType());
        resource.setCapacity(dto.getCapacity());
        resource.setFeatures(dto.getFeatures());
        if ("DESK".equalsIgnoreCase(resource.getType())) {
            if (dto.getDeskMode() == null) {
                throw new IllegalArgumentException("deskMode is required for DESK resources");
            }
            resource.setDeskMode(DeskMode.valueOf(dto.getDeskMode()));
        } else {
            // DB column is NOT NULL; use a harmless default for non-desk resources
            resource.setDeskMode(DeskMode.HOT_DESK);
        }
        if (StringUtils.hasText(dto.getDepartmentName())) {
            Department dept = departmentRepository.findByNameIgnoreCase(dto.getDepartmentName());
            if (dept == null) {
                throw new IllegalArgumentException("Invalid department name");
            }
            resource.setDepartment(dept);
        }
        if (StringUtils.hasText(dto.getFloorName())) {
            Floor floor = floorRepository.findByNameIgnoreCase(dto.getFloorName());
            if (floor == null) {
                throw new IllegalArgumentException("Invalid floor name");
            }
            resource.setFloor(floor);
        }
        return resource;
    }

    private ResourceResponseDTO toDTO(Resource resource) {
        ResourceResponseDTO dto = new ResourceResponseDTO();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setType(resource.getType());
        dto.setCapacity(resource.getCapacity());
        dto.setFeatures(resource.getFeatures());
        dto.setDeskMode(resource.getDeskMode() != null ? resource.getDeskMode().name() : null);
        dto.setDepartmentName(resource.getDepartment() != null ? resource.getDepartment().getName() : null);
        dto.setFloorName(resource.getFloor() != null ? resource.getFloor().getName() : null);
        return dto;
    }

    @Override
    public String createResource(ResourceRequestDTO resourceRequestDTO) {
        UserAccount user = JwtUtil.getCurrentUser();
        Resource resource = toEntity(resourceRequestDTO);
        logger.info("User {} creating resource: {}", user.getUsername(), resource);
        resourceRepository.save(resource);
        return "Resource created successfully";
    }

    @Override
    public String updateResource(Long id, ResourceRequestDTO resourceRequestDTO) {
        UserAccount user = JwtUtil.getCurrentUser();
        Resource existing = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
        existing.setName(resourceRequestDTO.getName());
        existing.setType(resourceRequestDTO.getType());
        if (StringUtils.hasText(resourceRequestDTO.getDepartmentName())) {
            Department dept = departmentRepository.findByNameIgnoreCase(resourceRequestDTO.getDepartmentName());
            if (dept == null) {
                throw new IllegalArgumentException("Invalid department name");
            }
            existing.setDepartment(dept);
        }
        if (StringUtils.hasText(resourceRequestDTO.getFloorName())) {
            Floor floor = floorRepository.findByNameIgnoreCase(resourceRequestDTO.getFloorName());
            if (floor == null) {
                throw new IllegalArgumentException("Invalid floor name");
            }
            existing.setFloor(floor);
        }
        existing.setCapacity(resourceRequestDTO.getCapacity());
        existing.setFeatures(resourceRequestDTO.getFeatures());
        existing.setDeskMode(DeskMode.valueOf(resourceRequestDTO.getDeskMode()));

        resourceRepository.save(existing);
        logger.info("System Admin {} updated resource {}", user.getUsername(), id);
        return " Resource Updated Successfully";
    }

    @Override
    public void deleteResource(Long id) {
        UserAccount user = JwtUtil.getCurrentUser();
        if (!"SYSTEM_ADMIN".equals(user.getRole().getName())) {
            throw new AccessDeniedException("Only system admin can manage resources");
        }
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found for deletion: " + id);
        }
        logger.info("Deleting resource: {}", id);
        resourceRepository.deleteById(id);
    }

    @Override
    public ResourceResponseDTO getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found for id: " + id));
        return toDTO(resource);
    }

    @Override
    public List<ResourceResponseDTO> searchResources(
            String type,
            String floorName,
            Integer capacity,
            String departmentName,
            List<String> features) {

        Long deptId = null;
        if (StringUtils.hasText(departmentName)) {
            Department department = departmentRepository.findByNameIgnoreCase(departmentName);
            if (department == null) {
                throw new ResourceNotFoundException("Department not found: " + departmentName);
            }
            deptId = department.getId();
        }

        Long floorId = null;
        if (StringUtils.hasText(floorName)) {
            Floor floor = floorRepository.findByNameIgnoreCase(floorName);
            if (floor == null) {
                throw new ResourceNotFoundException("Floor not found: " + floorName);
            }
            floorId = floor.getId();
        }

        List<Resource> resources = resourceRepository.searchResources(
                type,
                floorId,
                capacity,
                deptId,
                features
        );

        return resources.stream().map(this::toDTO).toList();
    }
}
