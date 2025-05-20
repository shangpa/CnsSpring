package com.example.springjwt.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.send") // 클라이언트에서 "/app/chat.send" 로 보냄
    public void sendMessage(ChatMessage message) {
        chatMessageService.save(message); // DB 저장
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + message.getRoomKey(),
                message
        ); // 구독자에게 전송
    }
}
