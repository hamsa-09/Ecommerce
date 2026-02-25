package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByNameIgnoreCase(String name);
}
