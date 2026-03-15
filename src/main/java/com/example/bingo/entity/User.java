package com.example.bingo.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * システムを利用するユーザーの情報を表すエンティティクラス。
 */
@Entity
@Table(name = "users") // 'user'はSQLの予約語になることが多いため複数形を使用
@Data
public class User {

    /** ユーザーを一意に識別するID（文字列） */
    @Id
    @Column(nullable = false, length = 100)
    private String id;

    // TODO: 必要に応じてその他のユーザー情報（名前や認証情報など）を追加
}
