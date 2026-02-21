package com.example.bingo.repository;

import com.example.bingo.entity.BingoCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.UUID;

@Repository
public interface BingoCardRepository extends JpaRepository<BingoCard, UUID> {
    List<BingoCard> findByUserId(String userId);

    void deleteByUserId(String userId);
}
