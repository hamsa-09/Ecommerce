package com.example.vertexspace_server.repository;

import com.example.vertexspace_server.model.WaitlistOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitlistOfferRepository extends JpaRepository<WaitlistOffer, Long> {
}
