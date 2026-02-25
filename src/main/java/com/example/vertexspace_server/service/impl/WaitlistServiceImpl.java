package com.example.vertexspace_server.service.impl;
import com.example.vertexspace_server.dto.WaitlistEntryDTO;
import com.example.vertexspace_server.dto.WaitlistJoinDTO;
import com.example.vertexspace_server.dto.WaitlistStatusDTO;
import com.example.vertexspace_server.exception.ResourceNotFoundException;
import com.example.vertexspace_server.exception.UnauthorizedException;
import com.example.vertexspace_server.model.*;
import com.example.vertexspace_server.repository.*;
import com.example.vertexspace_server.service.NotificationService;
import com.example.vertexspace_server.service.WaitlistService;
import com.example.vertexspace_server.security.JwtUtil;

import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WaitlistServiceImpl implements WaitlistService {
    private static final Logger logger = LoggerFactory.getLogger(WaitlistServiceImpl.class);

    private static final int OFFER_WINDOW_MINUTES = 10;

    private final WaitlistEntryRepository waitlistEntryRepo;
    private final WaitlistOfferRepository offerRepo;
    private final BookingRepository bookingRepo;
    private final ResourceRepository resourceRepo;

    private final NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    public WaitlistServiceImpl(
            WaitlistEntryRepository waitlistEntryRepo,
            WaitlistOfferRepository offerRepo,
            BookingRepository bookingRepo,
            ResourceRepository resourceRepo, NotificationService notificationService
    ) {
        this.waitlistEntryRepo = waitlistEntryRepo;
        this.offerRepo = offerRepo;
        this.bookingRepo = bookingRepo;
        this.resourceRepo = resourceRepo;
        this.notificationService = notificationService;
    }

    // =========================================================
    // JOIN WAITLIST
    // =========================================================
    @Override
    @Transactional
    public WaitlistEntryDTO joinWaitlist(WaitlistJoinDTO waitlistJoinDTO) {
        String resourceName = waitlistJoinDTO.getResourceName();
        Instant startUtc = Instant.parse(waitlistJoinDTO.getStartUtc());
        Instant endUtc = Instant.parse(waitlistJoinDTO.getEndUtc());
        Instant now = Instant.now();
        if (!startUtc.isAfter(now) || !endUtc.isAfter(startUtc)) {
            throw new UnauthorizedException("Cannot join waitlist for a past or invalid time slot");
        }
        UserAccount user = JwtUtil.getCurrentUser();

        Resource resource = resourceRepo.findByNameIgnoreCase(resourceName);
        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found with name: " + resourceName);
        }

        List<Booking> bookingList = bookingRepo.findByResourceIdAndTimeRange(resource.getId(), startUtc, endUtc);
        if(bookingList.isEmpty()){
            throw new ResourceNotFoundException("Booking Not Found for the given resource and time slot");
        }

        boolean userOwnsBooking = bookingList.stream()
                .anyMatch(b -> b.getUser() != null && b.getUser().getId().equals(user.getId()));
        boolean isSystemAdmin = user.getRole() != null && "SYSTEM_ADMIN".equalsIgnoreCase(user.getRole().getName());
        if (userOwnsBooking && !isSystemAdmin) {
            throw new UnauthorizedException("Cannot join waitlist for a slot you have already booked");
        }

        Optional<WaitlistEntry> existing = waitlistEntryRepo.findByUserAndSlot(
                user.getId(), resource.getId(), startUtc, endUtc);
        if (existing.isPresent()) {
            throw new UnauthorizedException("You are already in the waitlist for this slot");
        }

        WaitlistEntry entry = new WaitlistEntry();
        entry.setResource(resource);
        entry.setStartUtc(startUtc);
        entry.setEndUtc(endUtc);
        entry.setUser(user);
        entry.setCreatedAtUtc(Instant.now());

        WaitlistEntry saved = waitlistEntryRepo.save(entry);

        return toDTO(saved);
    }

    // =========================================================
    // LEAVE WAITLIST
    // =========================================================
    @Override
    @Transactional
    public void leaveWaitlist(Long waitlistEntryId) {
        //validate that entry belongs to current user
        UserAccount user = JwtUtil.getCurrentUser();
        WaitlistEntry entry = waitlistEntryRepo.findByUserAndId(user,waitlistEntryId);
        if(entry==null){
            throw new ResourceNotFoundException("Waitlist entry not found for user");
        }
        waitlistEntryRepo.delete(entry);
    }

    // =========================================================
    // STATUS
    // =========================================================
    @Override
    public WaitlistStatusDTO getWaitlistStatus(String resourceName,
                                                   Instant startUtc,
                                                   Instant endUtc) {

        Instant now = Instant.now();
        if (startUtc.isBefore(now)) {
            throw new UnauthorizedException("Cannot check waitlist status for past slots");
        }

        UserAccount user = JwtUtil.getCurrentUser();

        Resource resource = resourceRepo.findByNameIgnoreCase(resourceName);
        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found with name: " + resourceName);
        }

        Long resourceId = resource.getId();

        Optional<WaitlistEntry> entryOpt =
                waitlistEntryRepo.findByUserAndSlot(
                        user.getId(),
                        resourceId,
                        startUtc,
                        endUtc
                );

        if (entryOpt.isEmpty()) {
            throw new ResourceNotFoundException("You are not in the waitlist for this slot");
        }

        WaitlistEntry entry = entryOpt.get();

        // Get full FIFO queue for that slot
        List<WaitlistEntry> queue =
                waitlistEntryRepo.findFIFO(resourceId, startUtc, endUtc);

        WaitlistStatusDTO dto = toStatusDTO(entry);
        dto.setOfferStatus("WAITLIST");

        // Check latest offer (covers OFFERED/EXPIRED/etc.)
        Optional<WaitlistOffer> latestOffer = offerRepo.findTopByWaitlistEntryIdOrderByIdDesc(entry.getId());
        if (latestOffer.isPresent()) {
            OfferStatus status = latestOffer.get().getStatus();
            if (status == OfferStatus.EXPIRED) {
                throw new UnauthorizedException("Offer has expired for this waitlist entry");
            }
            dto.setOfferStatus(status.name());
            dto.setOfferExpiresAt(latestOffer.get().getExpiresAtUtc());
        }

        return dto;
    }


    // =========================================================
    // CREATE OFFER (FIFO + PROVISIONAL + SINGLE ACTIVE)
    // =========================================================
    @Override
    @Transactional
    public void createOfferForWaitlist(Long resourceId,
                                       Instant slotStart,
                                       Instant slotEnd) {

        // Lock resource row
        resourceRepo.findByIdForUpdate(resourceId);

        // Check if active offer already exists
        boolean activeOfferExists =
                offerRepo.existsActiveOffer(resourceId, slotStart, slotEnd);

        if (activeOfferExists) return;

        Optional<WaitlistEntry> candidateOpt = waitlistEntryRepo.findFirstEligible(resourceId, slotStart, slotEnd);
        if (candidateOpt.isEmpty()) {
            return;
        }

        WaitlistEntry nextUser = candidateOpt.get();

        //Create provisional booking (BLOCK SLOT)
        Booking provisional = new Booking();
        provisional.setResource(nextUser.getResource());
        provisional.setUser(nextUser.getUser());
        provisional.setStartUtc(slotStart);
        provisional.setEndUtc(slotEnd);
        provisional.setStatus(BookingStatus.PROVISIONAL);

        Booking savedBooking = bookingRepo.save(provisional);

        //Create offer
        WaitlistOffer offer = new WaitlistOffer();
        offer.setWaitlistEntry(nextUser);
        offer.setStatus(OfferStatus.OFFERED);
        offer.setOfferedAtUtc(Instant.now());
        offer.setExpiresAtUtc(
                Instant.now().plusSeconds(OFFER_WINDOW_MINUTES * 60));
        offer.setProvisionalBooking(savedBooking);
        notificationService.sendNotification(
                nextUser.getUser(),
                "A slot is available! You have been offered a booking.",
                NotificationType.BOOKING_OFFERED
        );
        offerRepo.save(offer);
    }

    // =========================================================
    // ACCEPT OFFER
    // =========================================================
    @Override
    @Transactional
    public String acceptOffer(Long waitlistEntryId) {

        WaitlistOffer offer = offerRepo.findByWaitlistEntryIdForUpdate(waitlistEntryId)
                .orElseThrow(() -> new RuntimeException("Offer not found for waitlist entry"));

        if (offer.getStatus() != OfferStatus.OFFERED)
            throw new RuntimeException("Offer not active");

        // Confirm provisional booking
        Booking booking = offer.getProvisionalBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepo.save(booking);

        offer.setStatus(OfferStatus.ACCEPTED);
        offerRepo.save(offer);

        // Remove from waitlist
        waitlistEntryRepo.delete(offer.getWaitlistEntry());

        return "Offer accepted. Booking confirmed.";
    }

    // =========================================================
    // SCHEDULED OFFER EXPIRY
    // =========================================================
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireOffers() {
        logger.info("Running scheduled task: expireOffers");
        List<WaitlistOffer> expired =
                offerRepo.findExpiredOffers(Instant.now());

        logger.info("Found {} expired offers", expired.size());
        for (WaitlistOffer offer : expired) {

            offer.setStatus(OfferStatus.EXPIRED);
            offerRepo.save(offer);

            // Delete provisional booking
            bookingRepo.delete(offer.getProvisionalBooking());

            // Trigger next FIFO (will skip expired entries and advance)
            createOfferForWaitlist(
                    offer.getWaitlistEntry().getResource().getId(),
                    offer.getWaitlistEntry().getStartUtc(),
                    offer.getWaitlistEntry().getEndUtc()
            );
        }
    }

    // =========================================================
    // DTO MAPPER
    // =========================================================
    private WaitlistEntryDTO toDTO(WaitlistEntry entry) {

        WaitlistEntryDTO dto = new WaitlistEntryDTO();
        dto.setId(entry.getId());
        dto.setResourceId(entry.getResource().getId());
        dto.setStartUtc(entry.getStartUtc());
        dto.setEndUtc(entry.getEndUtc());
        dto.setCreatedAtUtc(entry.getCreatedAtUtc());

        return dto;
    }
    // =========================================================
    // DTO MAPPER
    // =========================================================
    private WaitlistStatusDTO toStatusDTO(WaitlistEntry entry) {

        WaitlistStatusDTO dto = new WaitlistStatusDTO();
        dto.setId(entry.getId());
        dto.setResourceId(entry.getResource().getId());
        dto.setStartUtc(entry.getStartUtc());
        dto.setEndUtc(entry.getEndUtc());
        dto.setCreatedAtUtc(entry.getCreatedAtUtc());
        return dto;
    }
}
