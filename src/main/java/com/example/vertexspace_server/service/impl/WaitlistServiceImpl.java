package com.example.vertexspace_server.service.impl;
import com.example.vertexspace_server.dto.WaitlistEntryDTO;
import com.example.vertexspace_server.dto.WaitlistJoinDTO;
import com.example.vertexspace_server.dto.WaitlistStatusDTO;
import com.example.vertexspace_server.exception.ResourceNotFoundException;
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
        Long resourceId = waitlistJoinDTO.getResourceId();
        Instant startUtc = Instant.parse(waitlistJoinDTO.getStartUtc());
        Instant endUtc = Instant.parse(waitlistJoinDTO.getEndUtc());
        UserAccount user = JwtUtil.getCurrentUser();
        List<Booking> bookingList= bookingRepo.findByResourceIdAndTimeRange(resourceId, startUtc, endUtc);
        if(bookingList.isEmpty()){
            throw new ResourceNotFoundException("Booking Not Found for the given resource and time slot");
        }
        Resource resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + resourceId));

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
    public WaitlistStatusDTO getWaitlistStatus(Long resourceId,
                                                   Instant startUtc,
                                                   Instant endUtc) {

        UserAccount user = JwtUtil.getCurrentUser();

        // Find current user's waitlist entry
        Optional<WaitlistEntry> entryOpt =
                waitlistEntryRepo.findByUserAndSlot(
                        user.getId(),
                        resourceId,
                        startUtc,
                        endUtc
                );

        if (entryOpt.isEmpty()) {
            return null; // Not in waitlist
        }

        WaitlistEntry entry = entryOpt.get();

        // Get full FIFO queue for that slot
        List<WaitlistEntry> queue =
                waitlistEntryRepo.findFIFO(resourceId, startUtc, endUtc);

        int position = 1;
        for (WaitlistEntry e : queue) {
            if (e.getId().equals(entry.getId())) break;
            position++;
        }

        WaitlistStatusDTO dto = toStatusDTO(entry);
        dto.setQueuePosition(position);
        dto.setOfferStatus("WAITLIST");

        // Check active offer
        Optional<WaitlistOffer> activeOffer =
                offerRepo.findActiveOfferForUser(
                        user.getId(),
                        resourceId,
                        startUtc,
                        endUtc
                );

        activeOffer.ifPresent(offer -> {
            dto.setOfferStatus(offer.getStatus().name());
            dto.setOfferExpiresAt(offer.getExpiresAtUtc());
        });

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

        // Get FIFO waitlist
        List<WaitlistEntry> queue =
                waitlistEntryRepo.findFIFO(resourceId, slotStart, slotEnd);

        if (queue.isEmpty()) return;

        WaitlistEntry nextUser = queue.get(0);

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
    public String acceptOffer(Long offerId) {

        WaitlistOffer offer = offerRepo.findByIdForUpdate(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

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

        for (WaitlistOffer offer : expired) {

            offer.setStatus(OfferStatus.EXPIRED);
            offerRepo.save(offer);

            // Delete provisional booking
            bookingRepo.delete(offer.getProvisionalBooking());

            // Remove user from waitlist
            waitlistEntryRepo.delete(offer.getWaitlistEntry());

            // Trigger next FIFO
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
