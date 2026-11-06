package com.studysync.dto;

import java.util.Map;
import java.util.List;

public class AnalyticsDTO {
    private int currentStreak;
    private int totalStudyHours;
    private int totalSessions;
    private double weeklyCompletionRate;
    private double monthlyProductivity;
    private double revisionConsistencyRate;

    // Charts data
    private List<String> dailyLabels;
    private List<Long> dailyCompletedData;
    private List<Long> dailyCreatedData;

    private List<String> weeklyTrendLabels;
    private List<Double> weeklyTrendData;

    private Map<String, Integer> subjectStudyMinutes;
    private Map<String, Long> taskStatusCounts;

    public AnalyticsDTO() {}

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getTotalStudyHours() { return totalStudyHours; }
    public void setTotalStudyHours(int totalStudyHours) { this.totalStudyHours = totalStudyHours; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public double getWeeklyCompletionRate() { return weeklyCompletionRate; }
    public void setWeeklyCompletionRate(double weeklyCompletionRate) { this.weeklyCompletionRate = weeklyCompletionRate; }

    public double getMonthlyProductivity() { return monthlyProductivity; }
    public void setMonthlyProductivity(double monthlyProductivity) { this.monthlyProductivity = monthlyProductivity; }

    public double getRevisionConsistencyRate() { return revisionConsistencyRate; }
    public void setRevisionConsistencyRate(double revisionConsistencyRate) { this.revisionConsistencyRate = revisionConsistencyRate; }

    public List<String> getDailyLabels() { return dailyLabels; }
    public void setDailyLabels(List<String> dailyLabels) { this.dailyLabels = dailyLabels; }

    public List<Long> getDailyCompletedData() { return dailyCompletedData; }
    public void setDailyCompletedData(List<Long> dailyCompletedData) { this.dailyCompletedData = dailyCompletedData; }

    public List<Long> getDailyCreatedData() { return dailyCreatedData; }
    public void setDailyCreatedData(List<Long> dailyCreatedData) { this.dailyCreatedData = dailyCreatedData; }

    public List<String> getWeeklyTrendLabels() { return weeklyTrendLabels; }
    public void setWeeklyTrendLabels(List<String> weeklyTrendLabels) { this.weeklyTrendLabels = weeklyTrendLabels; }

    public List<Double> getWeeklyTrendData() { return weeklyTrendData; }
    public void setWeeklyTrendData(List<Double> weeklyTrendData) { this.weeklyTrendData = weeklyTrendData; }

    public Map<String, Integer> getSubjectStudyMinutes() { return subjectStudyMinutes; }
    public void setSubjectStudyMinutes(Map<String, Integer> subjectStudyMinutes) { this.subjectStudyMinutes = subjectStudyMinutes; }

    public Map<String, Long> getTaskStatusCounts() { return taskStatusCounts; }
    public void setTaskStatusCounts(Map<String, Long> taskStatusCounts) { this.taskStatusCounts = taskStatusCounts; }
}

