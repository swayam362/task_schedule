package com.studysync.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ScheduleSuggestionDTO {
    public static class SuggestedSlot {
        private String title;
        private String category;
        private String taskType;
        private LocalTime startTime;
        private LocalTime endTime;
        private int durationMinutes;
        private String priority;
        private String reason;
        private String existingTaskId; // if mapped to existing task

        public SuggestedSlot() {}

        public SuggestedSlot(String title, String category, String taskType, LocalTime startTime, LocalTime endTime, int durationMinutes, String priority, String reason, String existingTaskId) {
            this.title = title;
            this.category = category;
            this.taskType = taskType;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMinutes = durationMinutes;
            this.priority = priority;
            this.reason = reason;
            this.existingTaskId = existingTaskId;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getExistingTaskId() { return existingTaskId; }
        public void setExistingTaskId(String existingTaskId) { this.existingTaskId = existingTaskId; }
    }

    private LocalDate date;
    private int totalAvailableHours;
    private String strategySummary;
    private List<SuggestedSlot> slots;

    public ScheduleSuggestionDTO() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getTotalAvailableHours() { return totalAvailableHours; }
    public void setTotalAvailableHours(int totalAvailableHours) { this.totalAvailableHours = totalAvailableHours; }

    public String getStrategySummary() { return strategySummary; }
    public void setStrategySummary(String strategySummary) { this.strategySummary = strategySummary; }

    public List<SuggestedSlot> getSlots() { return slots; }
    public void setSlots(List<SuggestedSlot> slots) { this.slots = slots; }
}
