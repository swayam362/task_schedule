package com.studysync.service;

import com.studysync.dto.ScheduleSuggestionDTO;
import com.studysync.model.*;
import com.studysync.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlannerService {

    private final TaskRepository taskRepository;
    private final RevisionRepository revisionRepository;
    private final ExamRepository examRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserSettingsRepository settingsRepository;

    public PlannerService(TaskRepository taskRepository,
                          RevisionRepository revisionRepository,
                          ExamRepository examRepository,
                          AssignmentRepository assignmentRepository,
                          UserSettingsRepository settingsRepository) {
        this.taskRepository = taskRepository;
        this.revisionRepository = revisionRepository;
        this.examRepository = examRepository;
        this.assignmentRepository = assignmentRepository;
        this.settingsRepository = settingsRepository;
    }

    public ScheduleSuggestionDTO generateDailyPlan(String userId, LocalDate targetDate) {
        if (targetDate == null) {
            targetDate = LocalDate.now();
        }

        UserSettings settings = settingsRepository.findByUserId(userId)
                .orElse(new UserSettings(userId));

        int availableHours = settings.getDailyStudyHours() > 0 ? settings.getDailyStudyHours() : 6;
        String preferredTime = settings.getPreferredStudyTime() != null ? settings.getPreferredStudyTime() : "Morning";

        LocalTime blockStart;
        switch (preferredTime) {
            case "Afternoon":
                blockStart = LocalTime.of(13, 0);
                break;
            case "Evening":
                blockStart = LocalTime.of(17, 0);
                break;
            case "Night":
                blockStart = LocalTime.of(20, 0);
                break;
            case "Morning":
            default:
                blockStart = LocalTime.of(8, 30);
                break;
        }

        List<ScheduleSuggestionDTO.SuggestedSlot> slots = new ArrayList<>();
        LocalTime currentSlotTime = blockStart;

        // 1. Check for missed tasks from previous days
        List<Task> allUserTasks = taskRepository.findByUserId(userId);
        LocalDate finalTargetDate = targetDate;
        List<Task> missedTasks = allUserTasks.stream()
                .filter(t -> "Missed".equalsIgnoreCase(t.getStatus()) || 
                             ("Pending".equalsIgnoreCase(t.getStatus()) && t.getDate() != null && t.getDate().isBefore(finalTargetDate)))
                .toList();

        if (!missedTasks.isEmpty()) {
            Task highPriorityMissed = missedTasks.stream()
                    .filter(t -> "High".equalsIgnoreCase(t.getPriority()))
                    .findFirst()
                    .orElse(missedTasks.get(0));

            int dur = highPriorityMissed.getEstimatedDuration() != null ? highPriorityMissed.getEstimatedDuration() : 60;
            LocalTime end = currentSlotTime.plusMinutes(dur);
            slots.add(new ScheduleSuggestionDTO.SuggestedSlot(
                    "Recovery: " + highPriorityMissed.getTitle(),
                    highPriorityMissed.getCategory() != null ? highPriorityMissed.getCategory() : "Other",
                    highPriorityMissed.getTaskType() != null ? highPriorityMissed.getTaskType() : "Learning",
                    currentSlotTime,
                    end,
                    dur,
                    "High",
                    "Overdue task requiring immediate attention to avoid learning backlog.",
                    highPriorityMissed.getId()
            ));
            currentSlotTime = end.plusMinutes(10); // buffer
        }

        // 2. Check for upcoming exams in next 7 days
        List<Exam> upcomingExams = examRepository.findByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, targetDate);
        if (!upcomingExams.isEmpty()) {
            Exam nextExam = upcomingExams.get(0);
            long daysToExam = java.time.temporal.ChronoUnit.DAYS.between(targetDate, nextExam.getExamDate());
            if (daysToExam <= 7) {
                int dur = 90;
                LocalTime end = currentSlotTime.plusMinutes(dur);
                slots.add(new ScheduleSuggestionDTO.SuggestedSlot(
                        "Exam Prep: " + nextExam.getName() + " (" + nextExam.getSubject() + ")",
                        "Exam",
                        "Learning",
                        currentSlotTime,
                        end,
                        dur,
                        "High",
                        "Critical exam in " + daysToExam + " day(s). Focus on: " + (nextExam.getSyllabus() != null ? nextExam.getSyllabus() : "Core syllabus"),
                        null
                ));
                currentSlotTime = end.plusMinutes(15);
            }
        }

        // 3. Spaced Repetition Revisions scheduled for today
        List<Revision> todayRevisions = revisionRepository.findByUserIdAndScheduledDate(userId, targetDate);
        for (Revision rev : todayRevisions) {
            if (!"Completed".equalsIgnoreCase(rev.getStatus())) {
                int dur = 45;
                LocalTime end = currentSlotTime.plusMinutes(dur);
                slots.add(new ScheduleSuggestionDTO.SuggestedSlot(
                        "Spaced Revision: " + rev.getTopicTitle(),
                        rev.getLearningPathTitle() != null && rev.getLearningPathTitle().contains("DSA") ? "DSA" : "Java Backend",
                        "Revision",
                        currentSlotTime,
                        end,
                        dur,
                        "Medium",
                        "Revision #" + rev.getRevisionNumber() + " scheduled via spaced repetition to solidify memory.",
                        null
                ));
                currentSlotTime = end.plusMinutes(10);
            }
        }

        // 4. Assignments approaching deadline
        List<Assignment> upcomingAssignments = assignmentRepository.findByUserIdAndSubmissionDeadlineGreaterThanEqualOrderBySubmissionDeadlineAsc(userId, targetDate);
        for (Assignment assign : upcomingAssignments) {
            if (!"Submitted".equalsIgnoreCase(assign.getStatus())) {
                long daysToDeadline = java.time.temporal.ChronoUnit.DAYS.between(targetDate, assign.getSubmissionDeadline());
                if (daysToDeadline <= 3) {
                    int dur = 60;
                    LocalTime end = currentSlotTime.plusMinutes(dur);
                    slots.add(new ScheduleSuggestionDTO.SuggestedSlot(
                            "Assignment: " + assign.getTitle(),
                            assign.getType() != null ? assign.getType() : "Assignment",
                            "Assignment",
                            currentSlotTime,
                            end,
                            dur,
                            "High",
                            "Submission deadline is approaching in " + daysToDeadline + " day(s)!",
                            null
                    ));
                    currentSlotTime = end.plusMinutes(15);
                    break;
                }
            }
        }

        // 5. Existing pending tasks for target date
        List<Task> todayExistingTasks = taskRepository.findByUserIdAndDate(userId, targetDate);
        for (Task t : todayExistingTasks) {
            if (!"Completed".equalsIgnoreCase(t.getStatus())) {
                boolean alreadyIncluded = slots.stream().anyMatch(s -> t.getId() != null && t.getId().equals(s.getExistingTaskId()));
                if (!alreadyIncluded) {
                    int dur = t.getEstimatedDuration() != null ? t.getEstimatedDuration() : 60;
                    LocalTime start = t.getStartTime() != null ? t.getStartTime() : currentSlotTime;
                    LocalTime end = t.getEndTime() != null ? t.getEndTime() : start.plusMinutes(dur);
                    slots.add(new ScheduleSuggestionDTO.SuggestedSlot(
                            t.getTitle(),
                            t.getCategory() != null ? t.getCategory() : "Other",
                            t.getTaskType() != null ? t.getTaskType() : "Learning",
                            start,
                            end,
                            dur,
                            t.getPriority() != null ? t.getPriority() : "Medium",
                            "Scheduled task on your calendar.",
                            t.getId()
                    ));
                    if (end.isAfter(currentSlotTime)) {
                        currentSlotTime = end.plusMinutes(10);
                    }
                }
            }
        }

        // If very few slots, suggest a core DSA / Coding session
        if (slots.size() < 2) {
            int dur = 60;
            LocalTime end = currentSlotTime.plusMinutes(dur);
            slots.add(new ScheduleSuggestionDTO.SuggestedSlot(
                    "DSA Practice & LeetCode Problem Solving",
                    "DSA",
                    "Learning",
                    currentSlotTime,
                    end,
                    dur,
                    "High",
                    "Consistent daily practice of data structures & algorithms.",
                    null
            ));
        }

        ScheduleSuggestionDTO suggestion = new ScheduleSuggestionDTO();
        suggestion.setDate(targetDate);
        suggestion.setTotalAvailableHours(availableHours);
        suggestion.setStrategySummary("Engineered using spaced repetition intervals, approaching exam deadlines (" + upcomingExams.size() + " active), pending assignment submissions, and missed task recovery.");
        suggestion.setSlots(slots);
        return suggestion;
    }

    public List<Task> applyPlan(String userId, LocalDate targetDate, List<ScheduleSuggestionDTO.SuggestedSlot> selectedSlots) {
        List<Task> createdTasks = new ArrayList<>();
        for (ScheduleSuggestionDTO.SuggestedSlot slot : selectedSlots) {
            if (slot.getExistingTaskId() != null && !slot.getExistingTaskId().isBlank()) {
                // Update existing task time
                taskRepository.findById(slot.getExistingTaskId()).ifPresent(t -> {
                    t.setDate(targetDate);
                    t.setStartTime(slot.getStartTime());
                    t.setEndTime(slot.getEndTime());
                    t.setStatus("Pending");
                    createdTasks.add(taskRepository.save(t));
                });
            } else {
                // Create new task
                Task t = new Task();
                t.setUserId(userId);
                t.setTitle(slot.getTitle());
                t.setDescription(slot.getReason());
                t.setCategory(slot.getCategory());
                t.setTaskType(slot.getTaskType());
                t.setDate(targetDate);
                t.setStartTime(slot.getStartTime());
                t.setEndTime(slot.getEndTime());
                t.setEstimatedDuration(slot.getDurationMinutes());
                t.setPriority(slot.getPriority());
                t.setStatus("Pending");
                t.setReminderMinutesBefore(10);
                createdTasks.add(taskRepository.save(t));
            }
        }
        return createdTasks;
    }
}

