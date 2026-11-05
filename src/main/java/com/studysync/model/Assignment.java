package com.studysync.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Document(collection = "assignments")
public class Assignment {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private String subject;

    @Indexed
    private LocalDate submissionDeadline;
    private LocalTime submissionTime;

    private String details;
    private String practicalDetails;

    // Status: Pending, In Progress, Submitted
    @Indexed
    private String status = "Pending";

    // Priority: High, Medium, Low
    private String priority = "Medium";

    // Type: Assignment, Practical
    private String type = "Assignment";

    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;

    public Assignment() {
        this.createdAt = LocalDateTime.now();
    }

    public Assignment(String userId, String title, String subject, LocalDate submissionDeadline, String type) {
        this.userId = userId;
        this.title = title;
        this.subject = subject;
        this.submissionDeadline = submissionDeadline;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public LocalDate getSubmissionDeadline() { return submissionDeadline; }
    public void setSubmissionDeadline(LocalDate submissionDeadline) { this.submissionDeadline = submissionDeadline; }

    public LocalTime getSubmissionTime() { return submissionTime; }
    public void setSubmissionTime(LocalTime submissionTime) { this.submissionTime = submissionTime; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getPracticalDetails() { return practicalDetails; }
    public void setPracticalDetails(String practicalDetails) { this.practicalDetails = practicalDetails; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
