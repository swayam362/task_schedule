package com.studysync.service;

import com.studysync.exception.ResourceNotFoundException;
import com.studysync.model.*;
import com.studysync.repository.LearningPathRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class LearningService {

    private final LearningPathRepository pathRepository;

    public LearningService(LearningPathRepository pathRepository) {
        this.pathRepository = pathRepository;
    }

    public List<LearningPath> getAllPaths(String userId) {
        return pathRepository.findByUserId(userId);
    }

    public LearningPath getPathById(String id) {
        return pathRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found with id: " + id));
    }

    public LearningPath createPath(LearningPath path) {
        if (path.getSubjects() == null) {
            path.setSubjects(new ArrayList<>());
        }
        recalculateStats(path);
        path.setCreatedAt(LocalDateTime.now());
        path.setUpdatedAt(LocalDateTime.now());
        return pathRepository.save(path);
    }

    public LearningPath updatePath(String id, LearningPath updated) {
        LearningPath path = getPathById(id);
        path.setTitle(updated.getTitle());
        path.setDescription(updated.getDescription());
        path.setCategory(updated.getCategory());
        path.setIcon(updated.getIcon());
        path.setColor(updated.getColor());
        if (updated.getNotes() != null) {
            path.setNotes(updated.getNotes());
        }
        path.setUpdatedAt(LocalDateTime.now());
        return pathRepository.save(path);
    }

    public void deletePath(String id) {
        if (!pathRepository.existsById(id)) {
            throw new ResourceNotFoundException("Learning path not found with id: " + id);
        }
        pathRepository.deleteById(id);
    }

    public void recalculateStats(LearningPath path) {
        int total = 0;
        int completed = 0;
        int totalMinutes = 0;
        List<Topic> allTopics = new ArrayList<>();

        if (path.getSubjects() != null) {
            for (Subject s : path.getSubjects()) {
                if (s.getChapters() != null) {
                    for (Chapter c : s.getChapters()) {
                        if (c.getTopics() != null) {
                            for (Topic t : c.getTopics()) {
                                total++;
                                totalMinutes += t.getStudyMinutes();
                                allTopics.add(t);
                                if ("Completed".equalsIgnoreCase(t.getStatus())) {
                                    completed++;
                                }
                            }
                        }
                    }
                }
            }
        }

        path.setTotalTopics(total);
        path.setCompletedTopics(completed);
        path.setTotalStudyMinutes(totalMinutes);
        if (total > 0) {
            double pct = ((double) completed / total) * 100.0;
            path.setProgressPercentage(Math.round(pct * 10.0) / 10.0);
        } else {
            path.setProgressPercentage(0.0);
        }

        // Determine current, last studied, and next topic if not set
        if (path.getCurrentTopicId() == null || path.getNextTopicId() == null) {
            for (int i = 0; i < allTopics.size(); i++) {
                Topic t = allTopics.get(i);
                if ("In Progress".equalsIgnoreCase(t.getStatus())) {
                    path.setCurrentTopicId(t.getId());
                    path.setCurrentTopicTitle(t.getTitle());
                    if (i + 1 < allTopics.size()) {
                        path.setNextTopicId(allTopics.get(i + 1).getId());
                        path.setNextTopicTitle(allTopics.get(i + 1).getTitle());
                    }
                    break;
                } else if ("Pending".equalsIgnoreCase(t.getStatus()) && path.getCurrentTopicId() == null) {
                    path.setCurrentTopicId(t.getId());
                    path.setCurrentTopicTitle(t.getTitle());
                    if (i + 1 < allTopics.size()) {
                        path.setNextTopicId(allTopics.get(i + 1).getId());
                        path.setNextTopicTitle(allTopics.get(i + 1).getTitle());
                    }
                    break;
                }
            }
        }
    }

    public LearningPath updateTopicStatus(String pathId, String topicId, String status, String notes, Integer studyMinutes) {
        LearningPath path = getPathById(pathId);
        Topic targetTopic = null;
        List<Topic> flatTopics = new ArrayList<>();

        for (Subject s : path.getSubjects()) {
            if (s.getChapters() != null) {
                for (Chapter c : s.getChapters()) {
                    if (c.getTopics() != null) {
                        for (Topic t : c.getTopics()) {
                            flatTopics.add(t);
                            if (topicId.equals(t.getId())) {
                                targetTopic = t;
                            }
                        }
                    }
                }
            }
        }

        if (targetTopic == null) {
            throw new ResourceNotFoundException("Topic not found with id: " + topicId);
        }

        targetTopic.setStatus(status);
        if (notes != null && !notes.isBlank()) {
            targetTopic.setNotes(notes);
            path.setNotes(notes);
        }
        if (studyMinutes != null && studyMinutes > 0) {
            targetTopic.setStudyMinutes(targetTopic.getStudyMinutes() + studyMinutes);
        }
        targetTopic.setLastStudiedAt(LocalDateTime.now());
        path.setLastStudiedAt(LocalDateTime.now());

        int currentIndex = flatTopics.indexOf(targetTopic);

        if ("Completed".equalsIgnoreCase(status)) {
            path.setLastStudiedTopicId(targetTopic.getId());
            path.setLastStudiedTopicTitle(targetTopic.getTitle());

            // Next topic is the first topic after this that is not completed
            Topic nextTopic = null;
            for (int i = currentIndex + 1; i < flatTopics.size(); i++) {
                if (!"Completed".equalsIgnoreCase(flatTopics.get(i).getStatus())) {
                    nextTopic = flatTopics.get(i);
                    break;
                }
            }
            if (nextTopic != null) {
                path.setNextTopicId(nextTopic.getId());
                path.setNextTopicTitle(nextTopic.getTitle());
                path.setCurrentTopicId(nextTopic.getId());
                path.setCurrentTopicTitle(nextTopic.getTitle());
            } else {
                path.setNextTopicId(null);
                path.setNextTopicTitle("All topics completed!");
            }
        } else if ("In Progress".equalsIgnoreCase(status)) {
            path.setCurrentTopicId(targetTopic.getId());
            path.setCurrentTopicTitle(targetTopic.getTitle());
            if (currentIndex + 1 < flatTopics.size()) {
                path.setNextTopicId(flatTopics.get(currentIndex + 1).getId());
                path.setNextTopicTitle(flatTopics.get(currentIndex + 1).getTitle());
            }
        }

        recalculateStats(path);
        path.setUpdatedAt(LocalDateTime.now());
        return pathRepository.save(path);
    }

    public LearningPath addSubject(String pathId, Subject subject) {
        LearningPath path = getPathById(pathId);
        if (subject.getId() == null || subject.getId().isBlank()) {
            subject.setId(UUID.randomUUID().toString());
        }
        if (subject.getChapters() == null) {
            subject.setChapters(new ArrayList<>());
        }
        path.getSubjects().add(subject);
        recalculateStats(path);
        path.setUpdatedAt(LocalDateTime.now());
        return pathRepository.save(path);
    }

    public LearningPath addChapter(String pathId, String subjectId, Chapter chapter) {
        LearningPath path = getPathById(pathId);
        Subject subject = path.getSubjects().stream()
                .filter(s -> s.getId().equals(subjectId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));

        if (chapter.getId() == null || chapter.getId().isBlank()) {
            chapter.setId(UUID.randomUUID().toString());
        }
        if (chapter.getTopics() == null) {
            chapter.setTopics(new ArrayList<>());
        }
        subject.getChapters().add(chapter);
        recalculateStats(path);
        path.setUpdatedAt(LocalDateTime.now());
        return pathRepository.save(path);
    }

    public LearningPath addTopic(String pathId, String subjectId, String chapterId, Topic topic) {
        LearningPath path = getPathById(pathId);
        Subject subject = path.getSubjects().stream()
                .filter(s -> s.getId().equals(subjectId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + subjectId));

        Chapter chapter = subject.getChapters().stream()
                .filter(c -> c.getId().equals(chapterId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found: " + chapterId));

        if (topic.getId() == null || topic.getId().isBlank()) {
            topic.setId(UUID.randomUUID().toString());
        }
        if (topic.getStatus() == null || topic.getStatus().isBlank()) {
            topic.setStatus("Pending");
        }
        chapter.getTopics().add(topic);
        recalculateStats(path);
        path.setUpdatedAt(LocalDateTime.now());
        return pathRepository.save(path);
    }

    public LearningPath deleteTopic(String pathId, String topicId) {
        LearningPath path = getPathById(pathId);
        for (Subject s : path.getSubjects()) {
            if (s.getChapters() != null) {
                for (Chapter c : s.getChapters()) {
                    if (c.getTopics() != null) {
                        c.getTopics().removeIf(t -> t.getId().equals(topicId));
                    }
                }
            }
        }
        recalculateStats(path);
        path.setUpdatedAt(LocalDateTime.now());
        return pathRepository.save(path);
    }
}
