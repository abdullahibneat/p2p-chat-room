package ChatRoomGUI;

import ChatRoom.Message;
import ChatRoom.MessageType;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
    private boolean fullTime = false;
    private final String time;
    private final String dateTime;
    
    public MessageGUI(Message message, boolean myMessage) {
        this.message = message;
        this.myMessage = myMessage;
        time = message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm"));
        dateTime = "Sent on " + message.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " at " + message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss.S"));
        init();
    }
    
    private void init() {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        if(message.getMessageType() == MessageType.SYSTEM) {
            add(new JLabel(message.getContent()));
        } else {
            JPanel messageWrapper = new JPanel();
            messageWrapper.setLayout(new RelativeLayout());
            JTextArea messageText = new JTextArea((myMessage? "" : message.getUsername() + ": ") + message.getContent());
            
            messageText.setLineWrap(true);
            messageText.setEditable(false);
            messageText.setMargin(new Insets(10, 10, 10, 10)); // Add a margin around the text area
            
            if(myMessage) messageWrapper.add(new JPanel(), 0.3f);
            messageWrapper.add(messageText, 0.7f);
            if(!myMessage) messageWrapper.add(new JPanel(), 0.3f);
            
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            add(messageWrapper);
            
            JPanel timeWrapper = new JPanel(new BorderLayout());
            
            JLabel timestamp = new JLabel(time);
            timeWrapper.add(timestamp, myMessage? BorderLayout.EAST : BorderLayout.WEST);
            
            // Switch between time and date-time on mouse click
            timeWrapper.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    fullTime = !fullTime;
                    timestamp.setText(fullTime? dateTime : time);
                }

                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e) {}

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
            
            add(timeWrapper);
        }
    }
}
