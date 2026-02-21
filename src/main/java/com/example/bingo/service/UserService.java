package com.example.bingo.service;

import com.example.bingo.entity.User;
import com.example.bingo.repository.UserRepository;
import com.example.bingo.repository.TopicRepository;
import com.example.bingo.repository.BingoCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public User registerUser(String id) {
        User user = new User();
        user.setId(id);
        return userRepository.save(user);
    }

    public Optional<User> getUser(String id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUserAsAdmin(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Delete associated topics and bingo cards first due to foreign key constraints
        topicRepository.deleteByCreatorId(id);
        bingoCardRepository.deleteByUserId(id);

        userRepository.delete(user);
    }
}
