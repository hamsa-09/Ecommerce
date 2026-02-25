package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.WaitlistEntryDTO;
import com.example.vertexspace_server.dto.WaitlistJoinDTO;
import com.example.vertexspace_server.dto.WaitlistOfferDTO;
import com.example.vertexspace_server.dto.WaitlistStatusDTO;

import java.time.Instant;
import java.util.List;

public interface WaitlistService {
    WaitlistEntryDTO joinWaitlist(WaitlistJoinDTO waitlistJoinDTO);
    void leaveWaitlist(Long waitlistEntryId);
    WaitlistStatusDTO getWaitlistStatus(String resourceName, Instant startUtc, Instant endUtc);
    String acceptOffer(Long waitlistEntryId);
    void createOfferForWaitlist(Long resourceId, Instant slotStart, Instant slotEnd);

}
