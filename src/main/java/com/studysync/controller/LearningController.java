package com.studysync.controller;

import com.studysync.model.Chapter;
import com.studysync.model.LearningPath;
import com.studysync.model.Subject;
import com.studysync.model.Topic;
import com.studysync.service.LearningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-paths")
@CrossOrigin(origins = "*")
public class LearningController {

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping
    public ResponseEntity<List<LearningPath>> getAllPaths(@RequestParam(defaultValue = "user-sainath") String userId) {
        return ResponseEntity.ok(learningService.getAllPaths(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningPath> getPathById(@PathVariable String id) {
        return ResponseEntity.ok(learningService.getPathById(id));
    }

    @PostMapping
    public ResponseEntity<LearningPath> createPath(@RequestBody LearningPath path) {
        if (path.getUserId() == null) {
            path.setUserId("user-sainath");
        }
        return ResponseEntity.ok(learningService.createPath(path));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LearningPath> updatePath(@PathVariable String id, @RequestBody LearningPath path) {
        return ResponseEntity.ok(learningService.updatePath(id, path));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePath(@PathVariable String id) {
        learningService.deletePath(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/topics/{topicId}/status")
    public ResponseEntity<LearningPath> updateTopicStatus(
            @PathVariable String id,
            @PathVariable String topicId,
            @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        String notes = (String) body.get("notes");
        Integer studyMinutes = body.get("studyMinutes") != null ? Integer.valueOf(body.get("studyMinutes").toString()) : null;
        return ResponseEntity.ok(learningService.updateTopicStatus(id, topicId, status, notes, studyMinutes));
    }

    @PostMapping("/{id}/subjects")
    public ResponseEntity<LearningPath> addSubject(@PathVariable String id, @RequestBody Subject subject) {
        return ResponseEntity.ok(learningService.addSubject(id, subject));
    }

    @PostMapping("/{id}/subjects/{subjectId}/chapters")
    public ResponseEntity<LearningPath> addChapter(@PathVariable String id, @PathVariable String subjectId, @RequestBody Chapter chapter) {
        return ResponseEntity.ok(learningService.addChapter(id, subjectId, chapter));
    }

    @PostMapping("/{id}/subjects/{subjectId}/chapters/{chapterId}/topics")
    public ResponseEntity<LearningPath> addTopic(
            @PathVariable String id,
            @PathVariable String subjectId,
            @PathVariable String chapterId,
            @RequestBody Topic topic) {
        return ResponseEntity.ok(learningService.addTopic(id, subjectId, chapterId, topic));
    }

    @DeleteMapping("/{id}/topics/{topicId}")
    public ResponseEntity<LearningPath> deleteTopic(@PathVariable String id, @PathVariable String topicId) {
        return ResponseEntity.ok(learningService.deleteTopic(id, topicId));
    }
}
