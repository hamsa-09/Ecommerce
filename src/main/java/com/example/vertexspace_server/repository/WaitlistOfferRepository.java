package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.WaitlistOffer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WaitlistOfferRepository extends JpaRepository<WaitlistOffer, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM WaitlistOffer o WHERE o.id = :id")
    Optional<WaitlistOffer> findByIdForUpdate(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM WaitlistOffer o WHERE o.waitlistEntry.id = :entryId")
    Optional<WaitlistOffer> findByWaitlistEntryIdForUpdate(@Param("entryId") Long entryId);

    @Query("""
    SELECT COUNT(o) > 0 FROM WaitlistOffer o
        WHERE o.waitlistEntry.resource.id = :resourceId
        AND o.waitlistEntry.startUtc = :startUtc
        AND o.waitlistEntry.endUtc = :endUtc
        AND o.status = 'OFFERED'
    """)
    boolean existsActiveOffer(
                Long resourceId,
                Instant startUtc,
                Instant endUtc
    );

    @Query("""
        SELECT o FROM WaitlistOffer o
        WHERE o.status = 'OFFERED'
        AND o.expiresAtUtc <= :now
     """)
    List<WaitlistOffer> findExpiredOffers(Instant now);
    @Query("""
        SELECT o FROM WaitlistOffer o
        WHERE o.waitlistEntry.user.id = :userId
        AND o.waitlistEntry.resource.id = :resourceId
        AND o.waitlistEntry.startUtc = :startUtc
        AND o.waitlistEntry.endUtc = :endUtc
        AND o.status = 'OFFERED'
    """)
    Optional<WaitlistOffer> findActiveOfferForUser(
            @Param("userId") Long userId,
            @Param("resourceId") Long resourceId,
            @Param("startUtc") Instant startUtc,
            @Param("endUtc") Instant endUtc
    );

    Optional<WaitlistOffer> findTopByWaitlistEntryIdOrderByIdDesc(Long id);
}
