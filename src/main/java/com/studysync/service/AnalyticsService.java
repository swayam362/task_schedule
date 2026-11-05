package com.studysync.service;

import com.studysync.dto.AnalyticsDTO;
import com.studysync.model.FocusSession;
import com.studysync.model.Revision;
import com.studysync.model.Task;
import com.studysync.repository.FocusSessionRepository;
import com.studysync.repository.RevisionRepository;
import com.studysync.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final TaskRepository taskRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final RevisionRepository revisionRepository;

    public AnalyticsService(TaskRepository taskRepository, FocusSessionRepository focusSessionRepository, RevisionRepository revisionRepository) {
        this.taskRepository = taskRepository;
        this.focusSessionRepository = focusSessionRepository;
        this.revisionRepository = revisionRepository;
    }

    public AnalyticsDTO getAnalytics(String userId) {
        AnalyticsDTO dto = new AnalyticsDTO();
        LocalDate today = LocalDate.now();

        List<Task> allTasks = taskRepository.findByUserId(userId);
        List<FocusSession> allSessions = focusSessionRepository.findByUserId(userId);
        List<Revision> allRevisions = revisionRepository.findByUserId(userId);

        // 1. Task status counts
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("Completed", allTasks.stream().filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count());
        statusCounts.put("Pending", allTasks.stream().filter(t -> "Pending".equalsIgnoreCase(t.getStatus())).count());
        statusCounts.put("Missed", allTasks.stream().filter(t -> "Missed".equalsIgnoreCase(t.getStatus())).count());
        statusCounts.put("In Progress", allTasks.stream().filter(t -> "In Progress".equalsIgnoreCase(t.getStatus())).count());
        dto.setTaskStatusCounts(statusCounts);

        // 2. Study Hours & Sessions
        int totalSessionMinutes = allSessions.stream()
                .filter(s -> "Focus".equalsIgnoreCase(s.getSessionType()))
                .mapToInt(FocusSession::getDurationMinutes)
                .sum();
        int completedTaskMinutes = allTasks.stream()
                .filter(t -> "Completed".equalsIgnoreCase(t.getStatus()) && t.getEstimatedDuration() != null)
                .mapToInt(Task::getEstimatedDuration)
                .sum();
        
        int totalStudyMinutes = Math.max(totalSessionMinutes, completedTaskMinutes) + (totalSessionMinutes > 0 && completedTaskMinutes > 0 ? (totalSessionMinutes / 2) : 0);
        dto.setTotalStudyHours(Math.max(1, totalStudyMinutes / 60));
        dto.setTotalSessions(allSessions.size());

        // 3. Weekly completion rate (last 7 days)
        LocalDate oneWeekAgo = today.minusDays(6);
        List<Task> weekTasks = allTasks.stream()
                .filter(t -> t.getDate() != null && !t.getDate().isBefore(oneWeekAgo) && !t.getDate().isAfter(today))
                .collect(Collectors.toList());
        long weekCompleted = weekTasks.stream().filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count();
        double weeklyRate = weekTasks.isEmpty() ? 0.0 : ((double) weekCompleted / weekTasks.size()) * 100.0;
        dto.setWeeklyCompletionRate(Math.round(weeklyRate * 10.0) / 10.0);

        // 4. Monthly productivity
        LocalDate oneMonthAgo = today.minusDays(29);
        List<Task> monthTasks = allTasks.stream()
                .filter(t -> t.getDate() != null && !t.getDate().isBefore(oneMonthAgo) && !t.getDate().isAfter(today))
                .collect(Collectors.toList());
        long monthCompleted = monthTasks.stream().filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count();
        double monthlyRate = monthTasks.isEmpty() ? 0.0 : ((double) monthCompleted / monthTasks.size()) * 100.0;
        dto.setMonthlyProductivity(Math.round(monthlyRate * 10.0) / 10.0);

        // 5. Revision consistency
        long totalDueRevisions = allRevisions.stream().filter(r -> !r.getScheduledDate().isAfter(today)).count();
        long completedRevisions = allRevisions.stream().filter(r -> "Completed".equalsIgnoreCase(r.getStatus())).count();
        double revisionRate = totalDueRevisions == 0 ? 100.0 : ((double) completedRevisions / totalDueRevisions) * 100.0;
        dto.setRevisionConsistencyRate(Math.round(revisionRate * 10.0) / 10.0);

        // 6. Streak calculation (consecutive active days leading up to today or yesterday)
        Set<LocalDate> activeDates = new HashSet<>();
        for (Task t : allTasks) {
            if ("Completed".equalsIgnoreCase(t.getStatus())) {
                if (t.getCompletedAt() != null) {
                    activeDates.add(t.getCompletedAt().toLocalDate());
                } else if (t.getDate() != null) {
                    activeDates.add(t.getDate());
                }
            }
        }
        for (FocusSession s : allSessions) {
            if (s.getCompletedAt() != null) {
                activeDates.add(s.getCompletedAt().toLocalDate());
            }
        }

        int streak = 0;
        LocalDate checkDate = today;
        if (!activeDates.contains(checkDate)) {
            checkDate = today.minusDays(1);
        }
        while (activeDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        dto.setCurrentStreak(streak);

        // 7. Daily completed vs created (last 7 days)
        List<String> dailyLabels = new ArrayList<>();
        List<Long> dailyCompleted = new ArrayList<>();
        List<Long> dailyCreated = new ArrayList<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE (MM/dd)");

        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            dailyLabels.add(d.format(dayFmt));

            long comp = allTasks.stream()
                    .filter(t -> ("Completed".equalsIgnoreCase(t.getStatus())) && 
                                 ((t.getCompletedAt() != null && t.getCompletedAt().toLocalDate().equals(d)) || 
                                  (t.getCompletedAt() == null && d.equals(t.getDate()))))
                    .count();
            dailyCompleted.add(comp);

            long crt = allTasks.stream()
                    .filter(t -> d.equals(t.getDate()))
                    .count();
            dailyCreated.add(crt);
        }
        dto.setDailyLabels(dailyLabels);
        dto.setDailyCompletedData(dailyCompleted);
        dto.setDailyCreatedData(dailyCreated);

        // 8. Weekly trend data (last 4 weeks)
        List<String> weekTrendLabels = new ArrayList<>();
        List<Double> weekTrendData = new ArrayList<>();
        for (int w = 3; w >= 0; w--) {
            LocalDate startW = today.minusDays(w * 7 + 6);
            LocalDate endW = today.minusDays(w * 7);
            String label = "W-" + (w == 0 ? "Current" : w);
            weekTrendLabels.add(label);

            List<Task> wTasks = allTasks.stream()
                    .filter(t -> t.getDate() != null && !t.getDate().isBefore(startW) && !t.getDate().isAfter(endW))
                    .collect(Collectors.toList());
            long wComp = wTasks.stream().filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count();
            double wRate = wTasks.isEmpty() ? 0.0 : ((double) wComp / wTasks.size()) * 100.0;
            weekTrendData.add(Math.round(wRate * 10.0) / 10.0);
        }
        dto.setWeeklyTrendLabels(weekTrendLabels);
        dto.setWeeklyTrendData(weekTrendData);

        // 9. Subject-wise study minutes
        Map<String, Integer> subjectMinutes = new HashMap<>();
        for (Task t : allTasks) {
            String cat = t.getCategory() != null ? t.getCategory() : "Other";
            int dur = t.getEstimatedDuration() != null ? t.getEstimatedDuration() : 45;
            if ("Completed".equalsIgnoreCase(t.getStatus())) {
                subjectMinutes.put(cat, subjectMinutes.getOrDefault(cat, 0) + dur);
            }
        }
        for (FocusSession s : allSessions) {
            String cat = s.getCategory() != null ? s.getCategory() : "Focus";
            subjectMinutes.put(cat, subjectMinutes.getOrDefault(cat, 0) + s.getDurationMinutes());
        }
        if (subjectMinutes.isEmpty()) {
            subjectMinutes.put("DSA", 60);
            subjectMinutes.put("Java Backend", 90);
            subjectMinutes.put("Python", 45);
        }
        dto.setSubjectStudyMinutes(subjectMinutes);

        return dto;
    }
}
