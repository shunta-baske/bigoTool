package com.example.bingo.service;

import com.example.bingo.entity.User;
import com.example.bingo.repository.UserRepository;
import com.example.bingo.repository.TopicRepository;
import com.example.bingo.repository.BingoCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ユーザー情報の取得、登録、削除等のビジネスロジックを提供するサービスクラス。
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final BingoCardRepository bingoCardRepository;

    public UserService(UserRepository userRepository, TopicRepository topicRepository,
            BingoCardRepository bingoCardRepository) {
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.bingoCardRepository = bingoCardRepository;
    }

    /**
     * 指定されたIDで新しいユーザーを登録します。
     *
     * @param id ユーザーID
     * @return 登録されたユーザー
     */
    public User registerUser(String id) {
        User user = new User();
        user.setId(id);
        return userRepository.save(user);
    }

    /**
     * 指定されたIDのユーザーを取得します。
     *
     * @param id ユーザーID
     * @return ユーザー情報（Optional）
     */
    public Optional<User> getUser(String id) {
        return userRepository.findById(id);
    }

    /**
     * 全てのユーザーのリストを取得します。
     *
     * @return 全ユーザーのリスト
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 管理者権限で指定されたユーザーを強制的に削除します。
     * 外部キー制約のため、ユーザーに関連するトピックとビンゴカードも先に削除されます。
     *
     * @param id 削除するユーザーのID
     * @throws IllegalArgumentException ユーザーが存在しない場合
     */
    public void deleteUserAsAdmin(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Delete associated topics and bingo cards first due to foreign key constraints
        topicRepository.deleteByCreatorId(id);
        bingoCardRepository.deleteByUserId(id);

        userRepository.delete(user);
    }
}
