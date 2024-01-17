package nescol.connect.controller;

import nescol.connect.data.ChatData;
import nescol.connect.data.ChatMessage;
import nescol.connect.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

@Controller
public class ChatController {
    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat/search")
    public void searchChat(
            SimpMessageHeaderAccessor headerAccessor
    ) {
        chatService.searchChat(headerAccessor);
    }

    @MessageMapping("/chat/accept")
    public void acceptChat(
            @Payload ChatData chatData,
            SimpMessageHeaderAccessor headerAccessor
    ) throws InterruptedException {
        chatService.acceptChat(chatData, headerAccessor);
    }

    @MessageMapping("/chat/message/send")
    public void sendMessage(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        chatService.sendMessage(chatMessage, headerAccessor);
    }
}
