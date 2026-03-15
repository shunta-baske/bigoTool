package com.example.bingo.repository;

import com.example.bingo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ユーザー情報のデータベース操作を提供するリポジトリインターフェース。
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
