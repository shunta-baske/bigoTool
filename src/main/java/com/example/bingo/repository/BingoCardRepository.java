package com.example.bingo.repository;

import com.example.bingo.entity.BingoCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.UUID;

/**
 * ビンゴカードエンティティのデータベース操作を提供するリポジトリインターフェース。
 */
@Repository
public interface BingoCardRepository extends JpaRepository<BingoCard, UUID> {
    /**
     * 指定されたユーザーIDに紐づくビンゴカードのリストを取得します。
     *
     * @param userId ユーザーID
     * @return ビンゴカードのリスト
     */
    List<BingoCard> findByUserId(String userId);

    /**
     * 指定されたユーザーIDに紐づく全てのビンゴカードを削除します。
     *
     * @param userId ユーザーID
     */
    void deleteByUserId(String userId);
}
