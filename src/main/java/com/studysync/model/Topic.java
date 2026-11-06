package com.studysync.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Topic {
    private String id;
    private String title;
    private String description;
    private String notes;
    // Status: Pending, In Progress, Completed
    private String status = "Pending";
    private int studyMinutes = 0;
    private LocalDateTime lastStudiedAt;
    private int orderIndex = 0;
    private List<Subtopic> subtopics = new ArrayList<>();

    public Topic() {}

    public Topic(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = "Pending";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getStudyMinutes() { return studyMinutes; }
    public void setStudyMinutes(int studyMinutes) { this.studyMinutes = studyMinutes; }

    public LocalDateTime getLastStudiedAt() { return lastStudiedAt; }
    public void setLastStudiedAt(LocalDateTime lastStudiedAt) { this.lastStudiedAt = lastStudiedAt; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public List<Subtopic> getSubtopics() { return subtopics; }
    public void setSubtopics(List<Subtopic> subtopics) { this.subtopics = subtopics; }
}

