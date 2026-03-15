package com.example.bingo.service;

import com.example.bingo.entity.Topic;
import com.example.bingo.entity.User;
import com.example.bingo.repository.TopicRepository;
import com.example.bingo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * トピック（お題）の作成、取得、更新、削除等のビジネスロジックを提供するサービスクラス。
 */
@Service
@Transactional
public class TopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public TopicService(TopicRepository topicRepository, UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    /**
     * 新しいトピックを作成します。
     *
     * @param content     トピックのタイトルや内容
     * @param description トピックの詳細な説明
     * @param creatorId   作成者のユーザーID
     * @param amount      金額や回数などの目安
     * @param difficulty  難易度（1〜5、デフォルトは3）
     * @return 作成されたトピック
     * @throws IllegalArgumentException 作成者のユーザーが存在しない場合
     */
    public Topic createTopic(String content, String description, String creatorId, Integer amount, Integer difficulty) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Topic topic = new Topic();
        topic.setContent(content);
        topic.setDescription(description);
        topic.setAmount(amount != null ? amount : 0);

        // Default difficulty to 3 (normal), or clamp between 1-5
        if (difficulty == null || difficulty < 1 || difficulty > 5) {
            topic.setDifficulty(3);
        } else {
            topic.setDifficulty(difficulty);
        }

        topic.setCreator(creator);

        return topicRepository.save(topic);
    }

    /**
     * 全てのトピックのリストを取得します。
     *
     * @return 全トピックのリスト
     */
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    /**
     * 指定されたユーザーが作成したトピックのリストを取得します。
     *
     * @param creatorId ユーザーID
     * @return ユーザーが作成したトピックのリスト
     */
    public List<Topic> getTopicsByUser(String creatorId) {
        return topicRepository.findByCreatorId(creatorId);
    }

    /**
     * 既存のトピックを更新します。
     * 更新をリクエストしたユーザーがトピックの作成者である必要があります。
     *
     * @param topicId        更新するトピックID
     * @param newContent     新しいトピック内容
     * @param newDescription 新しい説明文
     * @param newAmount      新しい目安（金額等）
     * @param newDifficulty  新しい難易度
     * @param requestUserId  更新をリクエストするユーザーID
     * @return 更新されたトピック
     * @throws IllegalArgumentException トピックが存在しない場合
     * @throws IllegalStateException    リクエストしたユーザーが作成者でない場合
     */
    public Topic updateTopic(UUID topicId, String newContent, String newDescription, Integer newAmount,
            Integer newDifficulty,
            String requestUserId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!topic.getCreator().getId().equals(requestUserId)) {
            throw new IllegalStateException("User is not the creator of this topic");
        }

        topic.setContent(newContent);
        topic.setDescription(newDescription);
        topic.setAmount(newAmount != null ? newAmount : 0);

        if (newDifficulty == null || newDifficulty < 1 || newDifficulty > 5) {
            topic.setDifficulty(3);
        } else {
            topic.setDifficulty(newDifficulty);
        }

        return topicRepository.save(topic);
    }

    /**
     * 指定された内容（完全一致）を持つ最初のトピックを検索します。
     *
     * @param content 検索する内容
     * @return 見つかったトピック（Optional）
     */
    public Optional<Topic> findTopicByContent(String content) {
        return topicRepository.findFirstByContent(content);
    }

    /**
     * ユーザー権限でトピックを削除します。
     *
     * @param topicId       削除するトピックID
     * @param requestUserId リクエストを行うユーザーID
     * @throws IllegalArgumentException トピックが存在しない場合
     * @throws IllegalStateException    リクエストしたユーザーが作成者でない場合
     */
    public void deleteTopic(UUID topicId, String requestUserId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (!topic.getCreator().getId().equals(requestUserId)) {
            throw new IllegalStateException("User is not the creator of this topic");
        }

        topicRepository.delete(topic);
    }

    /**
     * 管理者権限でトピックを強制的に削除します。
     *
     * @param topicId 削除するトピックID
     * @throws IllegalArgumentException トピックが存在しない場合
     */
    public void deleteTopicAsAdmin(UUID topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        topicRepository.delete(topic);
    }
}
