package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    // Pessimistic lock for booking creation
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resource r WHERE r.id = :id")
    Resource findByIdForUpdate(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT r FROM Resource r
        LEFT JOIN r.features f
        WHERE (:type IS NULL OR r.type = :type)
        AND (:floorId IS NULL OR r.floor.id = :floorId)
        AND (:capacity IS NULL OR r.capacity >= :capacity)
        AND (:deptId IS NULL OR r.department.id = :deptId)
        AND (:features IS NULL OR f IN :features)
    """)
    List<Resource> searchResources(
            @Param("type") String type,
            @Param("floorId") Long floorId,
            @Param("capacity") Integer capacity,
            @Param("deptId") Long deptId,
            @Param("features") List<String> features
    );
}