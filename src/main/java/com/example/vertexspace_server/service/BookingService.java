package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.BookingListResponseDTO;
import com.example.vertexspace_server.dto.BookingRequestDTO;
import com.example.vertexspace_server.dto.BookingResponseDTO;
import com.example.vertexspace_server.model.Booking;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponseDTO createBooking(BookingRequestDTO bookingRequestDTO);
    String cancelBooking(Long id);
    String cancelSeries(Long bookingId);
    BookingListResponseDTO listBookingsByUser();
    BookingListResponseDTO listBookingsByDateRange(String startUtc, String endUtc);
    List<BookingResponseDTO> listBookingsByResource(Long resourceId, String startUtc, String endUtc);
}
