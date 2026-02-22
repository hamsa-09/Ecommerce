package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.DeskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface DeskAssignmentRepository extends JpaRepository<DeskAssignment, Long> {
    @Query("""
    SELECT d FROM DeskAssignment d
    WHERE d.resource.id = :resourceId
    AND (
        (d.endUtc IS NULL OR d.endUtc > :newStart)
        AND
        d.startUtc < :newEnd
    )
    """)
    List<DeskAssignment> findOverlappingWithEnd(
            @Param("resourceId") Long resourceId,
            @Param("newStart") Instant newStart,
            @Param("newEnd") Instant newEnd
    );
    @Query("""
    SELECT d FROM DeskAssignment d
    WHERE d.resource.id = :resourceId
    AND (d.endUtc IS NULL OR d.endUtc > :newStart)
    """)
    List<DeskAssignment> findOverlappingIndefinite(
            @Param("resourceId") Long resourceId,
            @Param("newStart") Instant newStart
    );
}
