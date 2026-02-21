package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.dto.BookingRequestDTO;
import com.example.vertexspace_server.dto.BookingResponseDTO;
import com.example.vertexspace_server.exception.BookingConflictException;
import com.example.vertexspace_server.exception.InvalidBookingTimeException;
import com.example.vertexspace_server.exception.ResourceNotFoundException;
import com.example.vertexspace_server.model.Booking;
import com.example.vertexspace_server.model.Resource;
import com.example.vertexspace_server.model.UserAccount;
import com.example.vertexspace_server.repository.BookingRepository;
import com.example.vertexspace_server.repository.ResourceRepository;
import com.example.vertexspace_server.repository.UserAccountRepository;
import com.example.vertexspace_server.service.BookingService;
import com.example.vertexspace_server.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO bookingRequestDTO) {
        UserAccount user = JwtUtil.getCurrentUser();
        Booking booking = toEntity(bookingRequestDTO, user);
        // 1. Validate 15-min grid alignment
        if (!isAlignedTo15MinGrid(booking.getStartUtc()) || !isAlignedTo15MinGrid(booking.getEndUtc())) {
            throw new InvalidBookingTimeException("Start and end times must align to a 15-minute grid");
        }
        // 2. Add 15-min buffer to end
        Instant bufferEnd = booking.getEndUtc().plusSeconds(15 * 60);
        // 3. Check for conflicts (including buffer)
        List<Booking> conflicts = bookingRepository.findConflicts(
            booking.getResource().getId(), booking.getStartUtc(), bufferEnd);
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("Booking conflicts with existing booking(s)");
        }
        // 4. Save booking
        logger.info("Creating booking: {}", booking);
        Booking saved = bookingRepository.save(booking);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id) {
        Long userId= JwtUtil.getCurrentUser().getId();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        // Add role-based cancellation logic here
        logger.info("Cancelling booking: {} by user {}", id + "", userId);
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    @Override
    public List<BookingResponseDTO> listBookingsByUser() {
        Long userId = JwtUtil.getCurrentUser().getId();
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream().map(this::toDTO).toList();
    }

    @Override
    public List<BookingResponseDTO> listBookingsByResource(Long resourceId,String startUtc, String endUtc) {
        List<Booking> bookings = bookingRepository.findByResourceIdAndTimeRange(resourceId, Instant.parse(startUtc), Instant.parse(endUtc));
        return bookings.stream().map(this::toDTO).toList();
    }

    @Override
    public List<BookingResponseDTO> listBookingsByDateRange(String startUtc, String endUtc) {
        List<Booking> bookings = bookingRepository.findByTimeRange(Instant.parse(startUtc), Instant.parse(endUtc));
        return bookings.stream().map(this::toDTO).toList();
    }

    private boolean isAlignedTo15MinGrid(Instant instant) {
        ZoneId ist = ZoneId.of("Asia/Kolkata");

        ZonedDateTime istTime = instant.atZone(ist);

        int minute = istTime.getMinute();
        int second = istTime.getSecond();

        return minute % 15 == 0 && second == 0;
    }

    private Booking toEntity(BookingRequestDTO dto, UserAccount user) {
        Booking booking = new Booking();
        booking.setResource(resourceRepository.findById(dto.getResourceId()).orElseThrow(() -> new IllegalArgumentException("Invalid resourceId")));
        booking.setUser(user);
        booking.setStartUtc(dto.getStartUtc());
        booking.setEndUtc(dto.getEndUtc());
        booking.setStatus("CONFIRMED");
        return booking;
    }

    private BookingResponseDTO toDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setResourceId(booking.getResource() != null ? booking.getResource().getId() : null);
        dto.setUserId(booking.getUser() != null ? booking.getUser().getId() : null);
        dto.setStartUtc(booking.getStartUtc());
        dto.setEndUtc(booking.getEndUtc());
        dto.setStatus(booking.getStatus());
        return dto;
    }
}
