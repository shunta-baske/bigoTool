package com.example.bingo.controller;

import com.example.bingo.dto.TopicRequest;
import com.example.bingo.entity.Topic;
import com.example.bingo.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping
    public ResponseEntity<Topic> createTopic(@RequestBody TopicRequest request) {
        try {
            Topic createdTopic = topicService.createTopic(request.getContent(), request.getDescription(),
                    request.getCreatorId(),
                    request.getAmount(), request.getDifficulty());
            return ResponseEntity.ok(createdTopic);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Topic>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Topic>> getTopicsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(topicService.getTopicsByUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<Topic> searchTopic(@RequestParam String content) {
        return topicService.findTopicByContent(content)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Topic> updateTopic(@PathVariable UUID id, @RequestBody TopicRequest request) {
        try {
            Topic updatedTopic = topicService.updateTopic(id, request.getContent(), request.getDescription(),
                    request.getAmount(),
                    request.getDifficulty(), request.getCreatorId());
            return ResponseEntity.ok(updatedTopic);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable UUID id, @RequestParam String userId) {
        try {
            topicService.deleteTopic(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteTopicAsAdmin(@PathVariable UUID id) {
        try {
            topicService.deleteTopicAsAdmin(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
