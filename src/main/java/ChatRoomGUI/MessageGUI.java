package ChatRoomGUI;

import ChatRoom.Message;
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
    
    private final Message message;
    private final boolean myMessage;
    
    public MessageGUI(Message message, boolean myMessage) {
        this.message = message;
        this.myMessage = myMessage;
        init();
    }
    
    private void init() {
        if(message.getMessageType() == MessageType.SYSTEM) {
            add(new JLabel(message.getContent()));
        } else {
            JTextArea messageText = new JTextArea((myMessage? "" : message.getUsername() + ": ") + message.getContent());
            
            messageText.setLineWrap(true);
            messageText.setEditable(false);
            messageText.setMargin(new Insets(10, 10, 10, 10)); // Add a margin around the text area
            
            setLayout(new RelativeLayout());
            
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            if(myMessage) add(new JPanel(), 0.3f);
            add(messageText, 0.7f);
            if(!myMessage) add(new JPanel(), 0.3f);
        }
    }
}
