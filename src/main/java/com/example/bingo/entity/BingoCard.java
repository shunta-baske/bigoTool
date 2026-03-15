package com.example.bingo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ビンゴカード情報を表すエンティティクラス。
 */
@Entity
@Table(name = "bingo_cards")
@Data
public class BingoCard {

    /** 一意なID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ビンゴカードに紐づくユーザー
    /** ビンゴカードを作成・所有するユーザー */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User user;

    // 生成された25個のお題の配置（カンマ区切り文字列やJSONなどで保存。ここではカンマ区切り文字列を想定）
    /** 生成された25個のお題の配置（カンマ区切り文字列） */
    @Column(name = "topics", length = 1000)
    private String topics;

    // 各マスの穴あけ状態（例: booleanのカンマ区切り "true,false,true..."）
    /** 各マスの穴あけ状態 ("true,false,true..." のようなカンマ区切り) */
    @Column(name = "punch_status", length = 200)
    private String punchStatus;
}
