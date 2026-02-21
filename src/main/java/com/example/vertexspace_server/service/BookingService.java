package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.BookingRequestDTO;
import com.example.vertexspace_server.dto.BookingResponseDTO;
import java.time.Instant;
import java.util.List;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO bookingRequestDTO);
    void cancelBooking(Long id);
    List<BookingResponseDTO> listBookingsByUser();
    List<BookingResponseDTO> listBookingsByResource(Long resourceId, String startUtc, String endUtc);
    List<BookingResponseDTO> listBookingsByDateRange(String startUtc, String endUtc);
}
