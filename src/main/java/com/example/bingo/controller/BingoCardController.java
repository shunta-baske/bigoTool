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

@RestController
@RequestMapping("/api/bingo")
public class BingoCardController {

    private final BingoCardService bingoCardService;
    private final SimpMessagingTemplate messagingTemplate;

    public BingoCardController(BingoCardService bingoCardService, SimpMessagingTemplate messagingTemplate) {
        this.bingoCardService = bingoCardService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/generate")
    public ResponseEntity<BingoCard> generateCard(@RequestParam String userId) {
        try {
            BingoCard card = bingoCardService.generateCard(userId);
            return ResponseEntity.ok(card);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BingoCard>> getCardsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(bingoCardService.getCardsByUser(userId));
    }

    @GetMapping
    public ResponseEntity<List<BingoCard>> getAllCards() {
        return ResponseEntity.ok(bingoCardService.getAllCards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BingoCard> getCard(@PathVariable UUID id) {
        Optional<BingoCard> cardOptional = bingoCardService.getCard(id);
        return cardOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/punch")
    public ResponseEntity<BingoCard> updatePunchStatus(
            @PathVariable UUID id,
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id, @RequestParam String userId) {
        try {
            bingoCardService.deleteCard(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteCardAdmin(@PathVariable UUID id) {
        try {
            bingoCardService.deleteCardAdmin(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
