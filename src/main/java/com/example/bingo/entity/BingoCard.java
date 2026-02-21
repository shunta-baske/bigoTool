package com.example.bingo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "bingo_cards")
@Data
public class BingoCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ビンゴカードに紐づくユーザー
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User user;

    // 生成された25個のお題の配置（カンマ区切り文字列やJSONなどで保存。ここではカンマ区切り文字列を想定）
    @Column(name = "topics", length = 1000)
    private String topics;

    // 各マスの穴あけ状態（例: booleanのカンマ区切り "true,false,true..."）
    @Column(name = "punch_status", length = 200)
    private String punchStatus;
}
