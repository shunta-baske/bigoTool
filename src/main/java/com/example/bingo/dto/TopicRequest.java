package com.example.bingo.dto;

import lombok.Data;

/**
 * トピック（お題）の作成・更新リクエストを受け取るためのDTOクラス。
 */
@Data
public class TopicRequest {
    /** お題のテキスト内容 */
    private String content;
    /** 金額や回数などに関連する数値 */
    private Integer amount;
    /** 難易度（通常 1〜5） */
    private Integer difficulty;
    /** お題の詳しい説明文 */
    private String description;
    /** お題の作成者ユーザーID (UUIDの文字列表現) */
    private String creatorId; // UUID string representation
}
