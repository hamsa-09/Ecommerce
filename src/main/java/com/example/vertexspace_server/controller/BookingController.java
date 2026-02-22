package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.BookingRequestDTO;
import com.example.vertexspace_server.dto.BookingResponseDTO;
import com.example.vertexspace_server.dto.SuccessResponse;
import com.example.vertexspace_server.model.Booking;
import com.example.vertexspace_server.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<BookingResponseDTO>> createBooking(@RequestBody BookingRequestDTO bookingRequestDTO) {
        return ResponseEntity.ok(new SuccessResponse<>(bookingService.createBooking(bookingRequestDTO)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<String>> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse<>(bookingService.cancelBooking(id)));
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<List<BookingResponseDTO>>> listBookingsByUser() {
        return ResponseEntity.ok(new SuccessResponse<>(bookingService.listBookingsByUser()));
    }

    @GetMapping("/resource/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity< SuccessResponse<List<BookingResponseDTO>>> listBookingsByResource(
            @PathVariable Long resourceId,
            @RequestParam String startUtc,
            @RequestParam String endUtc) {
        return ResponseEntity.ok(new SuccessResponse<>(bookingService.listBookingsByResource(resourceId, startUtc, endUtc)));
    }

    @GetMapping("/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<List<BookingResponseDTO>>> listBookingsByDateRange(
            @RequestParam String startUtc,
            @RequestParam String endUtc) {
        return ResponseEntity.ok(new SuccessResponse<>(bookingService.listBookingsByDateRange(startUtc, endUtc)));
    }
    @PatchMapping("/{id}/cancel-series")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<String>> cancelSeries(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse<>(bookingService.cancelSeries(id)));
    }
}
