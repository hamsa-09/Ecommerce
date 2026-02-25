package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Conflict detection with buffer logic
    @Query("""
        SELECT b FROM Booking b
        WHERE b.resource.id = :resourceId
        AND b.status = 'CONFIRMED'
        AND (b.startUtc < :bufferEnd AND b.endUtc > :startUtc)
    """)
    List<Booking> findConflicts(
            @Param("resourceId") Long resourceId,
            @Param("startUtc") Instant startUtc,
            @Param("bufferEnd") Instant bufferEnd
    );

    List<Booking> findByUserId(Long userId);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.resource.id = :resourceId
        AND b.startUtc >= :startUtc
        AND b.endUtc <= :endUtc
    """)
    List<Booking> findByResourceIdAndTimeRange(
            @Param("resourceId") Long resourceId,
            @Param("startUtc") Instant startUtc,
            @Param("endUtc") Instant endUtc
    );

    @Query("""
        SELECT b FROM Booking b
        WHERE b.startUtc >= :startUtc
        AND b.endUtc <= :endUtc
    """)
    List<Booking> findByTimeRange(
            @Param("startUtc") Instant startUtc,
            @Param("endUtc") Instant endUtc
    );
    List<Booking> findByRecurrenceGroupId(UUID recurrenceGroupId);
    @Query("""
        SELECT b FROM Booking b
        WHERE b.resource.id = :resourceId
        AND b.status = 'CONFIRMED'
        AND b.startUtc < :end
        AND b.endUtc > :start
        """)
    List<Booking> findActiveBookingsForRange(Long resourceId,
                                             Instant start,
                                             Instant end);
    @Query("""
        SELECT r.id, r.name, COUNT(b.id)
        FROM Booking b
        JOIN b.resource r
        WHERE b.user.id = :userId
        AND b.status = 'CONFIRMED'
        AND b.startUtc > :fromDate
        GROUP BY r.id, r.name
        ORDER BY COUNT(b.id) DESC
        """)
    List<Object[]> findTopResourcesLast30Days(Long userId,
                                              Instant fromDate);
    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 'CONFIRMED'
        AND b.startUtc BETWEEN :start AND :end
    """)
    List<Booking> findBookingsStartingBetween(
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}