package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface BookingRepository extends JpaRepository<Booking, Long> {
}
