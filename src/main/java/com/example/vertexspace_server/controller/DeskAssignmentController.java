package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.DeskAssignmentDTO;
import com.example.vertexspace_server.dto.ResourceResponseDTO;
import com.example.vertexspace_server.dto.SuccessResponse;
import com.example.vertexspace_server.dto.UserSummaryDTO;
import com.example.vertexspace_server.service.DeskAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/desk-assignments")
public class DeskAssignmentController {
    private final DeskAssignmentService deskAssignmentService;

    public DeskAssignmentController(DeskAssignmentService deskAssignmentService) {
        this.deskAssignmentService = deskAssignmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<SuccessResponse<String>> assignDesk(@RequestBody DeskAssignmentDTO deskAssignmentDTO) {
        return ResponseEntity.ok(new SuccessResponse<>(deskAssignmentService.assignDesk(deskAssignmentDTO)));
    }

    @GetMapping("/desks")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<SuccessResponse<List<ResourceResponseDTO>>> getAssignedDesks() {
        return ResponseEntity.ok(new SuccessResponse<>(deskAssignmentService.getAssignedDesks()));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<SuccessResponse<List<UserSummaryDTO>>> getAssignableUsers() {
        return ResponseEntity.ok(new SuccessResponse<>(deskAssignmentService.getAssignableUsers()));
    }

    @GetMapping("/assigned-desks")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<SuccessResponse<List<ResourceResponseDTO>>> getDesksWithAssignments() {
        return ResponseEntity.ok(new SuccessResponse<>(deskAssignmentService.getDesksWithAssignments()));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<SuccessResponse<String>> unassignDesk(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(new SuccessResponse<>(deskAssignmentService.unassignDesk(assignmentId)));
    }
}
