package nescol.connect.data;

import lombok.Data;

@Data
public class ChatMessage {
    private String chatId;
    private String senderId;
    private String messageText;
}
