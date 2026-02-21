package com.example.bingo.repository;

import com.example.bingo.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findByCreatorId(String creatorId);

    void deleteByCreatorId(String creatorId);
}
