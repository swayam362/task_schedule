package com.studysync.controller;

import com.studysync.dto.DashboardStatsDTO;
import com.studysync.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(dashboardService.getDashboardStats(userId));
    }
}
