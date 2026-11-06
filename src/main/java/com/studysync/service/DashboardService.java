package com.studysync.service;

import com.studysync.dto.DashboardStatsDTO;
import com.studysync.model.*;
import com.studysync.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TaskRepository taskRepository;
    private final RevisionRepository revisionRepository;
    private final ExamRepository examRepository;
    private final AssignmentRepository assignmentRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final TaskService taskService;
    private final AnalyticsService analyticsService;

    public DashboardService(TaskRepository taskRepository,
                            RevisionRepository revisionRepository,
                            ExamRepository examRepository,
                            AssignmentRepository assignmentRepository,
                            FocusSessionRepository focusSessionRepository,
                            TaskService taskService,
                            AnalyticsService analyticsService) {
        this.taskRepository = taskRepository;
        this.revisionRepository = revisionRepository;
        this.examRepository = examRepository;
        this.assignmentRepository = assignmentRepository;
        this.focusSessionRepository = focusSessionRepository;
        this.taskService = taskService;
        this.analyticsService = analyticsService;
    }

    public DashboardStatsDTO getDashboardStats(String userId) {
        DashboardStatsDTO dto = new DashboardStatsDTO();
        LocalDate today = LocalDate.now();

        // 1. Fetch today's tasks
        List<Task> todayTasks = taskService.getTasks(userId, today, null, null, null, null);
        long totalToday = todayTasks.size();
        long completedToday = todayTasks.stream().filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count();
        long pendingToday = todayTasks.stream().filter(t -> "Pending".equalsIgnoreCase(t.getStatus()) || "In Progress".equalsIgnoreCase(t.getStatus())).count();
        long overdueToday = todayTasks.stream().filter(t -> "Missed".equalsIgnoreCase(t.getStatus())).count();
        long highPriorityToday = todayTasks.stream().filter(t -> "High".equalsIgnoreCase(t.getPriority()) && !"Completed".equalsIgnoreCase(t.getStatus())).count();

        dto.setTotalTasksToday(totalToday);
        dto.setCompletedTasksToday(completedToday);
        dto.setPendingTasksToday(pendingToday);
        dto.setOverdueTasksToday(overdueToday);
        dto.setHighPriorityTasksToday(highPriorityToday);

        double dailyPct = totalToday == 0 ? 0.0 : ((double) completedToday / totalToday) * 100.0;
        dto.setDailyCompletionPercentage(Math.round(dailyPct * 10.0) / 10.0);

        // 2. Weekly productivity
        var analytics = analyticsService.getAnalytics(userId);
        dto.setWeeklyProductivitySummary(analytics.getWeeklyCompletionRate());
        dto.setCurrentStreak(analytics.getCurrentStreak());

        // 3. Study minutes today
        List<FocusSession> todaySessions = focusSessionRepository.findByUserId(userId).stream()
                .filter(s -> s.getCompletedAt() != null && s.getCompletedAt().toLocalDate().equals(today))
                .collect(Collectors.toList());
        int sessionMinutesToday = todaySessions.stream().mapToInt(FocusSession::getDurationMinutes).sum();
        int taskMinutesToday = todayTasks.stream()
                .filter(t -> "Completed".equalsIgnoreCase(t.getStatus()) && t.getEstimatedDuration() != null)
                .mapToInt(Task::getEstimatedDuration)
                .sum();
        dto.setTotalStudyMinutesToday(sessionMinutesToday + taskMinutesToday);

        // 4. Focus, timetable, revisions, exams, assignments
        dto.setTodayFocusTasks(taskService.getTodayFocusTasks(userId));
        dto.setTodayTimetable(taskService.getTodayTimetable(userId));

        List<Revision> upcomingRevs = revisionRepository.findByUserIdAndScheduledDateBetween(userId, today, today.plusDays(7));
        dto.setUpcomingRevisions(upcomingRevs.stream().filter(r -> !"Completed".equalsIgnoreCase(r.getStatus())).limit(5).collect(Collectors.toList()));

        List<Exam> upcomingExams = examRepository.findByUserIdAndExamDateGreaterThanEqualOrderByExamDateAsc(userId, today);
        dto.setUpcomingExams(upcomingExams.stream().limit(4).collect(Collectors.toList()));

        List<Assignment> upcomingAssignments = assignmentRepository.findByUserIdAndSubmissionDeadlineGreaterThanEqualOrderBySubmissionDeadlineAsc(userId, today);
        dto.setUpcomingAssignments(upcomingAssignments.stream().filter(a -> !"Submitted".equalsIgnoreCase(a.getStatus())).limit(4).collect(Collectors.toList()));

        return dto;
    }
}

