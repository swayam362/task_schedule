package com.studysync.controller;

import com.studysync.service.AIAdvisorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIAdvisorController {

    private final AIAdvisorService aiAdvisorService;

    public AIAdvisorController(AIAdvisorService aiAdvisorService) {
        this.aiAdvisorService = aiAdvisorService;
    }

    @GetMapping("/analysis")
    public ResponseEntity<Map<String, Object>> getAnalysis(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(aiAdvisorService.getWorkloadAnalysis(userId));
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askAdvisor(
            @RequestParam(defaultValue = "user-sainath") String userId,
            @RequestBody Map<String, String> request) {
        String prompt = request.getOrDefault("prompt", "");
        return ResponseEntity.ok(aiAdvisorService.askAdvisor(userId, prompt));
    }
}
