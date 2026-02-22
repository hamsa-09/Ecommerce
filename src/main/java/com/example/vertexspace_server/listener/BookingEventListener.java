package com.example.vertexspace_server.listener;

import com.example.vertexspace_server.events.BookingCancelledEvent;
import com.example.vertexspace_server.service.WaitlistService;
import com.example.vertexspace_server.service.impl.BookingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;



@Component
public class BookingEventListener {
    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);

    private final WaitlistService waitlistService;

    public BookingEventListener(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @Async
    @EventListener
    @Transactional
    public void handleBookingCancelled(BookingCancelledEvent event) {
        logger.info("Received BookingCancelledEvent for resourceId={}, start={}, end={}",
                event.getResourceId(), event.getStartUtc(), event.getEndUtc());
        waitlistService.createOfferForWaitlist(
                event.getResourceId(),
                event.getStartUtc(),
                event.getEndUtc()
        );
    }
}