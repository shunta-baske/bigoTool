package com.example.bingo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Use /topic prefix for broadcasting messages (from server to clients)
        config.enableSimpleBroker("/topic");
        // Prefix for messages bound for @MessageMapping methods (from clients to
        // server)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint clients will connect to.
        // withSockJS() enables fallback options for browsers that don't support
        // WebSocket.
        registry.addEndpoint("/ws-bingo").withSockJS();
    }
}
