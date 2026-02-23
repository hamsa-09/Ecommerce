package com.example.vertexspace_server.service.impl;

import com.example.vertexspace_server.model.Notification;
import com.example.vertexspace_server.model.NotificationType;
import com.example.vertexspace_server.model.UserAccount;
import com.example.vertexspace_server.repository.NotificationRepository;
import com.example.vertexspace_server.service.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

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
}
