package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.dto.DeskAssignmentDTO;
import com.example.vertexspace_server.dto.ResourceResponseDTO;
import com.example.vertexspace_server.dto.UserSummaryDTO;
import com.example.vertexspace_server.exception.DeskAssignmentException;
import com.example.vertexspace_server.exception.InvalidDeskModeException;
import com.example.vertexspace_server.exception.ResourceNotFoundException;
import com.example.vertexspace_server.exception.UnauthorizedException;
import com.example.vertexspace_server.model.DeskAssignment;
import com.example.vertexspace_server.model.DeskMode;
import com.example.vertexspace_server.model.Department;
import com.example.vertexspace_server.model.Resource;
import com.example.vertexspace_server.model.UserAccount;
import com.example.vertexspace_server.repository.DeskAssignmentRepository;
import com.example.vertexspace_server.repository.ResourceRepository;
import com.example.vertexspace_server.repository.UserAccountRepository;
import com.example.vertexspace_server.security.JwtUtil;
import com.example.vertexspace_server.service.DeskAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DeskAssignmentServiceImpl implements DeskAssignmentService {

    private final DeskAssignmentRepository deskAssignmentRepository;
    private final ResourceRepository resourceRepository;
    private final UserAccountRepository userAccountRepository;

    public DeskAssignmentServiceImpl(
            DeskAssignmentRepository deskAssignmentRepository,
            ResourceRepository resourceRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.deskAssignmentRepository = deskAssignmentRepository;
        this.resourceRepository = resourceRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public String assignDesk(DeskAssignmentDTO dto) {

        UserAccount currentUser = JwtUtil.getCurrentUser();

        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Desk not found"));

        // Only desks can be assigned
        if (!"DESK".equalsIgnoreCase(resource.getType())) {
            throw new InvalidDeskModeException("Resource is not a desk");
        }

        UserAccount targetUser = userAccountRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Rule 1: Desk must be ASSIGNED type
        if (resource.getDeskMode() != DeskMode.ASSIGNED) {
            throw new InvalidDeskModeException("Desk is not in ASSIGNED mode");
        }

        // Rule 2: Department Admin can assign only within department
        String roleName = currentUser.getRole().getName();

        if ("DEPARTMENT_ADMIN".equalsIgnoreCase(roleName)) {

            if (currentUser.getDepartment() == null ||
                    resource.getDepartment() == null ||
                    !resource.getDepartment().getId()
                            .equals(currentUser.getDepartment().getId())) {

                throw new UnauthorizedException(
                        "Department Admin cannot assign desk outside their department"
                );
            }
        }

        // Parse dates
        Instant start = Instant.parse(dto.getStartUtc());
        Instant end = dto.getEndUtc() != null
                ? Instant.parse(dto.getEndUtc())
                : null;

        if (end != null && !end.isAfter(start)) {
            throw new DeskAssignmentException("End time must be after start time");
        }

        // Rule 3: Prevent overlap
        List<DeskAssignment> overlaps;

        if (end != null) {
            overlaps = deskAssignmentRepository.findOverlappingWithEnd(
                    resource.getId(),
                    start,
                    end
            );
        } else {
            overlaps = deskAssignmentRepository.findOverlappingIndefinite(
                    resource.getId(),
                    start
            );
        }

        if (!overlaps.isEmpty()) {
            throw new DeskAssignmentException("Desk assignment overlaps with existing assignment");
        }

        DeskAssignment assignment = new DeskAssignment();
        assignment.setResource(resource);
        assignment.setUser(targetUser);
        assignment.setStartUtc(start);
        assignment.setEndUtc(end);

        deskAssignmentRepository.save(assignment);

        return "Desk assigned successfully";
    }

    @Override
    public String unassignDesk(Long assignmentId) {
        UserAccount currentUser = JwtUtil.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        DeskAssignment assignment = deskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Desk assignment not found"));

        if ("DEPARTMENT_ADMIN".equalsIgnoreCase(roleName)) {
            if (currentUser.getDepartment() == null || assignment.getResource().getDepartment() == null ||
                    !assignment.getResource().getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                throw new UnauthorizedException("Department Admin cannot unassign outside their department");
            }
        }

        deskAssignmentRepository.delete(assignment);
        return "Desk unassigned successfully";
    }

    @Override
    public List<ResourceResponseDTO> getDesksWithAssignments() {
        UserAccount currentUser = JwtUtil.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        List<Resource> resources = new ArrayList<>();
        if ("SYSTEM_ADMIN".equalsIgnoreCase(roleName)) {
            resources = deskAssignmentRepository.findAssignedDeskResources(null);
        } else if ("DEPARTMENT_ADMIN".equalsIgnoreCase(roleName)) {
            Department dept = currentUser.getDepartment();
            if (dept == null) {
                throw new UnauthorizedException("Department Admin has no department assigned");
            }
            resources = deskAssignmentRepository.findAssignedDeskResources(dept.getId());
        }

        return resources.stream().map(this::toResourceDTO).toList();
    }

    @Override
    public List<ResourceResponseDTO> getAssignedDesks() {
        UserAccount currentUser = JwtUtil.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        List<Resource> resources = new ArrayList<>();
        if ("SYSTEM_ADMIN".equalsIgnoreCase(roleName)) {
            resources = resourceRepository.findAssignedDesks();
        } else if ("DEPARTMENT_ADMIN".equalsIgnoreCase(roleName)) {
            Department dept = currentUser.getDepartment();
            if (dept == null) {
                throw new UnauthorizedException("Department Admin has no department assigned");
            }
            resources = resourceRepository.findAssignedDesksByDepartment(dept.getId());
        }

        return resources.stream().map(this::toResourceDTO).toList();
    }

    @Override
    public List<UserSummaryDTO> getAssignableUsers() {
        UserAccount currentUser = JwtUtil.getCurrentUser();
        String roleName = currentUser.getRole().getName();

        List<UserAccount> users = new ArrayList<>();
        if ("SYSTEM_ADMIN".equalsIgnoreCase(roleName)) {
            users = userAccountRepository.findAll();
        } else if ("DEPARTMENT_ADMIN".equalsIgnoreCase(roleName)) {
            Department dept = currentUser.getDepartment();
            if (dept == null) {
                throw new UnauthorizedException("Department Admin has no department assigned");
            }
            users = userAccountRepository.findByDepartmentId(dept.getId());
        }

        return users.stream().map(this::toUserDTO).toList();
    }

    private ResourceResponseDTO toResourceDTO(Resource resource) {
        ResourceResponseDTO dto = new ResourceResponseDTO();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setType(resource.getType());
        dto.setCapacity(resource.getCapacity());
        dto.setDepartmentName(resource.getDepartment() != null ? resource.getDepartment().getName() : null);
        dto.setFloorName(resource.getFloor() != null ? resource.getFloor().getName() : null);
        dto.setDeskMode(resource.getDeskMode() != null ? resource.getDeskMode().name() : null);
        dto.setFeatures(resource.getFeatures());
        return dto;
    }

    private UserSummaryDTO toUserDTO(UserAccount user) {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getName() : null);
        return dto;
    }
}
