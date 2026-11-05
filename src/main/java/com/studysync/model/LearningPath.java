package com.studysync.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "learning_paths")
public class LearningPath {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private String description;
    private String category; // DSA, Java Backend, Python, AI/ML, SQL and DBMS, Other
    private String icon;
    private String color; // emerald, blue, purple, amber, cyan

    private List<Subject> subjects = new ArrayList<>();

    // Resume Where You Left Off fields
    private String lastStudiedTopicId;
    private String lastStudiedTopicTitle;
    private LocalDateTime lastStudiedAt;
    private String currentTopicId;
    private String currentTopicTitle;
    private String nextTopicId;
    private String nextTopicTitle;
    private String notes; // Saved course-level or last topic notes

    private int totalTopics = 0;
    private int completedTopics = 0;
    private double progressPercentage = 0.0;
    private int totalStudyMinutes = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LearningPath() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public LearningPath(String userId, String title, String description, String category, String icon, String color) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.icon = icon;
        this.color = color;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public List<Subject> getSubjects() { return subjects; }
    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }

    public String getLastStudiedTopicId() { return lastStudiedTopicId; }
    public void setLastStudiedTopicId(String lastStudiedTopicId) { this.lastStudiedTopicId = lastStudiedTopicId; }

    public String getLastStudiedTopicTitle() { return lastStudiedTopicTitle; }
    public void setLastStudiedTopicTitle(String lastStudiedTopicTitle) { this.lastStudiedTopicTitle = lastStudiedTopicTitle; }

    public LocalDateTime getLastStudiedAt() { return lastStudiedAt; }
    public void setLastStudiedAt(LocalDateTime lastStudiedAt) { this.lastStudiedAt = lastStudiedAt; }

    public String getCurrentTopicId() { return currentTopicId; }
    public void setCurrentTopicId(String currentTopicId) { this.currentTopicId = currentTopicId; }

    public String getCurrentTopicTitle() { return currentTopicTitle; }
    public void setCurrentTopicTitle(String currentTopicTitle) { this.currentTopicTitle = currentTopicTitle; }

    public String getNextTopicId() { return nextTopicId; }
    public void setNextTopicId(String nextTopicId) { this.nextTopicId = nextTopicId; }

    public String getNextTopicTitle() { return nextTopicTitle; }
    public void setNextTopicTitle(String nextTopicTitle) { this.nextTopicTitle = nextTopicTitle; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getTotalTopics() { return totalTopics; }
    public void setTotalTopics(int totalTopics) { this.totalTopics = totalTopics; }

    public int getCompletedTopics() { return completedTopics; }
    public void setCompletedTopics(int completedTopics) { this.completedTopics = completedTopics; }

    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

    public int getTotalStudyMinutes() { return totalStudyMinutes; }
    public void setTotalStudyMinutes(int totalStudyMinutes) { this.totalStudyMinutes = totalStudyMinutes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
