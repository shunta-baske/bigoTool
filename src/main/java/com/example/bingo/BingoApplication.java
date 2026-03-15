package com.example.bingo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Bootアプリケーションの起動クラス。
 * ビンゴツールのバックエンドサーバーを起動します。
 */
@SpringBootApplication
public class BingoApplication {

    /**
     * アプリケーションのメインメソッド。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(BingoApplication.class, args);
    }

}
