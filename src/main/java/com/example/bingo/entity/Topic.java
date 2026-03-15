package com.example.bingo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ビンゴゲームの各マスに配置されるお題（トピック）の情報を表すエンティティクラス。
 */
@Entity
@Table(name = "topics")
@Data
public class Topic {

    /** 一意なID */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** お題のテキスト内容 */
    @Column(nullable = false, length = 500)
    private String content;

    /** 金額や回数など、このお題に関連する数値 */
    @Column(nullable = false)
    private Integer amount;

    /** 難易度（例: 1=簡単 〜 5=難しい） */
    @Column(nullable = false)
    private Integer difficulty;

    /** このお題を作成したユーザー */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User creator;

    /** お題に関する詳細な説明文 */
    @Column(columnDefinition = "TEXT")
    private String description;
}
