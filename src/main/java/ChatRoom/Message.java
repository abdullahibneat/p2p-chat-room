package ChatRoom;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

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
    
    /**
     * Override comparison criteria to check for differences between two members.
     * Returns true if all fields are the same (case insensitive).
     * 
     * @param o The object to compare against.
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o.getClass() == getClass()) {
            Message msgO = (Message)o;
            return msgO.userName.equalsIgnoreCase(userName) && msgO.messageType == messageType && msgO.content.equalsIgnoreCase(content) && msgO.timestamp.equals(timestamp);
        }
        else return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.userName);
        hash = 19 * hash + Objects.hashCode(this.messageType);
        hash = 19 * hash + Objects.hashCode(this.content);
        hash = 19 * hash + Objects.hashCode(this.timestamp);
        return hash;
    }
}
