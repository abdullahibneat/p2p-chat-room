package ChatRoomGUI;

import ChatRoom.MessageType;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * JPanel component to show messages in the GUI.
 *
 * @author iAbdu
 */
public class MessageGUI extends JPanel {
    
    private final String message;
    private final MessageType messageType;
    
    public MessageGUI(String message, MessageType messageType) {
        this.message = message;
        this.messageType = messageType;
        init();
    }
    
    private void init() {
        JTextArea messageText = new JTextArea(message);
        messageText.setLineWrap(true);
        messageText.setEditable(false);
        messageText.setMargin(new Insets(10, 10, 10, 10)); // Add a margin around the text area
        
        if(messageType == MessageType.SYSTEM) {
            add(new JLabel(message));
        } else {
            setLayout(new RelativeLayout());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            if(messageType == MessageType.OUTBOUND) add(new JPanel(), 0.3f);
            add(messageText, 0.7f);
            if(messageType == MessageType.INBOUND) add(new JPanel(), 0.3f);
        }
    }
}
