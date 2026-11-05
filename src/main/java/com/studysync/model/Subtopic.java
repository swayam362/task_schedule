package com.studysync.model;

public class Subtopic {
    private String id;
    private String title;
    private boolean completed;

    public Subtopic() {}

    public Subtopic(String id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
