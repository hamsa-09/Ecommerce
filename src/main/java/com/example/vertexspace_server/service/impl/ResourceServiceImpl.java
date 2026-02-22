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
        if(resource.getType().equals("DESK")) {
            resource.setDeskMode(DeskMode.valueOf(dto.getDeskMode()));
        }
        else{
            resource.setDeskMode(null);
        }
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid departmentId"));
            resource.setDepartment(dept);
        }
        if (dto.getFloorId() != null) {
            Floor floor = floorRepository.findById(dto.getFloorId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid floorId"));
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
        dto.setDeskMode(resource.getDeskMode().name());
        dto.setDepartmentId(resource.getDepartment() != null ? resource.getDepartment().getId() : null);
        dto.setFloorId(resource.getFloor() != null ? resource.getFloor().getId() : null);
        return dto;
    }

    @Override
    public String createResource(ResourceRequestDTO resourceRequestDTO) {
        UserAccount user = JwtUtil.getCurrentUser();
        Resource resource = toEntity(resourceRequestDTO);
        // Only System Admin can set departmentId/type arbitrarily
        if (!user.getRole().getName().equals("SYSTEM_ADMIN")) {
            // Department Admin can only create resources in their own department
            if (!user.getRole().getName().equals("DEPARTMENT_ADMIN") ||
                !resource.getDepartment().getId().equals(user.getDepartment().getId())) {
                logger.warn("User {} not permitted to create resource in department {}", user.getUsername(), resource.getDepartment().getId());
                throw new AccessDeniedException("Not permitted to create resource in this department");
            }
            // Department Admin cannot create resources of type DESK with mode HOT_DESK
            System.out.println("Resource type: " + resource.getType() + ", Desk mode: " + resource.getDeskMode().name());
            if (resource.getType().equals("DESK") && resource.getDeskMode().name().equalsIgnoreCase(DeskMode.HOT_DESK.name())) {
                logger.warn("Department Admin {} cannot create HOT_DESK", user.getUsername());
                throw new AccessDeniedException("Department Admin cannot create HOT_DESK");
            }
        }
        logger.info("User {} creating resource: {}", user.getUsername(), resource);
        Resource saved = resourceRepository.save(resource);
        return "Resource created successfully";
    }

    @Override
    public String updateResource(Long id, ResourceRequestDTO resourceRequestDTO) {
        UserAccount user = JwtUtil.getCurrentUser();
        Resource existing = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
        // System Admin can update all fields
        if (user.getRole().getName().equals("SYSTEM_ADMIN")) {
            existing.setName(resourceRequestDTO.getName());
            existing.setType(resourceRequestDTO.getType());
            if (resourceRequestDTO.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(resourceRequestDTO.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid departmentId"));
                existing.setDepartment(dept);
            }
            existing.setCapacity(resourceRequestDTO.getCapacity());
            existing.setFeatures(resourceRequestDTO.getFeatures());
            if (existing.getType().equals("DESK")) {
                existing.setDeskMode(DeskMode.valueOf(resourceRequestDTO.getDeskMode()));
            }
            resourceRepository.save(existing);
            logger.info("System Admin {} updated resource {}", user.getUsername(), id);
            return " Resource Updated Successfully";
        }
        // Department Admin: can only update resources in their department
        if (user.getRole().getName().equals("DEPARTMENT_ADMIN") &&
            existing.getDepartment().getId().equals(user.getDepartment().getId())) {
            // Cannot change type or department
            if (!existing.getType().equals(resourceRequestDTO.getType()) ||
                !existing.getDepartment().getId().equals(resourceRequestDTO.getDepartmentId())) {
                logger.warn("Department Admin {} cannot change type/department for resource {}", user.getUsername(), id);
                throw new AccessDeniedException("Unauthorised to change type or department");
            }
            // For DESK, cannot change desk mode
            System.out.println("Existing desk mode: " + existing.getDeskMode().name()+ ", Requested desk mode: " + resourceRequestDTO.getDeskMode());
            if (existing.getType().equals("DESK") &&
                !existing.getDeskMode().name().equalsIgnoreCase(resourceRequestDTO.getDeskMode())) {
                logger.warn("Department Admin {} cannot change desk mode for resource {}", user.getUsername(), id);
                throw new AccessDeniedException("Unauthorised to change desk mode");
            }
            // Can update other fields
            existing.setName(resourceRequestDTO.getName());
            existing.setCapacity(resourceRequestDTO.getCapacity());
            existing.setFeatures(resourceRequestDTO.getFeatures());
            resourceRepository.save(existing);
            logger.info("Department Admin {} updated resource {}", user.getUsername(), id);
            return "Resource Updated Successfully";
        }
        logger.warn("User {} not permitted to update resource {}", user.getUsername(), id);
        throw new AccessDeniedException("Not permitted to update this resource");
    }

    @Override
    public void deleteResource(Long id) {
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
            Long floorId,
            Integer capacity,
            String departmentId,
            List<String> features) {

        Long deptId = null;
        if (departmentId != null && !departmentId.isEmpty()) {
            deptId = Long.parseLong(departmentId);
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
