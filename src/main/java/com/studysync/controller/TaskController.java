package com.studysync.controller;

import com.studysync.dto.TaskConflictResponse;
import com.studysync.model.Task;
import com.studysync.service.TaskService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(
            @RequestParam(defaultValue = "user-sainath") String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(taskService.getTasks(userId, date, category, priority, status, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping("/check-conflict")
    public ResponseEntity<TaskConflictResponse> checkConflict(@RequestBody Task task) {
        if (task.getUserId() == null) {
            task.setUserId("user-sainath");
        }
        return ResponseEntity.ok(taskService.checkConflict(task));
    }

    @PostMapping
    public ResponseEntity<?> createTask(
            @RequestBody Task task,
            @RequestParam(defaultValue = "false") boolean forceOverride) {
        if (task.getUserId() == null) {
            task.setUserId("user-sainath");
        }
        try {
            Task created = taskService.createTask(task, forceOverride);
            return ResponseEntity.ok(created);
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("CONFLICT:")) {
                return ResponseEntity.status(409).body(Map.of("conflict", true, "message", e.getMessage().substring(9)));
            }
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable String id,
            @RequestBody Task task,
            @RequestParam(defaultValue = "false") boolean forceOverride) {
        if (task.getUserId() == null) {
            task.setUserId("user-sainath");
        }
        try {
            Task updated = taskService.updateTask(id, task, forceOverride);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("CONFLICT:")) {
                return ResponseEntity.status(409).body(Map.of("conflict", true, "message", e.getMessage().substring(9)));
            }
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Task> markCompleted(@PathVariable String id) {
        return ResponseEntity.ok(taskService.markCompleted(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> changeStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(taskService.changeStatus(id, status));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<Task> rescheduleTask(@PathVariable String id, @RequestBody Map<String, String> body) {
        LocalDate newDate = LocalDate.parse(body.get("date"));
        LocalTime newStartTime = body.containsKey("startTime") && body.get("startTime") != null && !body.get("startTime").isBlank() ?
                LocalTime.parse(body.get("startTime")) : null;
        LocalTime newEndTime = body.containsKey("endTime") && body.get("endTime") != null && !body.get("endTime").isBlank() ?
                LocalTime.parse(body.get("endTime")) : null;
        return ResponseEntity.ok(taskService.rescheduleTask(id, newDate, newStartTime, newEndTime));
    }

    @GetMapping("/missed")
    public ResponseEntity<List<Task>> getMissedTasks(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(taskService.getMissedTasks(userId));
    }

    @GetMapping("/today-focus")
    public ResponseEntity<List<Task>> getTodayFocus(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(taskService.getTodayFocusTasks(userId));
    }

    @GetMapping("/today-timetable")
    public ResponseEntity<List<Task>> getTodayTimetable(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(taskService.getTodayTimetable(userId));
    }
}
