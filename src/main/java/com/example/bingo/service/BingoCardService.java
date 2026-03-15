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

/**
 * ビンゴカードの生成、取得、更新、削除等のビジネスロジックを提供するサービスクラス。
 */
@Service
@Transactional
public class BingoCardService {

    /** 中央マスに固定で配置するお題 */
    private static final String CENTER_TOPIC = "思い出の写真撮影";

    private final BingoCardRepository bingoCardRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    public BingoCardService(BingoCardRepository bingoCardRepository, TopicRepository topicRepository,
            UserRepository userRepository) {
        this.bingoCardRepository = bingoCardRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    /**
     * 難易度ごとのバケツを構築します。
     */
    private Map<Integer, List<Topic>> buildDifficultyBuckets(List<Topic> allTopics) {
        Map<Integer, List<Topic>> buckets = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            buckets.put(i, new ArrayList<>());
        }
        for (Topic t : allTopics) {
            int diff = (t.getDifficulty() != null) ? t.getDifficulty() : 3;
            diff = Math.max(1, Math.min(5, diff));
            buckets.get(diff).add(t);
        }
        return buckets;
    }

    /**
     * ラウンドロビン方式で24枚のお題を選択します。
     *
     * @param allTopics      全お題リスト
     * @param excludeIds     除外するお題IDセット（重複回避用）
     * @param random         乱数
     * @return 選択された24枚のお題リスト
     */
    private List<Topic> selectTopics(List<Topic> allTopics, Set<UUID> excludeIds, Random random) {
        Map<Integer, List<Topic>> difficultyBuckets = buildDifficultyBuckets(allTopics);

        List<Integer> availableDifficulties = new ArrayList<>();
        int totalAvailable = 0;
        for (int i = 1; i <= 5; i++) {
            List<Topic> filteredBucket = difficultyBuckets.get(i).stream()
                    .filter(t -> !excludeIds.contains(t.getId()))
                    .toList();
            difficultyBuckets.put(i, new ArrayList<>(filteredBucket)); // Update bucket to only contain available topics
            if (!filteredBucket.isEmpty()) {
                availableDifficulties.add(i);
                totalAvailable += filteredBucket.size();
            }
        }

        if (totalAvailable < 24) {
             throw new IllegalStateException("Not enough unique topics available to generate a bingo card. Expected at least 24, but found " + totalAvailable);
        }

        List<Topic> selectedTopics = new ArrayList<>();
        int bucketIndex = 0;
        Set<UUID> usedTopicIds = new HashSet<>(excludeIds);

        while (selectedTopics.size() < 24) {
            // Check and clean up empty buckets from availableDifficulties
            availableDifficulties.removeIf(diff -> difficultyBuckets.get(diff).isEmpty());
            
            if (availableDifficulties.isEmpty()) {
                throw new IllegalStateException("Not enough topics available (unexpected empty buckets).");
            }

            int diff = availableDifficulties.get(bucketIndex % availableDifficulties.size());
            List<Topic> bucket = difficultyBuckets.get(diff);

            Topic randomTopic = bucket.get(random.nextInt(bucket.size()));
            selectedTopics.add(randomTopic);
            usedTopicIds.add(randomTopic.getId());
            bucket.remove(randomTopic); // Remove to prevent selection again

            bucketIndex++;
        }

        Collections.shuffle(selectedTopics, random);
        return selectedTopics;
    }

    /**
     * 24枚のお題リストからビンゴカードを生成して保存します。
     * 中央マス（index 12）は固定で「思い出の写真撮影」を配置します。
     */
    private BingoCard buildAndSaveCard(User user, List<Topic> topics) {
        List<String> finalBoard = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) {
            if (i == 12) {
                finalBoard.add(CENTER_TOPIC);
            } else {
                int selectedIndex = i > 12 ? i - 1 : i;
                Topic selectedTopic = topics.get(selectedIndex);
                String content = selectedTopic.getContent().replace(",", "，");
                finalBoard.add(content);
            }
        }

        String topicsCsv = String.join(",", finalBoard);

        List<String> punches = new ArrayList<>(25);
        for (int i = 0; i < 25; i++) {
            punches.add(i == 12 ? "true" : "false");
        }
        String punchStatusCsv = String.join(",", punches);

        BingoCard bingoCard = new BingoCard();
        bingoCard.setUser(user);
        bingoCard.setTopics(topicsCsv);
        bingoCard.setPunchStatus(punchStatusCsv);

        return bingoCardRepository.save(bingoCard);
    }

    /**
     * 指定されたユーザーのために2枚のビンゴカードを同時に生成します。
     * 2枚の難易度合計が均等になるようにお題を振り分けます。
     * 中央マス（index 12）は固定で「思い出の写真撮影」を配置します。
     *
     * @param userId ユーザーID
     * @return 生成・保存されたビンゴカードのリスト（2枚）
     * @throws IllegalArgumentException ユーザーが存在しない場合
     * @throws IllegalStateException    トピックが1つも存在しない場合
     */
    public List<BingoCard> generateCards(String userId) {
        User user = userRepository.findById(userId)
                .orElseGet(() -> {
                    if ("useradmin".equals(userId)) {
                        User adminUser = new User();
                        adminUser.setId(userId);
                        return userRepository.save(adminUser);
                    }
                    throw new IllegalArgumentException("User not found");
                });

        List<Topic> allTopics = topicRepository.findAll();
        if (allTopics.size() < 24) {
            throw new IllegalStateException("Cannot generate Bingo card: At least 24 topics required.");
        }

        Random random = new Random();

        // 1枚目のお題を選択
        List<Topic> topics1 = selectTopics(allTopics, Collections.emptySet(), random);

        // 2枚目のお題を選択（1枚目で使ったIDを除外しようとする）
        Set<UUID> usedIds1 = new HashSet<>();
        for (Topic t : topics1) {
            usedIds1.add(t.getId());
        }
        
        List<Topic> topics2;
        try {
            topics2 = selectTopics(allTopics, usedIds1, random);
        } catch (IllegalStateException e) {
            // もし残りのユニークなトピックが24未満でエラーになった場合は
            // 2枚目は重複を許して（ただし2枚目のカード内で重複しないよう）再選択する
             topics2 = selectTopics(allTopics, Collections.emptySet(), random);
        }

        // 難易度合計を計算
        int sum1 = topics1.stream().mapToInt(t -> t.getDifficulty() != null ? t.getDifficulty() : 3).sum();
        int sum2 = topics2.stream().mapToInt(t -> t.getDifficulty() != null ? t.getDifficulty() : 3).sum();

        // 難易度差を均等化：差が大きい場合、2枚間でお題をスワップして差を縮める
        for (int iter = 0; iter < 50 && Math.abs(sum1 - sum2) > 2; iter++) {
            if (sum1 > sum2) {
                // 1枚目の高難易度お題と2枚目の低難易度お題をスワップ
                int maxDiff1 = topics1.stream().mapToInt(t -> t.getDifficulty() != null ? t.getDifficulty() : 3).max().orElse(3);
                int minDiff2 = topics2.stream().mapToInt(t -> t.getDifficulty() != null ? t.getDifficulty() : 3).min().orElse(3);
                if (maxDiff1 <= minDiff2) break; // スワップしても改善しない

                int idx1 = findLastIndex(topics1, maxDiff1);
                int idx2 = findFirstIndex(topics2, minDiff2);
                Topic tmp = topics1.get(idx1);
                topics1.set(idx1, topics2.get(idx2));
                topics2.set(idx2, tmp);
            } else {
                int minDiff1 = topics1.stream().mapToInt(t -> t.getDifficulty() != null ? t.getDifficulty() : 3).min().orElse(3);
                int maxDiff2 = topics2.stream().mapToInt(t -> t.getDifficulty() != null ? t.getDifficulty() : 3).max().orElse(3);
                if (minDiff1 >= maxDiff2) break;

                int idx1 = findFirstIndex(topics1, minDiff1);
                int idx2 = findLastIndex(topics2, maxDiff2);
                Topic tmp = topics1.get(idx1);
                topics1.set(idx1, topics2.get(idx2));
                topics2.set(idx2, tmp);
            }

            sum1 = topics1.stream().mapToInt(t -> t.getDifficulty() != null ? t.getDifficulty() : 3).sum();
            sum2 = topics2.stream().mapToInt(t -> t.getDifficulty() != null ? t.getDifficulty() : 3).sum();
        }

        BingoCard card1 = buildAndSaveCard(user, topics1);
        BingoCard card2 = buildAndSaveCard(user, topics2);

        return List.of(card1, card2);
    }

    private int findFirstIndex(List<Topic> topics, int difficulty) {
        for (int i = 0; i < topics.size(); i++) {
            int d = topics.get(i).getDifficulty() != null ? topics.get(i).getDifficulty() : 3;
            if (d == difficulty) return i;
        }
        return 0;
    }

    private int findLastIndex(List<Topic> topics, int difficulty) {
        for (int i = topics.size() - 1; i >= 0; i--) {
            int d = topics.get(i).getDifficulty() != null ? topics.get(i).getDifficulty() : 3;
            if (d == difficulty) return i;
        }
        return 0;
    }

    /**
     * 指定されたユーザーが作成したビンゴカードのリストを取得します。
     *
     * @param userId ユーザーID
     * @return ユーザーが作成したビンゴカードのリスト
     */
    public List<BingoCard> getCardsByUser(String userId) {
        return bingoCardRepository.findByUserId(userId);
    }

    /**
     * 全てのビンゴカードのリストを取得します。
     *
     * @return 全ビンゴカードのリスト
     */
    public List<BingoCard> getAllCards() {
        return bingoCardRepository.findAll();
    }

    /**
     * 指定されたカードIDのビンゴカードを取得します。
     *
     * @param cardId ビンゴカードID
     * @return ビンゴカード情報（Optional）
     */
    public Optional<BingoCard> getCard(UUID cardId) {
        return bingoCardRepository.findById(cardId);
    }

    /**
     * 指定されたビンゴカードのマスの状態を更新します。
     *
     * @param cardId ビンゴカードID
     * @param index  マスのインデックス (0〜24)
     * @param status 新しい状態 (true: 穴あき, false: 穴なし)
     * @return 更新されたビンゴカード
     * @throws IllegalArgumentException カードが存在しない、またはインデックスが不正な場合
     * @throws IllegalStateException    データが破損している場合
     */
    public BingoCard updatePunchStatus(UUID cardId, int index, boolean status) {
        BingoCard card = bingoCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Bingo Card not found"));

        if (index < 0 || index >= 25) {
            throw new IllegalArgumentException("Invalid square index. Must be between 0 and 24");
        }

        if (index == 12) {
            throw new IllegalArgumentException("Cannot modify the center square");
        }

        String[] statuses = card.getPunchStatus().split(",");
        if (statuses.length != 25) {
            throw new IllegalStateException("Corrupted punch status data");
        }

        statuses[index] = String.valueOf(status);
        card.setPunchStatus(String.join(",", statuses));

        return bingoCardRepository.save(card);
    }

    /**
     * ユーザー権限でビンゴカードを削除します。
     *
     * @param cardId        削除するビンゴカードID
     * @param requestUserId リクエストを行うユーザーID
     * @throws IllegalArgumentException カードが存在しない場合
     * @throws IllegalStateException    リクエストしたユーザーが作成者でない場合
     */
    public void deleteCard(UUID cardId, String requestUserId) {
        BingoCard card = bingoCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Bingo Card not found"));

        if (!card.getUser().getId().equals(requestUserId)) {
            throw new IllegalStateException("User is not the creator of this bingo card");
        }

        bingoCardRepository.delete(card);
    }

    /**
     * 管理者権限でビンゴカードを強制的に削除します。
     *
     * @param cardId 削除するビンゴカードID
     * @throws IllegalArgumentException カードが存在しない場合
     */
    public void deleteCardAdmin(UUID cardId) {
        BingoCard card = bingoCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Bingo Card not found"));
        bingoCardRepository.delete(card);
    }
}
