package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.dto.BookingRequestDTO;
import com.example.vertexspace_server.dto.BookingResponseDTO;
import com.example.vertexspace_server.events.BookingCancelledEvent;
import com.example.vertexspace_server.exception.BookingConflictException;
import com.example.vertexspace_server.exception.InvalidBookingTimeException;
import com.example.vertexspace_server.exception.ResourceNotFoundException;
import com.example.vertexspace_server.model.*;
import com.example.vertexspace_server.repository.BookingRepository;
import com.example.vertexspace_server.repository.ResourceRepository;
import com.example.vertexspace_server.service.BookingService;
import com.example.vertexspace_server.service.NotificationService;
import com.example.vertexspace_server.service.WaitlistService;
import com.example.vertexspace_server.security.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final WaitlistService waitlistService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NotificationService notificationService;
    public BookingServiceImpl(
            BookingRepository bookingRepository,
            ResourceRepository resourceRepository,
            WaitlistService waitlistService, ApplicationEventPublisher applicationEventPublisher, NotificationService notificationService
    ) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.waitlistService = waitlistService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.notificationService = notificationService;
    }

    // =========================================================
    // CREATE BOOKING (Concurrency Safe)
    // =========================================================
    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO dto) {

        UserAccount user = JwtUtil.getCurrentUser();

        Resource resource = resourceRepository.findByIdForUpdate(dto.getResourceId());
        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found");
        }
        //todo if the  is resource desktype , assigned status should not allow to book
        if(resource.getType().equalsIgnoreCase("DESK") && resource.getDeskMode().name().equalsIgnoreCase("ASSIGNED") ){
            throw new InvalidBookingTimeException("Cannot book a desk that is in Assigned mode");
        }
        Instant now = Instant.now();

        if (dto.getStartUtc().isBefore(now)) {
            throw new InvalidBookingTimeException("Cannot book a past time slot");
        }

        if (!dto.getEndUtc().isAfter(dto.getStartUtc())) {
            throw new InvalidBookingTimeException("End time must be after start time");
        }

        if (!isAlignedTo15MinGrid(dto.getStartUtc()) ||
                !isAlignedTo15MinGrid(dto.getEndUtc())) {
            throw new InvalidBookingTimeException("Start and end must align to 15-minute grid");
        }

        boolean recurring = Boolean.TRUE.equals(dto.getRecurring());
        UUID recurrenceGroupId = recurring ? UUID.randomUUID() : null;
        int occurrences = recurring ? dto.getRecurrenceCount() : 1;
        String recurrenceType = recurring?dto.getRecurrenceType():"No recurrence";
        List<Booking> bookingsToSave = new ArrayList<>();

        for (int i = 0; i < occurrences; i++) {

            Instant start = calculateOccurrenceStart(dto, i);
            Instant end = calculateOccurrenceEnd(dto, i);

            if (start.isBefore(now)) {
                throw new InvalidBookingTimeException(
                        "Occurrence " + (i + 1) + " is in the past"
                );
            }

            if (!end.isAfter(start)) {
                throw new InvalidBookingTimeException(
                        "Invalid time range at occurrence " + (i + 1)
                );
            }

            Instant bufferEnd = end.plusSeconds(15 * 60);

            List<Booking> conflicts = bookingRepository.findConflicts(
                    resource.getId(),
                    start,
                    bufferEnd
            );

            if (!conflicts.isEmpty()) {
                throw new BookingConflictException(
                        "Conflict detected at occurrence " + (i + 1)
                );
            }

            Booking booking = new Booking();
            booking.setResource(resource);
            booking.setUser(user);
            booking.setStartUtc(start);
            booking.setEndUtc(end);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setRecurrenceGroupId(recurrenceGroupId);
            bookingsToSave.add(booking);
        }

        //  Single optimized DB call
        List<Booking> savedBookings = bookingRepository.saveAll(bookingsToSave);
        for(Booking b : savedBookings){
            logger.info("Created booking {} for resource {} from {} to {}",
                    b.getId(), b.getResource().getId(), b.getStartUtc(), b.getEndUtc());
            notificationService.sendNotification(
                    user,
                    "Your booking is confirmed for " + b.getStartUtc(),
                    NotificationType.BOOKING_CONFIRMED
            );
        }
        logger.info("Booking created with recurrence group {}", recurrenceGroupId);
        BookingResponseDTO response = toDTO(savedBookings.get(0));
        response.setRecurring(recurring);
        response.setRecurrenceType(recurrenceType);
        response.setRecurrenceCount(occurrences);

        return response; // return first occurrence
    }

    // =========================================================
    // CANCEL BOOKING (Concurrency Safe)
    // =========================================================
    @Override
    @Transactional
    public String cancelBooking(Long id) {

        Long userId = JwtUtil.getCurrentUser().getId();

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("You cannot cancel this booking");
        }

        if ("CANCELLED".equals(booking.getStatus().name())) {
            return "Booking Already Cancelled";
        }

        // Lock resource to serialize cancellation
        Resource resource = resourceRepository
                .findByIdForUpdate(booking.getResource().getId());

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        logger.info("Booking {} cancelled by user {}", id, userId);


        // Publish async domain event
        applicationEventPublisher.publishEvent(
                new BookingCancelledEvent(
                        resource.getId(),
                        saved.getStartUtc(),
                        saved.getEndUtc()
                )
        );
        notificationService.sendNotification(
                booking.getUser(),
                "Your booking has been cancelled.",
                NotificationType.BOOKING_CANCELLED
        );
        // Return immediately
        return "Booking Cancelled Successfully";
    }



    @Transactional
    public String cancelSeries(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getRecurrenceGroupId() == null) {
            return cancelBooking(bookingId);
        }

        List<Booking> series =
                bookingRepository.findByRecurrenceGroupId(
                        booking.getRecurrenceGroupId()
                );

        for (Booking b : series) {
            b.setStatus(BookingStatus.CANCELLED);
        }

        bookingRepository.saveAll(series);

        return "Entire series cancelled";
    }
    // =========================================================
    // LIST METHODS
    // =========================================================
    @Override
    public List<BookingResponseDTO> listBookingsByUser() {
        Long userId = JwtUtil.getCurrentUser().getId();
        return bookingRepository.findByUserId(userId)
                .stream().map(this::toDTO).toList();
    }

    @Override
    public List<BookingResponseDTO> listBookingsByResource(
            Long resourceId, String startUtc, String endUtc) {

        return bookingRepository.findByResourceIdAndTimeRange(
                resourceId,
                Instant.parse(startUtc),
                Instant.parse(endUtc)
        ).stream().map(this::toDTO).toList();
    }

    @Override
    public List<BookingResponseDTO> listBookingsByDateRange(
            String startUtc, String endUtc) {

        return bookingRepository.findByTimeRange(
                Instant.parse(startUtc),
                Instant.parse(endUtc)
        ).stream().map(this::toDTO).toList();
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
        logger.info("Deleted booking {} via batch cleanup", bookingId);
    }

    // =========================================================
    // UTILITY METHODS
    // =========================================================
    private Instant calculateOccurrenceStart(BookingRequestDTO dto, int index) {

        if (!Boolean.TRUE.equals(dto.getRecurring())) {
            return dto.getStartUtc();
        }

        return switch (dto.getRecurrenceType()) {
            case "DAILY" -> dto.getStartUtc().plusSeconds(86400L * index);
            case "WEEKLY" -> dto.getStartUtc().plusSeconds(86400L * 7 * index);
            default -> throw new RuntimeException("Unsupported recurrence type");
        };
    }

    private Instant calculateOccurrenceEnd(BookingRequestDTO dto, int index) {

        if (!Boolean.TRUE.equals(dto.getRecurring())) {
            return dto.getEndUtc();
        }

        return switch (dto.getRecurrenceType()) {
            case "DAILY" -> dto.getEndUtc().plusSeconds(86400L * index);
            case "WEEKLY" -> dto.getEndUtc().plusSeconds(86400L * 7 * index);
            default -> throw new RuntimeException("Unsupported recurrence type");
        };
    }
    private boolean isAlignedTo15MinGrid(Instant instant) {
        ZoneId ist = ZoneId.of("Asia/Kolkata");
        ZonedDateTime istTime = instant.atZone(ist);
        return istTime.getMinute() % 15 == 0 && istTime.getSecond() == 0;
    }

    private BookingResponseDTO toDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setResourceId(booking.getResource().getId());
        dto.setStartUtc(booking.getStartUtc());
        dto.setEndUtc(booking.getEndUtc());
        dto.setStatus(booking.getStatus().name());
        return dto;
    }
}