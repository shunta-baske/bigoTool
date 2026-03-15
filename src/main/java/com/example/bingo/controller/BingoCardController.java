package com.example.bingo.controller;

import com.example.bingo.dto.PunchStatusRequest;
import com.example.bingo.entity.BingoCard;
import com.example.bingo.service.BingoCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

/**
 * ビンゴカードに関するAPIを提供するコントローラークラス。
 */
@RestController
@RequestMapping("/api/bingo")
public class BingoCardController {

    private final BingoCardService bingoCardService;
    private final SimpMessagingTemplate messagingTemplate;

    public BingoCardController(BingoCardService bingoCardService, SimpMessagingTemplate messagingTemplate) {
        this.bingoCardService = bingoCardService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 管理者専用：ビンゴカードを2枚同時に生成します。
     *
     * @param userId ユーザーID（"useradmin" のみ許可）
     * @return 生成された2枚のビンゴカード情報
     */
    @PostMapping("/generate")
    public ResponseEntity<List<BingoCard>> generateCards(@RequestParam("userId") String userId) {
        if (!"useradmin".equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        try {
            List<BingoCard> cards = bingoCardService.generateCards(userId);
            return ResponseEntity.ok(cards);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 指定されたユーザーが作成した全ビンゴカードを取得します。
     *
     * @param userId ユーザーID
     * @return ユーザーのビンゴカードのリスト
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BingoCard>> getCardsByUser(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(bingoCardService.getCardsByUser(userId));
    }

    /**
     * 全てのビンゴカードを取得します。
     *
     * @return 全ビンゴカードのリスト
     */
    @GetMapping
    public ResponseEntity<List<BingoCard>> getAllCards() {
        return ResponseEntity.ok(bingoCardService.getAllCards());
    }

    /**
     * 指定されたIDのビンゴカードを取得します。
     *
     * @param id ビンゴカードID
     * @return ビンゴカード情報。存在しない場合は404。
     */
    @GetMapping("/{id}")
    public ResponseEntity<BingoCard> getCard(@PathVariable("id") UUID id) {
        Optional<BingoCard> cardOptional = bingoCardService.getCard(id);
        return cardOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * ビンゴカードの指定されたマスの穴あけ状態を更新します。
     * 更新後はWebSocketを通じてクライアントに変更をブロードキャストします。
     *
     * @param id      ビンゴカードID
     * @param request 穴をあけるマスのインデックスと状態を持つリクエストオブジェクト
     * @return 更新後のビンゴカード情報
     */
    @PutMapping("/{id}/punch")
    public ResponseEntity<BingoCard> updatePunchStatus(
            @PathVariable("id") UUID id,
            @RequestBody PunchStatusRequest request) {
        try {
            BingoCard updatedCard = bingoCardService.updatePunchStatus(id, request.getIndex(), request.isStatus());

            // Broadcast the updated card to all subscribed clients
            messagingTemplate.convertAndSend("/topic/bingo/" + id.toString(), updatedCard);

            return ResponseEntity.ok(updatedCard);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 指定されたビンゴカードを削除します。
     *
     * @param id     ビンゴカードID
     * @param userId 削除をリクエストしたユーザーID
     * @return 処理結果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable("id") UUID id, @RequestParam("userId") String userId) {
        try {
            bingoCardService.deleteCard(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 管理者権限で指定されたビンゴカードを強制的に削除します。
     *
     * @param id ビンゴカードID
     * @return 処理結果
     */
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteCardAdmin(@PathVariable("id") UUID id) {
        try {
            bingoCardService.deleteCardAdmin(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
