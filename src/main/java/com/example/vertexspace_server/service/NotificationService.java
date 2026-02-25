package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.NotificationDTO;
import com.example.vertexspace_server.model.NotificationType;
import com.example.vertexspace_server.model.UserAccount;
import java.util.List;

public interface NotificationService {
    void sendNotification(UserAccount user,
                          String message,
                          NotificationType type);
    List<NotificationDTO> getCurrentUserNotifications();
}
