package com.example.bingo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "topics")
@Data
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer difficulty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User creator;

    @Column(columnDefinition = "TEXT")
    private String description;
}
