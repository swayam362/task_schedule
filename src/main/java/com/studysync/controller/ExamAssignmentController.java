package com.studysync.controller;

import com.studysync.model.Assignment;
import com.studysync.model.Exam;
import com.studysync.model.Task;
import com.studysync.service.ExamAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/academic")
@CrossOrigin(origins = "*")
public class ExamAssignmentController {

    private final ExamAssignmentService examAssignmentService;

    public ExamAssignmentController(ExamAssignmentService examAssignmentService) {
        this.examAssignmentService = examAssignmentService;
    }

    // Exams
    @GetMapping("/exams")
    public ResponseEntity<List<Exam>> getAllExams(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(examAssignmentService.getAllExams(userId));
    }

    @GetMapping("/exams/upcoming")
    public ResponseEntity<List<Exam>> getUpcomingExams(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(examAssignmentService.getUpcomingExams(userId));
    }

    @PostMapping("/exams")
    public ResponseEntity<Exam> createExam(@RequestBody Exam exam) {
        if (exam.getUserId() == null) {
            exam.setUserId("user-sainath");
        }
        return ResponseEntity.ok(examAssignmentService.createExam(exam));
    }

    @PutMapping("/exams/{id}")
    public ResponseEntity<Exam> updateExam(@PathVariable String id, @RequestBody Exam exam) {
        return ResponseEntity.ok(examAssignmentService.updateExam(id, exam));
    }

    @DeleteMapping("/exams/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable String id) {
        examAssignmentService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exams/{id}/generate-tasks")
    public ResponseEntity<List<Task>> generateExamTasks(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, Integer> body) {
        int days = body != null && body.containsKey("daysBefore") ? body.get("daysBefore") : 3;
        int hours = body != null && body.containsKey("hoursPerDay") ? body.get("hoursPerDay") : 2;
        return ResponseEntity.ok(examAssignmentService.generatePrepTasksForExam(id, days, hours));
    }

    // Assignments
    @GetMapping("/assignments")
    public ResponseEntity<List<Assignment>> getAllAssignments(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(examAssignmentService.getAllAssignments(userId));
    }

    @GetMapping("/assignments/upcoming")
    public ResponseEntity<List<Assignment>> getUpcomingAssignments(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(examAssignmentService.getUpcomingAssignments(userId));
    }

    @PostMapping("/assignments")
    public ResponseEntity<Assignment> createAssignment(@RequestBody Assignment assignment) {
        if (assignment.getUserId() == null) {
            assignment.setUserId("user-sainath");
        }
        return ResponseEntity.ok(examAssignmentService.createAssignment(assignment));
    }

    @PutMapping("/assignments/{id}")
    public ResponseEntity<Assignment> updateAssignment(@PathVariable String id, @RequestBody Assignment assignment) {
        return ResponseEntity.ok(examAssignmentService.updateAssignment(id, assignment));
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable String id) {
        examAssignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assignments/{id}/generate-task")
    public ResponseEntity<Task> generateAssignmentTask(@PathVariable String id) {
        return ResponseEntity.ok(examAssignmentService.generateTaskForAssignment(id));
    }
}

