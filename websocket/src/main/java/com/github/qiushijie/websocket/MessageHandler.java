package com.github.qiushijie.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class MessageHandler extends TextWebSocketHandler {

//    private final static String uuid = UUID.randomUUID().toString().replace("-", "");

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private Map<Integer, WebSocketSession> clients = new HashMap<>();

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Value("${server.port}")
    private String port;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer id = (Integer) session.getAttributes().get("id");
        clients.put(id, session);
        System.out.println("连接建立 userId: " + id + " port: " + port);
        Queue queue = new Queue("ws-user-" + id);
        rabbitAdmin.declareQueue(queue);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Integer id = (Integer) session.getAttributes().get("id");
        clients.remove(id);
        System.out.println("连接断开 userId: " + session.getId());
        rabbitAdmin.deleteQueue("ws-user-" + id);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String payload = textMessage.getPayload();
        Message message = objectMapper.readValue(payload, Message.class);
        System.out.println("接受到的数据: " + message);
        rabbitTemplate.convertAndSend("user-" + message.getTo(), message.getText());
//        String toServer = redisTemplate.opsForValue().get("ws-" + message.getTo());
//        if (toServer != null) {
//            System.out.println("toServer: " + toServer);
//        }
//        session.sendMessage(new TextMessage("服务器返回收到的信息," + payload));
    }
}
