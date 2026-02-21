package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
