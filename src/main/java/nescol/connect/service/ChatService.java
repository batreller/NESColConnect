package nescol.connect.service;

import nescol.connect.data.ChatAccepted;
import nescol.connect.data.ChatData;
import nescol.connect.data.ChatMessage;
import nescol.connect.facade.ChatFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Objects;
import java.util.UUID;

@Service
public class ChatService {
    @Value("${time.to.accept.chat}")
    private long timeToAcceptChat;

    @Value("${time.chat.live}")
    private long timeChatLive;

    @Autowired
    private ChatFacade chatFacade;

    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void searchChat(SimpMessageHeaderAccessor headerAccessor) {
        UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
        chatFacade.addUserToQueue(user);
        UsernamePasswordAuthenticationToken userToSpeak = chatFacade.getStudentToSpeak(user);

        if (userToSpeak != null) {
            // found
            String userToSpeakSessionId = chatFacade.getSession(userToSpeak);
            String userSessionId = Objects.requireNonNull(headerAccessor.getSessionId());
            String chatId = UUID.randomUUID().toString();
            ChatData chatDataMessage = new ChatData();
            chatDataMessage.setChatId(chatId);
            chatFacade.addUserToChat(chatId, userSessionId, timeToAcceptChat);
            chatFacade.addUserToChat(chatId, userToSpeakSessionId, timeToAcceptChat);
            chatFacade.removeUserFromQueue(user);
            chatFacade.removeUserFromQueue(userToSpeak);
            messagingTemplate.convertAndSendToUser(userToSpeakSessionId, "/private/student.found", chatDataMessage);
            messagingTemplate.convertAndSendToUser(userSessionId, "/private/student.found", chatDataMessage);
        } else {
            ChatData chatDataMessage = new ChatData();
            // didnt find
            messagingTemplate.convertAndSendToUser(Objects.requireNonNull(headerAccessor.getSessionId()), "/private/student.not.found", chatDataMessage);
        }
    }

    public void acceptChat(ChatData chatData, SimpMessageHeaderAccessor headerAccessor) throws InterruptedException {
        String userSessionId = Objects.requireNonNull(headerAccessor.getSessionId());
        String chatId = chatData.getChatId();
        if (!chatFacade.isUserChatParticipant(chatId, userSessionId)) {
            return;
        }
        chatFacade.userAcceptedChat(chatId, userSessionId, chatData.getPublicKey());
        Thread.sleep(1000);
        ChatAccepted isChatAccepted = new ChatAccepted();
        if (chatFacade.isChatAcceptedByEveryone(chatId)) {
            chatFacade.increaseChatLifeTime(chatId, timeChatLive);
            isChatAccepted.setIsAccepted(true);
            isChatAccepted.setPublicKeys(chatFacade.getPublicKeys(chatId));
        } else {
            isChatAccepted.setIsAccepted(false);
            chatFacade.removeChat(chatId);
        }
        messagingTemplate.convertAndSendToUser(userSessionId, "/private/chat.acceptance", isChatAccepted);
    }

    public void sendMessage(ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String userSessionId = headerAccessor.getSessionId();
        if (!chatFacade.isUserChatParticipant(chatMessage.getChatId(), userSessionId)) {
            return;
        }
        chatMessage.setSenderId(userSessionId);
        chatFacade.increaseChatLifeTime(chatMessage.getChatId(), timeChatLive);
        messagingTemplate.convertAndSend("/chat/" + chatMessage.getChatId() + "/message", chatMessage);
    }

    public void userDisconnectedFromWebsocket(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal user = headerAccessor.getUser();
        chatFacade.removeUserFromQueue(user);
        String chatId = chatFacade.getUsersChatId(sessionId);
        if (chatId != null) {
            messagingTemplate.convertAndSend("/chat/" + chatId + "/closed", "");
            chatFacade.removeChat(chatId);
        }
    }
}
