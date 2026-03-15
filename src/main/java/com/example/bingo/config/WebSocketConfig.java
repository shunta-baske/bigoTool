package com.example.bingo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket通信の設定を行うクラス。
 * ビンゴカードのリアルタイム更新（マスの穴あけ等）に使用されます。
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * メッセージブローカーの設定を行います。
     * サーバーからクライアントへのブロードキャスト用プレフィックスや、
     * クライアントからサーバーへのメッセージ用プレフィックスを定義します。
     *
     * @param config メッセージブローカーのレジストリ
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Use /topic prefix for broadcasting messages (from server to clients)
        config.enableSimpleBroker("/topic");
        // Prefix for messages bound for @MessageMapping methods (from clients to
        // server)
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * クライアントがWebSocket通信を開始するためのエンドポイントを登録します。
     * SockJSによるフォールバックも有効化します。
     *
     * @param registry STOMPエンドポイントのレジストリ
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint clients will connect to.
        // withSockJS() enables fallback options for browsers that don't support
        // WebSocket.
        registry.addEndpoint("/ws-bingo").withSockJS();
    }
}
