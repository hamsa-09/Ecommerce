package com.example.vertexspace_server.service;

import com.example.vertexspace_server.dto.BestSlotDTO;
import java.util.List;

public interface BestSlotService {
    List<BestSlotDTO> findBestSlots(Long resourceId, String istDate, int durationMinutes);
}
