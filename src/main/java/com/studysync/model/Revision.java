package com.studysync.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "revisions")
public class Revision {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String learningPathId;
    private String learningPathTitle;
    private String topicId;
    private String topicTitle;
    private int revisionNumber; // 1, 2, 3, 4

    @Indexed
    private LocalDate scheduledDate;
    
    // Status: Pending, Completed, Missed, Rescheduled
    @Indexed
    private String status = "Pending";

    private LocalDateTime completedAt;
    private String notes;
    private LocalDateTime createdAt;

    public Revision() {
        this.createdAt = LocalDateTime.now();
    }

    public Revision(String userId, String learningPathId, String learningPathTitle, String topicId, String topicTitle, int revisionNumber, LocalDate scheduledDate) {
        this.userId = userId;
        this.learningPathId = learningPathId;
        this.learningPathTitle = learningPathTitle;
        this.topicId = topicId;
        this.topicTitle = topicTitle;
        this.revisionNumber = revisionNumber;
        this.scheduledDate = scheduledDate;
        this.status = "Pending";
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLearningPathId() { return learningPathId; }
    public void setLearningPathId(String learningPathId) { this.learningPathId = learningPathId; }

    public String getLearningPathTitle() { return learningPathTitle; }
    public void setLearningPathTitle(String learningPathTitle) { this.learningPathTitle = learningPathTitle; }

    public String getTopicId() { return topicId; }
    public void setTopicId(String topicId) { this.topicId = topicId; }

    public String getTopicTitle() { return topicTitle; }
    public void setTopicTitle(String topicTitle) { this.topicTitle = topicTitle; }

    public int getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(int revisionNumber) { this.revisionNumber = revisionNumber; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

