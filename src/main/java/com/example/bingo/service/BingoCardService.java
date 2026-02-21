package com.example.bingo.service;

import com.example.bingo.entity.BingoCard;
import com.example.bingo.entity.Topic;
import com.example.bingo.entity.User;
import com.example.bingo.repository.BingoCardRepository;
import com.example.bingo.repository.TopicRepository;
import com.example.bingo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class BingoCardService {

    private final BingoCardRepository bingoCardRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public BingoCardService(BingoCardRepository bingoCardRepository, TopicRepository topicRepository,
            UserRepository userRepository) {
        this.bingoCardRepository = bingoCardRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    public BingoCard generateCard(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Topic> allTopics = topicRepository.findAll();
        if (allTopics.isEmpty()) {
            throw new IllegalStateException("Cannot generate Bingo card: No topics available");
        }

        // Group topics by difficulty (1 to 5)
        Map<Integer, List<Topic>> difficultyBuckets = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            difficultyBuckets.put(i, new ArrayList<>());
        }
        for (Topic t : allTopics) {
            // Safety bounds in case of old data
            int diff = (t.getDifficulty() != null) ? t.getDifficulty() : 3;
            diff = Math.max(1, Math.min(5, diff));
            difficultyBuckets.get(diff).add(t);
        }

        // Get available difficulty buckets
        List<Integer> availableDifficulties = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            if (!difficultyBuckets.get(i).isEmpty()) {
                availableDifficulties.add(i);
            }
        }

        // We need 24 topics to surround the FREE square
        List<Topic> selectedTopics = new ArrayList<>();
        Random random = new Random();
        int bucketIndex = 0;

        // Draw round-robin across available difficulty buckets
        while (selectedTopics.size() < 24) {
            int diff = availableDifficulties.get(bucketIndex % availableDifficulties.size());
            List<Topic> bucket = difficultyBuckets.get(diff);
            Topic randomTopic = bucket.get(random.nextInt(bucket.size()));
            selectedTopics.add(randomTopic);
            bucketIndex++;
        }

        // Let's shuffle the selected 24 topics just in case
        Collections.shuffle(selectedTopics);

        // Build the 25 topics list, with FREE at index 12
        List<String> finalBoard = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) {
            if (i == 12) {
                finalBoard.add("FREE");
            } else {
                int selectedIndex = i > 12 ? i - 1 : i;
                Topic selectedTopic = selectedTopics.get(selectedIndex);
                // Escape commas to prevent breaking the CSV format if a topic contains a comma
                String content = selectedTopic.getContent().replace(",", "，");
                finalBoard.add(content);
            }
        }

        String topicsCsv = String.join(",", finalBoard);

        // Initial punch status: all false, except index 12 which is true
        List<String> punches = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) {
            if (i == 12) {
                punches.add("true");
            } else {
                punches.add("false");
            }
        }
        String punchStatusCsv = String.join(",", punches);

        BingoCard bingoCard = new BingoCard();
        bingoCard.setUser(user);
        bingoCard.setTopics(topicsCsv);
        bingoCard.setPunchStatus(punchStatusCsv);

        return bingoCardRepository.save(bingoCard);
    }

    public List<BingoCard> getCardsByUser(String userId) {
        return bingoCardRepository.findByUserId(userId);
    }

    public List<BingoCard> getAllCards() {
        return bingoCardRepository.findAll();
    }

    public Optional<BingoCard> getCard(UUID cardId) {
        return bingoCardRepository.findById(cardId);
    }

    public BingoCard updatePunchStatus(UUID cardId, int index, boolean status) {
        BingoCard card = bingoCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Bingo Card not found"));

        if (index < 0 || index >= 25) {
            throw new IllegalArgumentException("Invalid square index. Must be between 0 and 24");
        }

        // 12 is always FREE and always punched
        if (index == 12) {
            throw new IllegalArgumentException("Cannot modify the center FREE square");
        }

        String[] statuses = card.getPunchStatus().split(",");
        if (statuses.length != 25) {
            throw new IllegalStateException("Corrupted punch status data");
        }

        statuses[index] = String.valueOf(status);
        card.setPunchStatus(String.join(",", statuses));

        return bingoCardRepository.save(card);
    }

    public void deleteCard(UUID cardId, String requestUserId) {
        BingoCard card = bingoCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Bingo Card not found"));

        if (!card.getUser().getId().equals(requestUserId)) {
            throw new IllegalStateException("User is not the creator of this bingo card");
        }

        bingoCardRepository.delete(card);
    }

    public void deleteCardAdmin(UUID cardId) {
        BingoCard card = bingoCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Bingo Card not found"));
        bingoCardRepository.delete(card);
    }
}
