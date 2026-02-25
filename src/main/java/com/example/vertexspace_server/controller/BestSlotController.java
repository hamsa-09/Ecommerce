package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.BestSlotDTO;
import com.example.vertexspace_server.service.BestSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/best-slot")
public class BestSlotController {

    private final BestSlotService bestSlotService;

    public BestSlotController(BestSlotService bestSlotService) {
        this.bestSlotService = bestSlotService;
    }

    @GetMapping("/{resourceId}/istdate/{istDate}/duration/{durationMinutes}")
    public List<BestSlotDTO> findBestSlots(@PathVariable Long resourceId,
                                           @PathVariable String istDate,
                                           @PathVariable int durationMinutes) {
        return bestSlotService.findBestSlots(resourceId, istDate, durationMinutes);
    }
}
