package com.example.bingo.controller;

import com.example.bingo.dto.TopicRequest;
import com.example.bingo.entity.Topic;
import com.example.bingo.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.UUID;

/**
 * トピック（お題）に関するAPIを提供するコントローラークラス。
 */
@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * 新しいトピックを作成します。
     *
     * @param request トピック作成に必要な情報を持つリクエストオブジェクト
     * @return 作成されたトピック
     */
    @PostMapping
    public ResponseEntity<Topic> createTopic(@RequestBody TopicRequest request) {
        try {
            Topic createdTopic = topicService.createTopic(request.getContent(), request.getDescription(),
                    request.getCreatorId(),
                    request.getAmount(), request.getDifficulty());
            return ResponseEntity.ok(createdTopic);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 全てのトピックを取得します。
     *
     * @return トピックのリスト
     */
    @GetMapping
    public ResponseEntity<List<Topic>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }

    /**
     * 指定されたユーザーが作成した全てのトピックを取得します。
     *
     * @param userId ユーザーID
     * @return ユーザーのトピックリスト
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Topic>> getTopicsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(topicService.getTopicsByUser(userId));
    }

    /**
     * トピックの内容（文字列）でトピックを検索します。
     *
     * @param content 検索するトピックの文字列
     * @return 見つかったトピック。存在しない場合は404。
     */
    @GetMapping("/search")
    public ResponseEntity<Topic> searchTopic(@RequestParam String content) {
        return topicService.findTopicByContent(content)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 指定されたトピックを更新します。
     *
     * @param id      トピックID
     * @param request 更新するトピックの情報を持つリクエストオブジェクト
     * @return 更新されたトピック
     */
    @PutMapping("/{id}")
    public ResponseEntity<Topic> updateTopic(@PathVariable UUID id, @RequestBody TopicRequest request) {
        try {
            Topic updatedTopic = topicService.updateTopic(id, request.getContent(), request.getDescription(),
                    request.getAmount(),
                    request.getDifficulty(), request.getCreatorId());
            return ResponseEntity.ok(updatedTopic);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 指定されたトピックを削除します。
     *
     * @param id     トピックID
     * @param userId 削除をリクエストしたユーザーID
     * @return 処理結果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable UUID id, @RequestParam String userId) {
        try {
            topicService.deleteTopic(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 管理者権限で指定されたトピックを強制的に削除します。
     *
     * @param id トピックID
     * @return 処理結果
     */
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteTopicAsAdmin(@PathVariable UUID id) {
        try {
            topicService.deleteTopicAsAdmin(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
