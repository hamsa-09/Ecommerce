package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.NotificationDTO;
import com.example.vertexspace_server.dto.SuccessResponse;
import com.example.vertexspace_server.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<List<NotificationDTO>>> getMyNotifications() {
        return ResponseEntity.ok(new SuccessResponse<>(notificationService.getCurrentUserNotifications()));
    }
}

