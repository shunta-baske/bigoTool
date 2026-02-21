package com.example.bingo.service;

import com.example.bingo.entity.Topic;
import com.example.bingo.entity.User;
import com.example.bingo.repository.TopicRepository;
import com.example.bingo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public TopicService(TopicRepository topicRepository, UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    public Topic createTopic(String content, String creatorId, Integer amount, Integer difficulty) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Topic topic = new Topic();
        topic.setContent(content);
        topic.setAmount(amount != null ? amount : 0);

        // Default difficulty to 3 (normal), or clamp between 1-5
        if (difficulty == null || difficulty < 1 || difficulty > 5) {
            topic.setDifficulty(3);
        } else {
            topic.setDifficulty(difficulty);
        }

        topic.setCreator(creator);

        return topicRepository.save(topic);
    }

    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    public List<Topic> getTopicsByUser(String creatorId) {
        return topicRepository.findByCreatorId(creatorId);
    }

    public Topic updateTopic(UUID topicId, String newContent, Integer newAmount, Integer newDifficulty,
            String requestUserId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!topic.getCreator().getId().equals(requestUserId)) {
            throw new IllegalStateException("User is not the creator of this topic");
        }

        topic.setContent(newContent);
        topic.setAmount(newAmount != null ? newAmount : 0);

        if (newDifficulty == null || newDifficulty < 1 || newDifficulty > 5) {
            topic.setDifficulty(3);
        } else {
            topic.setDifficulty(newDifficulty);
        }

        return topicRepository.save(topic);
    }

    public void deleteTopic(UUID topicId, String requestUserId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!topic.getCreator().getId().equals(requestUserId)) {
            throw new IllegalStateException("User is not the creator of this topic");
        }

        topicRepository.delete(topic);
    }

    public void deleteTopicAsAdmin(UUID topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        topicRepository.delete(topic);
    }
}
