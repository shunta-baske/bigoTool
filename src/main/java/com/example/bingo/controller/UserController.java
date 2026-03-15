package com.example.bingo.controller;

import com.example.bingo.entity.User;
import com.example.bingo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ユーザー情報の管理や登録に関するAPIを提供するコントローラークラス。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 新規ユーザーを登録します。
     *
     * @param body リクエストボディに含まれるパラメータのマップ
     * @return 登録されたユーザー情報
     */
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

    /**
     * 指定されたIDのユーザーを取得します。
     *
     * @param id ユーザーID
     * @return ユーザー情報。存在しない場合は404。
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") String id) {
        Optional<User> userOptional = userService.getUser(id);
        return userOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 全てのユーザーを取得します。
     *
     * @return 全ユーザーのリスト
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * 管理者権限で指定されたユーザーを強制的に削除します。
     *
     * @param id ユーザーID
     * @return 処理結果
     */
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Void> deleteUserAsAdmin(@PathVariable("id") String id) {
        try {
            userService.deleteUserAsAdmin(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
