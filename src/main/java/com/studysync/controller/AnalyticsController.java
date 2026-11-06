package com.studysync.controller;

import com.studysync.dto.AnalyticsDTO;
import com.studysync.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<AnalyticsDTO> getAnalytics(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(analyticsService.getAnalytics(userId));
    }
}

