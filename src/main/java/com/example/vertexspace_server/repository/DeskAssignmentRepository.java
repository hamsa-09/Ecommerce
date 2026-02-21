package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.DeskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeskAssignmentRepository extends JpaRepository<DeskAssignment, Long> {
}
