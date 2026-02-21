package com.example.bingo.controller;

import com.example.bingo.entity.User;
import com.example.bingo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if ("useradmin".equals(id)) {
            return ResponseEntity.status(403).body("このIDは登録できません");
        }

        Optional<User> existingUser = userService.getUser(id);
        if (existingUser.isPresent()) {
            return ResponseEntity.status(409).body("既に登録されているIDです");
        }

        User newUser = userService.registerUser(id);
        return ResponseEntity.ok(newUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        Optional<User> userOptional = userService.getUser(id);
        return userOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteUserAsAdmin(@PathVariable String id) {
        try {
            userService.deleteUserAsAdmin(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
