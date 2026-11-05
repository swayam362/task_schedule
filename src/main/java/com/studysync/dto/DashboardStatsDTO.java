package com.studysync.dto;

import com.studysync.model.Task;
import com.studysync.model.Revision;
import com.studysync.model.Exam;
import com.studysync.model.Assignment;

import java.util.List;

public class DashboardStatsDTO {
    private long totalTasksToday;
    private long completedTasksToday;
    private long pendingTasksToday;
    private long overdueTasksToday;
    private long highPriorityTasksToday;
    private double dailyCompletionPercentage;
    private double weeklyProductivitySummary; // weekly completion rate
    private int currentStreak;
    private int totalStudyMinutesToday;

    private List<Task> todayFocusTasks;
    private List<Task> todayTimetable;
    private List<Revision> upcomingRevisions;
    private List<Exam> upcomingExams;
    private List<Assignment> upcomingAssignments;

    public DashboardStatsDTO() {}

    public long getTotalTasksToday() { return totalTasksToday; }
    public void setTotalTasksToday(long totalTasksToday) { this.totalTasksToday = totalTasksToday; }

    public long getCompletedTasksToday() { return completedTasksToday; }
    public void setCompletedTasksToday(long completedTasksToday) { this.completedTasksToday = completedTasksToday; }

    public long getPendingTasksToday() { return pendingTasksToday; }
    public void setPendingTasksToday(long pendingTasksToday) { this.pendingTasksToday = pendingTasksToday; }

    public long getOverdueTasksToday() { return overdueTasksToday; }
    public void setOverdueTasksToday(long overdueTasksToday) { this.overdueTasksToday = overdueTasksToday; }

    public long getHighPriorityTasksToday() { return highPriorityTasksToday; }
    public void setHighPriorityTasksToday(long highPriorityTasksToday) { this.highPriorityTasksToday = highPriorityTasksToday; }

    public double getDailyCompletionPercentage() { return dailyCompletionPercentage; }
    public void setDailyCompletionPercentage(double dailyCompletionPercentage) { this.dailyCompletionPercentage = dailyCompletionPercentage; }

    public double getWeeklyProductivitySummary() { return weeklyProductivitySummary; }
    public void setWeeklyProductivitySummary(double weeklyProductivitySummary) { this.weeklyProductivitySummary = weeklyProductivitySummary; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getTotalStudyMinutesToday() { return totalStudyMinutesToday; }
    public void setTotalStudyMinutesToday(int totalStudyMinutesToday) { this.totalStudyMinutesToday = totalStudyMinutesToday; }

    public List<Task> getTodayFocusTasks() { return todayFocusTasks; }
    public void setTodayFocusTasks(List<Task> todayFocusTasks) { this.todayFocusTasks = todayFocusTasks; }

    public List<Task> getTodayTimetable() { return todayTimetable; }
    public void setTodayTimetable(List<Task> todayTimetable) { this.todayTimetable = todayTimetable; }

    public List<Revision> getUpcomingRevisions() { return upcomingRevisions; }
    public void setUpcomingRevisions(List<Revision> upcomingRevisions) { this.upcomingRevisions = upcomingRevisions; }

    public List<Exam> getUpcomingExams() { return upcomingExams; }
    public void setUpcomingExams(List<Exam> upcomingExams) { this.upcomingExams = upcomingExams; }

    public List<Assignment> getUpcomingAssignments() { return upcomingAssignments; }
    public void setUpcomingAssignments(List<Assignment> upcomingAssignments) { this.upcomingAssignments = upcomingAssignments; }
}
