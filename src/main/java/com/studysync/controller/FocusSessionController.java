package com.studysync.controller;

import com.studysync.model.FocusSession;
import com.studysync.service.FocusSessionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/focus-sessions")
@CrossOrigin(origins = "*")
public class FocusSessionController {

    private final FocusSessionService focusSessionService;

    public FocusSessionController(FocusSessionService focusSessionService) {
        this.focusSessionService = focusSessionService;
    }

    @PostMapping
    public ResponseEntity<FocusSession> recordSession(@RequestBody FocusSession session) {
        if (session.getUserId() == null) {
            session.setUserId("user-sainath");
        }
        return ResponseEntity.ok(focusSessionService.recordSession(session));
    }

    @GetMapping
    public ResponseEntity<List<FocusSession>> getSessions(
            @RequestParam(defaultValue = "user-sainath") String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(focusSessionService.getSessions(userId, start, end));
    }
}
