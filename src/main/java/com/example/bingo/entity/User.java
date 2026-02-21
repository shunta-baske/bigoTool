package com.example.bingo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users") // 'user'はSQLの予約語になることが多いため複数形を使用
@Data
public class User {

    @Id
    @Column(nullable = false, length = 100)
    private String id;

    // TODO: 必要に応じてその他のユーザー情報（名前や認証情報など）を追加
}
