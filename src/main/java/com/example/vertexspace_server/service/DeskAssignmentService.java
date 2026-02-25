package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.DeskAssignmentDTO;
import com.example.vertexspace_server.dto.ResourceResponseDTO;
import com.example.vertexspace_server.dto.UserSummaryDTO;
import java.util.List;


public interface DeskAssignmentService {
    String assignDesk(DeskAssignmentDTO deskAssignmentDTO);
    List<ResourceResponseDTO> getAssignedDesks();
    List<UserSummaryDTO> getAssignableUsers();
    String unassignDesk(Long assignmentId);
    List<ResourceResponseDTO> getDesksWithAssignments();
}
