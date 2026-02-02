package main.com.example.bingo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "users") // 'user'は予約語のため複数形を推奨
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 必要に応じてユーザー名などを追加可能
    // private String name;
}O
