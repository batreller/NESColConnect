package nescol.connect.facade;

import nescol.connect.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ChatFacade {
    @Value("${jwt.token.expired}")
    private long jwtValidityInMilliseconds;

    @Value("${time.to.accept.chat}")
    private long timeToAcceptChat;

    @Value("${time.cooldown.same.student}")
    private long timeCooldownSameStudent;

    @Autowired
    private RedisService redisService;

    private final String WEBSOCKET_CHAT_QUEUE = "WEBSOCKET_CHAT_QUEUE";
    private final String DIALOGS = "DIALOGS";
    private final String ACCEPT_CHAT = "ACCEPTED_CHAT";
    private final String PUBLIC_KEY = "PUBLIC_KEYS";
    private final String CHAT_PARTICIPANTS = "CHAT_PARTICIPANTS";
    private final String CHAT_ID = "CHAT_ID";

    public UsernamePasswordAuthenticationToken getStudentToSpeak(Principal user) {
        Set<Object> usersInQueueNow = getUsersInQueue();

        for (Object possibleUser : usersInQueueNow) {
            if (!Objects.equals(user, possibleUser) && !isUsersHaveSpoken(user, (Principal) possibleUser)) {
                return (UsernamePasswordAuthenticationToken) possibleUser;
            }
        }
        return null;
    }

    public void addUserToChat(String chatId, String sessionId, long expiringTime) {
        redisService.addToSetWithExpiry(chatId + CHAT_PARTICIPANTS, sessionId, expiringTime);
        redisService.storeWithExpiry(sessionId + CHAT_ID, chatId, expiringTime);
    }

    public void increaseChatLifeTime(String chatId, long expiringTime) {
        Set<Object> chatParticipants = getChatParticipants(chatId);
        for (Object sessionId : chatParticipants) {
            redisService.extendKeyExpiry(sessionId + CHAT_ID, expiringTime);
        }
        redisService.extendKeyExpiry(chatId + CHAT_PARTICIPANTS, expiringTime);
    }

    public String getUsersChatId(String sessionId) {
        return (String) redisService.get(sessionId + CHAT_ID);
    }

    public boolean isUserChatParticipant(String chatId, String chatParticipantId) {
        return getChatParticipants(chatId).contains(chatParticipantId);
    }

    public Set<Object> getChatParticipants(String chatId) {
        return redisService.getSet(chatId + CHAT_PARTICIPANTS);
    }

    public void addChatCooldown(Principal user, Set<Object> usersToIgnore) {
        for (Object userToIgnoreSessionId : usersToIgnore) {
            Principal userToIgnore = getUserBySession(userToIgnoreSessionId);
            redisService.storeWithExpiry(user.getName() + userToIgnore.getName() + DIALOGS, true, timeCooldownSameStudent);
        }
    }

    public boolean isUsersHaveSpoken(Principal user, Principal user2) {
        Object value = redisService.get(user.getName() + user2.getName() + DIALOGS);
        return (value != null);
    }

    public void removeChat(String chatId) {
        Set<Object> chatParticipants = getChatParticipants(chatId);
        for (Object chatParticipant : chatParticipants) {
            Principal user = getUserBySession(chatParticipant);
            addChatCooldown(user, chatParticipants);
        }
        redisService.remove(chatId + CHAT_PARTICIPANTS);
    }

    public void userAcceptedChat(String chatId, String sessionId, String publicKey) {
        redisService.addToSetWithExpiry(chatId + ACCEPT_CHAT, sessionId, timeToAcceptChat);
        redisService.addToHashMapWithExpiry(chatId + PUBLIC_KEY, sessionId, publicKey, timeToAcceptChat);
    }

    public Map<Object, Object> getPublicKeys(String chatId) {
        return redisService.getHashMap(chatId + PUBLIC_KEY);
    }

    public Set<Object> getUsersWhoAcceptedChat(String chatId) {
        return redisService.getSet(chatId + ACCEPT_CHAT);
    }

    public boolean isChatAcceptedByEveryone(String chatId) {
        Set<Object> chatParticipants = getChatParticipants(chatId);
        Set<Object> usersWhoAcceptedChat = getUsersWhoAcceptedChat(chatId);

        return usersWhoAcceptedChat.containsAll(chatParticipants);
    }

    public void addUserToQueue(Principal user) {
        redisService.addToSet(WEBSOCKET_CHAT_QUEUE, user);
    }

    public Set<Object> getUsersInQueue() {
        return redisService.getSet(WEBSOCKET_CHAT_QUEUE);
    }

    public void removeUserFromQueue(Principal user) {
        redisService.removeFromSet(WEBSOCKET_CHAT_QUEUE, user);
    }

    public void saveSession(Principal user, String sessionId) {
        redisService.storeWithExpiry(user, sessionId, jwtValidityInMilliseconds / 1000);
        redisService.storeWithExpiry(sessionId, user, jwtValidityInMilliseconds / 1000);
    }

    public String getSession(Principal user) {
        return (String) redisService.get(user);
    }

    public Principal getUserBySession(Object sessionId) {
        return (Principal) redisService.get(sessionId);
    }
}
