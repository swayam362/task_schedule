package com.studysync.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String email;
    private String fullName;
    private String courseDegree;
    private LocalDateTime createdAt;

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String id, String username, String email, String fullName, String courseDegree) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.courseDegree = courseDegree;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCourseDegree() { return courseDegree; }
    public void setCourseDegree(String courseDegree) { this.courseDegree = courseDegree; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
