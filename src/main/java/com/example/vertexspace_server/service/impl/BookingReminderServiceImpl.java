package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.model.Booking;
import com.example.vertexspace_server.model.NotificationType;
import com.example.vertexspace_server.repository.BookingRepository;
import com.example.vertexspace_server.service.BookingService;
import com.example.vertexspace_server.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class BookingReminderServiceImpl  {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public BookingReminderServiceImpl(BookingRepository bookingRepository, NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void sendReminders() {

        Instant now = Instant.now();
        Instant nextMinute = now.plusSeconds(60);

        List<Booking> upcomingBookings =
                bookingRepository.findBookingsStartingBetween(
                        now,
                        nextMinute
                );

        for (Booking booking : upcomingBookings) {

            notificationService.sendNotification(
                    booking.getUser(),
                    "Reminder: Your booking starts now.",
                    NotificationType.BOOKING_REMINDER
            );
        }
    }
}