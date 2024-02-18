package com.utochkin.kafkaproducerforsma.config;

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
        config.setApplicationDestinationPrefixes("/app"); //Сообщения STOMP,
        // заголовок назначения которых начинается с /chat, направляются к @MessageMapping методам в @Controller классах
        config.enableSimpleBroker("/chat"); // Используйте встроенный брокер сообщений для подписки
        // и широковещательной рассылки и направляйте брокеру сообщения, заголовок назначения которых начинается с /chat
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS(); //это HTTP-URL для конечной точки, к которой клиенту WebSocket
        // (или SockJS) необходимо подключиться для подтверждения связи с WebSocket
    }

}