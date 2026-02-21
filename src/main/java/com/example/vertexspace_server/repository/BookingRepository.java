package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId AND b.status = 'CONFIRMED' AND " +
           "((b.startUtc < :bufferEnd AND b.endUtc > :startUtc))")
    List<Booking> findConflicts(@Param("resourceId") Long resourceId,
                                @Param("startUtc") Instant startUtc,
                                @Param("bufferEnd") Instant bufferEnd);

    List<Booking> findByUserId(Long userId);

    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId AND b.startUtc >= :startUtc AND b.endUtc <= :endUtc")
    List<Booking> findByResourceIdAndTimeRange(@Param("resourceId") Long resourceId,
                                               @Param("startUtc") Instant startUtc,
                                               @Param("endUtc") Instant endUtc);

    @Query("SELECT b FROM Booking b WHERE b.startUtc >= :startUtc AND b.endUtc <= :endUtc")
    List<Booking> findByTimeRange(@Param("startUtc") Instant startUtc,
                                  @Param("endUtc") Instant endUtc);
}
