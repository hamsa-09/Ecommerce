package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.ResourceRequestDTO;
import com.example.vertexspace_server.dto.ResourceResponseDTO;
import com.example.vertexspace_server.dto.SuccessResponse;
import com.example.vertexspace_server.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SuccessResponse<String>> createResource(@RequestBody ResourceRequestDTO resourceRequestDTO) {
        return ResponseEntity.status(201).body(new SuccessResponse<>(resourceService.createResource(resourceRequestDTO)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SuccessResponse<String>> updateResource(@PathVariable Long id, @RequestBody ResourceRequestDTO resourceRequestDTO) {
        return ResponseEntity.ok(new SuccessResponse<>(resourceService.updateResource(id, resourceRequestDTO)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ResourceResponseDTO>> getResourceById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse<>(resourceService.getResourceById(id)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<List<ResourceResponseDTO>>> searchResources(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String floorName,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) List<String> features)
    {
        return ResponseEntity.ok(new SuccessResponse<>(resourceService.searchResources(type, floorName, capacity, departmentName, features)));
    }
}
