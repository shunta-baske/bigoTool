package com.example.bingo.repository;

import com.example.bingo.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.UUID;

/**
 * トピック（お題）エンティティのデータベース操作を提供するリポジトリインターフェース。
 */
@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    /**
     * 指定されたユーザーが作成したトピックのリストを取得します。
     *
     * @param creatorId 作成者のユーザーID
     * @return トピックのリスト
     */
    List<Topic> findByCreatorId(String creatorId);

    /**
     * 指定された内容を持つ最初のトピックを検索します。
     *
     * @param content 検索するトピック内容
     * @return 見つかったトピック（Optional）
     */
    java.util.Optional<Topic> findFirstByContent(String content);

    /**
     * 指定されたユーザーが作成した全てのトピックを削除します。
     *
     * @param creatorId 作成者のユーザーID
     */
    void deleteByCreatorId(String creatorId);
}
