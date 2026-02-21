package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.BookingRequestDTO;
import com.example.vertexspace_server.dto.BookingResponseDTO;
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
    @Autowired
    private BookingService bookingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody BookingRequestDTO bookingRequestDTO) {
        return ResponseEntity.ok(bookingService.createBooking(bookingRequestDTO));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponseDTO>> listBookingsByUser() {
        return ResponseEntity.ok(bookingService.listBookingsByUser());
    }

    @GetMapping("/resource/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponseDTO>> listBookingsByResource(
            @PathVariable Long resourceId,
            @RequestParam String startUtc,
            @RequestParam String endUtc) {
        return ResponseEntity.ok(bookingService.listBookingsByResource(resourceId, startUtc, endUtc));
    }

    @GetMapping("/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponseDTO>> listBookingsByDateRange(
            @RequestParam String startUtc,
            @RequestParam String endUtc) {
        return ResponseEntity.ok(bookingService.listBookingsByDateRange(startUtc, endUtc));
    }
}
