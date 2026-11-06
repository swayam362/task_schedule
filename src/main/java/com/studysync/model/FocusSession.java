package com.studysync.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "focus_sessions")
public class FocusSession {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String taskId;
    private String taskTitle;
    private String category;
    private int durationMinutes;
    private String sessionType = "Focus"; // Focus, ShortBreak, LongBreak
    private LocalDateTime completedAt;

    public FocusSession() {
        this.completedAt = LocalDateTime.now();
    }

    public FocusSession(String userId, String taskId, String taskTitle, String category, int durationMinutes, String sessionType) {
        this.userId = userId;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.sessionType = sessionType;
        this.completedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}

