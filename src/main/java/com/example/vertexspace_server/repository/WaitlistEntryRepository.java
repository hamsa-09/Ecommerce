package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.UserAccount;
import com.example.vertexspace_server.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
    List<WaitlistEntry> findByResourceId(Long resourceId);

    @Query("SELECT w FROM WaitlistEntry w WHERE w.resource.id = :resourceId AND w.startUtc = :slotStart AND w.endUtc = :slotEnd ORDER BY w.createdAtUtc ASC")
    List<WaitlistEntry> findByResourceIdAndSlot(@Param("resourceId") Long resourceId, @Param("slotStart") Instant slotStart, @Param("slotEnd") Instant slotEnd);

    List<WaitlistEntry> findByResourceIdAndUserId(Long resourceId, Long userId);
    @Query("""
        SELECT w FROM WaitlistEntry w
        WHERE w.resource.id = :resourceId
        AND w.startUtc = :startUtc
        AND w.endUtc = :endUtc
        ORDER BY w.createdAtUtc ASC
    """)
    List<WaitlistEntry> findFIFO(
                Long resourceId,
                Instant startUtc,
                Instant endUtc
    );

    void deleteByUserIdAndResourceIdAndStartUtcAndEndUtc(
                Long userId,
                Long resourceId,
                Instant startUtc,
                Instant endUtc
    );
    @Query("""
    SELECT w FROM WaitlistEntry w
    WHERE w.user.id = :userId
    AND w.resource.id = :resourceId
    AND w.startUtc = :startUtc
    AND w.endUtc = :endUtc
""")
    Optional<WaitlistEntry> findByUserAndSlot(
            @Param("userId") Long userId,
            @Param("resourceId") Long resourceId,
            @Param("startUtc") Instant startUtc,
            @Param("endUtc") Instant endUtc
    );


    WaitlistEntry findByUserAndId(UserAccount user, Long id);

    @Query("""
        SELECT e FROM WaitlistEntry e
        WHERE e.resource.id = :resourceId
        AND e.startUtc = :startUtc
        AND e.endUtc = :endUtc
        AND NOT EXISTS (
            SELECT 1 FROM WaitlistOffer o
            WHERE o.waitlistEntry = e
            AND (o.status = 'OFFERED' OR o.status = 'EXPIRED')
        )
        ORDER BY e.createdAtUtc ASC
    """)
    Optional<WaitlistEntry> findFirstEligible(Long resourceId, Instant startUtc, Instant endUtc);
}
