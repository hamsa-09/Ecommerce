package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.service.DashboardService;
import com.example.vertexspace_server.dto.RecommendationDTO;
import com.example.vertexspace_server.model.Booking;
import com.example.vertexspace_server.model.Resource;
import com.example.vertexspace_server.model.UserAccount;
import com.example.vertexspace_server.repository.BookingRepository;
import com.example.vertexspace_server.repository.ResourceRepository;
import com.example.vertexspace_server.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final BookingRepository bookingRepository;

    public DashboardServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<RecommendationDTO> getTopResources() {

        UserAccount user = JwtUtil.getCurrentUser();

        logger.info("Generating dashboard recommendations for user {}",
                user.getId());

        Instant thirtyDaysAgo =
                Instant.now().minus(30, ChronoUnit.DAYS);

        List<Object[]> results =
                bookingRepository.findTopResourcesLast30Days(
                        user.getId(), thirtyDaysAgo);

        List<RecommendationDTO> response = results.stream()
                .limit(3)
                .map(r -> {
                    RecommendationDTO dto = new RecommendationDTO();
                    dto.setResourceId((Long) r[0]);
                    dto.setResourceName((String) r[1]);
                    dto.setBookingCount(((Long) r[2]).intValue());
                    return dto;
                })
                .toList();

        logger.info("Returning {} recommendations for user {}",
                response.size(), user.getId());

        return response;
    }
}