package ChatRoom;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A component to store details about a message.
 *
 * @author iAbdu
 */
public class Message implements Serializable {
    private final String userName;
    private final MessageType messageType;
    private final String content;
    private final LocalDateTime timestamp;
    
    public Message(String userName, String content, MessageType messageType) {
        this.userName = userName;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getUsername() { return userName; }
    public MessageType getMessageType() { return messageType; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "{username: " + userName + ", messageType: " + messageType + ", content: " + content + ", timestamp: " + timestamp + "}";
    }
}
