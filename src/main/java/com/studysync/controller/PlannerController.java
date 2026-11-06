package com.studysync.controller;

import com.studysync.dto.ScheduleSuggestionDTO;
import com.studysync.model.Task;
import com.studysync.service.PlannerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planner")
@CrossOrigin(origins = "*")
public class PlannerController {

    private final PlannerService plannerService;

    public PlannerController(PlannerService plannerService) {
        this.plannerService = plannerService;
    }

    @GetMapping("/suggest")
    public ResponseEntity<ScheduleSuggestionDTO> getSuggestion(
            @RequestParam(defaultValue = "user-sainath") String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(plannerService.generateDailyPlan(userId, date));
    }

    @PostMapping("/apply")
    public ResponseEntity<List<Task>> applyPlan(
            @RequestParam(defaultValue = "user-sainath") String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody List<ScheduleSuggestionDTO.SuggestedSlot> slots) {
        if (date == null) {
            date = LocalDate.now();
        }
        return ResponseEntity.ok(plannerService.applyPlan(userId, date, slots));
    }
}

