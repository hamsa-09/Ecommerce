package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
}
