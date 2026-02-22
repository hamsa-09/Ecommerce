package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.DeskAssignmentDTO;
import com.example.vertexspace_server.dto.SuccessResponse;
import com.example.vertexspace_server.service.DeskAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/desk-assignments")
public class DeskAssignmentController {
    private final DeskAssignmentService deskAssignmentService;

    public DeskAssignmentController(DeskAssignmentService deskAssignmentService) {
        this.deskAssignmentService = deskAssignmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'DEPARTMENT_ADMIN')")
    public  ResponseEntity<SuccessResponse<String>> assignDesk(@RequestBody DeskAssignmentDTO deskAssignmentDTO) {
        return ResponseEntity.ok(new SuccessResponse<>(deskAssignmentService.assignDesk(deskAssignmentDTO)));
    }
}
