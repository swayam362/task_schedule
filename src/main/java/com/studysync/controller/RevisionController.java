package com.studysync.controller;

import com.studysync.model.Revision;
import com.studysync.service.RevisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/revisions")
@CrossOrigin(origins = "*")
public class RevisionController {

    private final RevisionService revisionService;

    public RevisionController(RevisionService revisionService) {
        this.revisionService = revisionService;
    }

    @GetMapping
    public ResponseEntity<List<Revision>> getRevisions(
            @RequestParam(defaultValue = "user-sainath") String userId,
            @RequestParam(defaultValue = "all") String filter) {
        return ResponseEntity.ok(revisionService.getRevisions(userId, filter));
    }

    @PostMapping
    public ResponseEntity<Revision> createRevision(@RequestBody Revision revision) {
        if (revision.getUserId() == null) {
            revision.setUserId("user-sainath");
        }
        return ResponseEntity.ok(revisionService.createRevision(revision));
    }

    @PostMapping("/schedule-topic")
    public ResponseEntity<List<Revision>> scheduleTopicRevisions(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "user-sainath");
        String pathId = (String) body.get("pathId");
        String pathTitle = (String) body.get("pathTitle");
        String topicId = (String) body.get("topicId");
        String topicTitle = (String) body.get("topicTitle");
        
        @SuppressWarnings("unchecked")
        List<Integer> intervals = (List<Integer>) body.get("intervals");
        boolean createTasks = Boolean.TRUE.equals(body.get("createTasks"));

        List<Revision> created = revisionService.scheduleRevisionsForTopic(userId, pathId, pathTitle, topicId, topicTitle, intervals, createTasks);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Revision> markCompleted(@PathVariable String id) {
        return ResponseEntity.ok(revisionService.markCompleted(id));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<Revision> reschedule(@PathVariable String id, @RequestBody Map<String, String> body) {
        LocalDate newDate = LocalDate.parse(body.get("date"));
        return ResponseEntity.ok(revisionService.reschedule(id, newDate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRevision(@PathVariable String id) {
        revisionService.deleteRevision(id);
        return ResponseEntity.noContent().build();
    }
}

