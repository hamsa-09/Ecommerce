package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
}