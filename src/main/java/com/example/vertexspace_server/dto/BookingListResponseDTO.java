package com.example.vertexspace_server.dto;

import java.util.List;

public class BookingListResponseDTO {
    private List<BookingResponseDTO> currentUserBookings;
    private List<BookingResponseDTO> otherUserBookings;

    public List<BookingResponseDTO> getCurrentUserBookings() {
        return currentUserBookings;
    }

    public void setCurrentUserBookings(List<BookingResponseDTO> currentUserBookings) {
        this.currentUserBookings = currentUserBookings;
    }

    public List<BookingResponseDTO> getOtherUserBookings() {
        return otherUserBookings;
    }

    public void setOtherUserBookings(List<BookingResponseDTO> otherUserBookings) {
        this.otherUserBookings = otherUserBookings;
    }
}

