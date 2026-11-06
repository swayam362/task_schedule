package com.studysync.service;

import com.studysync.exception.ResourceNotFoundException;
import com.studysync.model.Assignment;
import com.studysync.model.Exam;
import com.studysync.model.Task;
import com.studysync.repository.AssignmentRepository;
import com.studysync.repository.ExamRepository;
import com.studysync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExamAssignmentService {

    private final ExamRepository examRepository;
    private final AssignmentRepository assignmentRepository;
    private final TaskRepository taskRepository;

    public ExamAssignmentService(ExamRepository examRepository, AssignmentRepository assignmentRepository, TaskRepository taskRepository) {
        this.examRepository = examRepository;
        this.assignmentRepository = assignmentRepository;
        this.taskRepository = taskRepository;
    }

    // Exam operations
    public List<Exam> getAllExams(String userId) {
        return examRepository.findByUserId(userId);
    }

    public List<Exam> getUpcomingExams(String userId) {
        return examRepository.findByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, LocalDate.now());
    }

    public Exam createExam(Exam exam) {
        exam.setCreatedAt(LocalDateTime.now());
        if (exam.getPreparationStatus() == null) {
            exam.setPreparationStatus("Not Started");
        }
        return examRepository.save(exam);
    }

    public Exam updateExam(String id, Exam updated) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        exam.setName(updated.getName());
        exam.setSubject(updated.getSubject());
        exam.setExamDate(updated.getExamDate());
        exam.setExamTime(updated.getExamTime());
        exam.setSyllabus(updated.getSyllabus());
        exam.setPreparationStatus(updated.getPreparationStatus());
        exam.setPriority(updated.getPriority());
        exam.setTargetScore(updated.getTargetScore());
        exam.setNotes(updated.getNotes());
        return examRepository.save(exam);
    }

    public void deleteExam(String id) {
        if (!examRepository.existsById(id)) {
            throw new ResourceNotFoundException("Exam not found with id: " + id);
        }
        examRepository.deleteById(id);
    }

    public List<Task> generatePrepTasksForExam(String examId, int daysBefore, int hoursPerDay) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        List<Task> prepTasks = new ArrayList<>();
        LocalDate examDate = exam.getExamDate();
        if (examDate == null) return prepTasks;

        LocalDate startDate = examDate.minusDays(Math.max(1, daysBefore));
        if (startDate.isBefore(LocalDate.now())) {
            startDate = LocalDate.now();
        }

        LocalDate cur = startDate;
        int dayCount = 1;
        while (!cur.isAfter(examDate.minusDays(1))) {
            Task task = new Task();
            task.setUserId(exam.getUserId());
            task.setTitle("Exam Prep: " + exam.getName() + " (Day " + dayCount + ")");
            task.setDescription("Targeted revision of syllabus: " + (exam.getSyllabus() != null ? exam.getSyllabus() : exam.getSubject()));
            task.setCategory("Exam");
            task.setTaskType("Learning");
            task.setDate(cur);
            task.setStartTime(LocalTime.of(17, 0));
            task.setEndTime(LocalTime.of(17 + Math.min(4, hoursPerDay), 0));
            task.setEstimatedDuration(hoursPerDay * 60);
            task.setPriority("High");
            task.setReminderMinutesBefore(15);
            task.setLinkedEntityId(exam.getId());
            task.setLinkedEntityType("Exam");
            task.setStatus("Pending");
            prepTasks.add(taskRepository.save(task));

            cur = cur.plusDays(1);
            dayCount++;
        }

        exam.setPreparationStatus("In Progress");
        examRepository.save(exam);

        return prepTasks;
    }

    // Assignment operations
    public List<Assignment> getAllAssignments(String userId) {
        return assignmentRepository.findByUserId(userId);
    }

    public List<Assignment> getUpcomingAssignments(String userId) {
        return assignmentRepository.findByUserIdAndSubmissionDeadlineGreaterThanEqualOrderBySubmissionDeadlineAsc(userId, LocalDate.now());
    }

    public Assignment createAssignment(Assignment assignment) {
        assignment.setCreatedAt(LocalDateTime.now());
        if (assignment.getStatus() == null) {
            assignment.setStatus("Pending");
        }
        return assignmentRepository.save(assignment);
    }

    public Assignment updateAssignment(String id, Assignment updated) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        assignment.setTitle(updated.getTitle());
        assignment.setSubject(updated.getSubject());
        assignment.setSubmissionDeadline(updated.getSubmissionDeadline());
        assignment.setSubmissionTime(updated.getSubmissionTime());
        assignment.setDetails(updated.getDetails());
        assignment.setPracticalDetails(updated.getPracticalDetails());
        assignment.setStatus(updated.getStatus());
        assignment.setPriority(updated.getPriority());
        assignment.setType(updated.getType());
        if ("Submitted".equalsIgnoreCase(updated.getStatus())) {
            assignment.setSubmittedAt(LocalDateTime.now());
        }
        return assignmentRepository.save(assignment);
    }

    public void deleteAssignment(String id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    public Task generateTaskForAssignment(String assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        Task task = new Task();
        task.setUserId(assignment.getUserId());
        task.setTitle("Work on " + assignment.getType() + ": " + assignment.getTitle());
        task.setDescription("Deadline: " + assignment.getSubmissionDeadline() + ". " + (assignment.getDetails() != null ? assignment.getDetails() : ""));
        task.setCategory(assignment.getType() != null && assignment.getType().equalsIgnoreCase("Practical") ? "Practical" : "Assignment");
        task.setTaskType("Assignment");
        task.setDate(assignment.getSubmissionDeadline().isAfter(LocalDate.now()) ? assignment.getSubmissionDeadline().minusDays(1) : LocalDate.now());
        task.setStartTime(LocalTime.of(15, 0));
        task.setEndTime(LocalTime.of(16, 30));
        task.setEstimatedDuration(90);
        task.setPriority(assignment.getPriority());
        task.setReminderMinutesBefore(15);
        task.setLinkedEntityId(assignment.getId());
        task.setLinkedEntityType("Assignment");
        task.setStatus("Pending");
        return taskRepository.save(task);
    }
}

