package com.carogame.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Mở endpoint "/ws" cho Front-end kết nối vào.
        // Bật withSockJS() để tương thích với thư viện SockJS ở Front-end.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép Front-end (port 5173) truy cập
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Tiền tố cho các kênh mà Server sẽ đẩy dữ liệu về cho Client
        registry.enableSimpleBroker("/topic", "/queue");

        // Tiền tố cho các message mà Client gửi lên Server
        registry.setApplicationDestinationPrefixes("/app");
    }
}