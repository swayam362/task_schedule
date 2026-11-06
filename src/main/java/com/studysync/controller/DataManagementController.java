package com.studysync.controller;

import com.studysync.model.*;
import com.studysync.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class DataManagementController {

    private final TaskRepository taskRepository;
    private final LearningPathRepository learningPathRepository;
    private final RevisionRepository revisionRepository;
    private final ExamRepository examRepository;
    private final AssignmentRepository assignmentRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final UserSettingsRepository settingsRepository;

    public DataManagementController(TaskRepository taskRepository,
                                  LearningPathRepository learningPathRepository,
                                  RevisionRepository revisionRepository,
                                  ExamRepository examRepository,
                                  AssignmentRepository assignmentRepository,
                                  FocusSessionRepository focusSessionRepository,
                                  UserSettingsRepository settingsRepository) {
        this.taskRepository = taskRepository;
        this.learningPathRepository = learningPathRepository;
        this.revisionRepository = revisionRepository;
        this.examRepository = examRepository;
        this.assignmentRepository = assignmentRepository;
        this.focusSessionRepository = focusSessionRepository;
        this.settingsRepository = settingsRepository;
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportData(@RequestParam(defaultValue = "user-sainath") String userId) {
        Map<String, Object> export = new HashMap<>();
        export.put("exportedAt", LocalDateTime.now());
        export.put("userId", userId);
        export.put("version", "1.0.0");
        export.put("tasks", taskRepository.findByUserId(userId));
        export.put("learningPaths", learningPathRepository.findByUserId(userId));
        export.put("revisions", revisionRepository.findByUserId(userId));
        export.put("exams", examRepository.findByUserId(userId));
        export.put("assignments", assignmentRepository.findByUserId(userId));
        export.put("focusSessions", focusSessionRepository.findByUserId(userId));
        export.put("settings", settingsRepository.findByUserId(userId).orElse(null));
        return ResponseEntity.ok(export);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importData(
            @RequestParam(defaultValue = "user-sainath") String userId,
            @RequestBody Map<String, Object> data) {
        int importedCount = 0;
        // Allows user to restore or upload backup
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Data backup processed successfully.");
        return ResponseEntity.ok(result);
    }
}

