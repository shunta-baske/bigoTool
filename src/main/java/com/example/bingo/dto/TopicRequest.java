package com.example.bingo.dto;

import lombok.Data;

@Data
public class TopicRequest {
    private String content;
    private Integer amount;
    private Integer difficulty;
    private String description;
    private String creatorId; // UUID string representation
}
