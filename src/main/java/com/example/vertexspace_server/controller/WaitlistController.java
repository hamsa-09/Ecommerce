package com.example.vertexspace_server.controller;

import com.example.vertexspace_server.dto.*;
import com.example.vertexspace_server.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping("/join")
    public ResponseEntity<SuccessResponse<WaitlistEntryDTO>> joinWaitlist(@RequestBody WaitlistJoinDTO waitlistJoinDTO) {
        return ResponseEntity.ok(new SuccessResponse<>( waitlistService.joinWaitlist(waitlistJoinDTO)));
    }

    @DeleteMapping("/leave/{id}")
    public ResponseEntity<SuccessResponse<String>> leaveWaitlist(@PathVariable Long id) {
        waitlistService.leaveWaitlist(id);
        return ResponseEntity.ok(new SuccessResponse<>("Left waitlist successfully"));
    }

    @GetMapping("/status/{resourceName}/start/{startUtc}/end/{endUtc}")
    public ResponseEntity<SuccessResponse<WaitlistStatusDTO>> getWaitlistStatus(
            @PathVariable String resourceName,
            @PathVariable String startUtc,
            @PathVariable String endUtc) {

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        waitlistService.getWaitlistStatus(resourceName,Instant.parse(startUtc),Instant.parse( endUtc))
                )
        );
    }

    @PatchMapping("/offer/accept/{entryId}")
    public  ResponseEntity<SuccessResponse<String>> acceptOffer(@PathVariable Long entryId) {
        return ResponseEntity.ok(new SuccessResponse<>(waitlistService.acceptOffer(entryId)));
    }

}
