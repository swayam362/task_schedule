package com.studysync.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Document(collection = "exams")
public class Exam {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String name;
    private String subject;

    @Indexed
    private LocalDate examDate;
    private LocalTime examTime;
    
    private String syllabus;
    
    // Status: Not Started, In Progress, Ready
    private String preparationStatus = "Not Started";

    // Priority: High, Medium, Low
    private String priority = "High";

    private String targetScore;
    private String notes;
    private LocalDateTime createdAt;

    public Exam() {
        this.createdAt = LocalDateTime.now();
    }

    public Exam(String userId, String name, String subject, LocalDate examDate, LocalTime examTime, String syllabus) {
        this.userId = userId;
        this.name = name;
        this.subject = subject;
        this.examDate = examDate;
        this.examTime = examTime;
        this.syllabus = syllabus;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public LocalTime getExamTime() { return examTime; }
    public void setExamTime(LocalTime examTime) { this.examTime = examTime; }

    public String getSyllabus() { return syllabus; }
    public void setSyllabus(String syllabus) { this.syllabus = syllabus; }

    public String getPreparationStatus() { return preparationStatus; }
    public void setPreparationStatus(String preparationStatus) { this.preparationStatus = preparationStatus; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getTargetScore() { return targetScore; }
    public void setTargetScore(String targetScore) { this.targetScore = targetScore; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
