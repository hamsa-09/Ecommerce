package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface BuildingRepository extends JpaRepository<Building, Long> {
    Building findByNameIgnoreCase(String name);
}
