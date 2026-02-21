package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    @Query("SELECT r FROM Resource r WHERE (:type IS NULL OR r.type = :type) AND (:floorId IS NULL OR r.floor.id = :floorId) AND (:capacity IS NULL OR r.capacity >= :capacity) AND (:departmentId IS NULL OR r.department.id = :departmentId)")
    List<Resource> searchResources(@Param("type") String type, @Param("floorId") Long floorId, @Param("capacity") Integer capacity, @Param("departmentId") Long departmentId);
}
