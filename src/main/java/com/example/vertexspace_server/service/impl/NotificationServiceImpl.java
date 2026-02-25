package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.dto.NotificationDTO;
import com.example.vertexspace_server.model.Notification;
import com.example.vertexspace_server.model.NotificationType;
import com.example.vertexspace_server.model.UserAccount;
import com.example.vertexspace_server.repository.NotificationRepository;
import com.example.vertexspace_server.security.JwtUtil;
import com.example.vertexspace_server.service.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final SimpUserRegistry simpUserRegistry;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(
            SimpUserRegistry simpUserRegistry, NotificationRepository notificationRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.simpUserRegistry = simpUserRegistry;

        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendNotification(UserAccount user,
                                 String message,
                                 NotificationType type) {

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);

        notificationRepository.save(notification);
        System.out.println("Sending to user: " + user.getUsername());
        System.out.println("Connected users: " + simpUserRegistry.getUsers());
        // Real-time push
        messagingTemplate.convertAndSendToUser(
                user.getUsername(),     // must match Principal name
                "/queue/notifications",
                message
        );
    }

    @Override
    public List<NotificationDTO> getCurrentUserNotifications() {
        UserAccount currentUser = JwtUtil.getCurrentUser();
        return notificationRepository.findByUserIdOrdered(currentUser.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType() != null ? notification.getType().name() : null);
        dto.setReadStatus(notification.isReadStatus());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
