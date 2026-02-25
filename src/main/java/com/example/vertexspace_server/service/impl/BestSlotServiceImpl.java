package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.exception.InvalidBookingTimeException;
import com.example.vertexspace_server.exception.ResourceNotFoundException;
import com.example.vertexspace_server.model.UserAccount;
import com.example.vertexspace_server.service.BestSlotService;
import com.example.vertexspace_server.dto.BestSlotDTO;
import com.example.vertexspace_server.model.Booking;
import com.example.vertexspace_server.model.Resource;
import com.example.vertexspace_server.repository.BookingRepository;
import com.example.vertexspace_server.repository.ResourceRepository;
import com.example.vertexspace_server.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BestSlotServiceImpl implements BestSlotService {

    private static final Logger logger = LoggerFactory.getLogger(BestSlotServiceImpl.class);

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int BUFFER_MINUTES = 15;
    private static final DateTimeFormatter REQUEST_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;

    public BestSlotServiceImpl(BookingRepository bookingRepository,
                               ResourceRepository resourceRepository) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public List<BestSlotDTO> findBestSlots(Long resourceId,
                                           String istDate,
                                           int durationMinutes) {

        UserAccount user = JwtUtil.getCurrentUser();

        logger.info("User {} requesting best slots for resource {} on {} for {} minutes",
                user.getId(), resourceId, istDate, durationMinutes);

        if (durationMinutes <= 0) {
            throw new InvalidBookingTimeException("Duration must be greater than zero");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Resource not found"));

        LocalDate date = parseRequestedDate(istDate);

        LocalDate todayIst = LocalDate.now(IST_ZONE);
        if (date.isBefore(todayIst)) {
            throw new InvalidBookingTimeException("Cannot search slots for past date");
        }

        LocalDateTime istDayStart = date.atTime(8, 0);
        LocalDateTime istDayEnd = date.atTime(20, 0);

        Instant utcDayStart = istDayStart.atZone(IST_ZONE).toInstant();
        Instant utcDayEnd = istDayEnd.atZone(IST_ZONE).toInstant();

        // If searching for today, skip past time by starting from now rounded up to next 15 minutes
        if (date.isEqual(todayIst)) {
            ZonedDateTime nowIst = Instant.now().atZone(IST_ZONE);
            LocalDateTime roundedNow = roundUpToNext15(nowIst.toLocalDateTime());
            if (roundedNow.isAfter(istDayEnd)) {
                throw new InvalidBookingTimeException("No available slots remain for today");
            }
            utcDayStart = roundedNow.atZone(IST_ZONE).toInstant();
        }

        List<Booking> activeBookings =
                bookingRepository.findActiveBookingsForRange(
                        resourceId, utcDayStart, utcDayEnd);

        List<BestSlotDTO> result = new ArrayList<>();

        Instant candidateStart = utcDayStart;

        while (!candidateStart.plusSeconds(durationMinutes * 60L)
                .isAfter(utcDayEnd)) {

            Instant candidateEnd =
                    candidateStart.plusSeconds(durationMinutes * 60L);

            Instant bufferedCandidateEnd =
                    candidateEnd.plusSeconds(BUFFER_MINUTES * 60L);

            Instant finalCandidateStart = candidateStart;
            boolean conflict = activeBookings.stream().anyMatch(b ->
                    !(b.getEndUtc().plusSeconds(BUFFER_MINUTES * 60L)
                            .isBefore(finalCandidateStart)
                            || b.getStartUtc().isAfter(bufferedCandidateEnd))
            );

            if (!conflict) {
                BestSlotDTO dto = new BestSlotDTO();
                dto.setStartUtc(candidateStart);
                dto.setEndUtc(candidateEnd);
                result.add(dto);

                if (result.size() == 5) {
                    break;
                }
            }

            candidateStart = candidateStart.plusSeconds(15 * 60L);
        }

        logger.info("Returning {} best slots for resource {}",
                result.size(), resourceId);

        return result;
    }

    private LocalDate parseRequestedDate(String istDate) {
        try {
            return LocalDate.parse(istDate, REQUEST_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(istDate);
            } catch (DateTimeParseException ignored) {
                throw new InvalidBookingTimeException("Date must be in dd-MM-yyyy format");
            }
        }
    }

    private LocalDateTime roundUpToNext15(LocalDateTime time) {
        int minute = time.getMinute();
        int remainder = minute % 15;
        if (remainder == 0 && time.getSecond() == 0 && time.getNano() == 0) {
            return time;
        }
        int minutesToAdd = 15 - remainder;
        return time.withSecond(0).withNano(0).plusMinutes(minutesToAdd);
    }
}

