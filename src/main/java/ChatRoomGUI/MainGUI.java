package ChatRoomGUI;

import ChatRoom.ClientGUI;
import ChatRoom.Member;
import ChatRoom.MessageType;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

/**
 * User Interface for this application.
 *
 * @author iAbdu
 */
public class MainGUI extends JFrame implements ClientGUI {
    
    private final JSplitPane sp = new JSplitPane();
    private final JPanel leftPanel = new JPanel(); // Panel to display all members
    private final JPanel rightPanel = new JPanel(); // Panel with chat, message input and send button.
    private JScrollPane chatScrollPane = null;
    private final JPanel chatPanel = new JPanel(); // Chat messages are displayed here
    private final JButton sendButton = new JButton("Send");
    private final JTextArea messageInput = new JTextArea();
    
    private final String PLACEHOLDER_TEXT = "Type your message here...";
    
    public MainGUI() {
        
        // Placeholder text handling
        messageInput.setForeground(Color.gray);
        messageInput.setText(PLACEHOLDER_TEXT);
        messageInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if(messageInput.getText().equals(PLACEHOLDER_TEXT)) {
                    messageInput.setText("");
                    messageInput.setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(messageInput.getText().isEmpty()) {
                    messageInput.setForeground(Color.gray);
                    messageInput.setText(PLACEHOLDER_TEXT);
                }
            }
        });
        
        init();
    }
    
    /**
     * Initialise the user interface.
     */
    private void init() {
        setTitle("Chat Room");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 500));
        
        // Configure JSplitPane
        sp.setOneTouchExpandable(true);
        sp.setDividerLocation(200);
        sp.setLeftComponent(initializeLeftPanel());
        sp.setRightComponent(initializeRightPanel());
        
        add(sp);
        
        setVisible(true);
    }
    
    /**
     * Initialise panel where member details can be seen.
     */
    private JScrollPane initializeLeftPanel() {
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        JScrollPane s = new JScrollPane(leftPanel);
        s.setMinimumSize(new Dimension(200, 0)); // Same as initial divider location of the JSplitPane
        return s;
    }
    
    /**
     * Initialise the right-hand side of the interface, with the chat on top and message input and send button at the bottom.
     */
    private JPanel initializeRightPanel() {
        RelativeLayout rightPanelLayout = new RelativeLayout(RelativeLayout.Y_AXIS);
        rightPanelLayout.setFill(true); // Fill components both horizontally AND vertically
        rightPanel.setLayout(rightPanelLayout);
        
        JPanel topRight = new JPanel(new BorderLayout()); // Use BorderLayout so that messages can cover entire width (by adding to North)
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        topRight.add(chatPanel, BorderLayout.NORTH);
        
        chatScrollPane = new JScrollPane(topRight);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(20); // Increase scroll speed
        
        rightPanel.add(chatScrollPane, 0.8f);
        
        RelativeLayout inputPanelLayout = new RelativeLayout();
        inputPanelLayout.setFill(true);
        JPanel inputPanel = new JPanel(inputPanelLayout);
        
        messageInput.setLineWrap(true);
        messageInput.setMargin(new Insets(5, 5, 5, 5)); // Add a margin around the text area
        
        inputPanel.add(new JScrollPane(messageInput), 0.8f);
        inputPanel.add(sendButton, 0.2f);
        
        rightPanel.add(inputPanel, 0.2f);
        return rightPanel;
    }

    @Override
    public AbstractButton getSendButton() { return sendButton; }

    @Override
    public JTextComponent getMessageInput() { return messageInput; }
    
    @Override
    public String getPlaceholderText() { return PLACEHOLDER_TEXT; }

    @Override
    public void addMember(Member newMember) {
        leftPanel.add(new MemberGUI(newMember));
        revalidateMembersList();
    }

    @Override
    public void clearMembersList() { leftPanel.removeAll(); }
    
    @Override
    public void revalidateMembersList() {
        if(leftPanel.getComponents().length == 0) {
            JPanel pnl = new JPanel();
            pnl.add(new JLabel("No members connected."));
            leftPanel.add(pnl);
        }
        leftPanel.revalidate();
    }

    @Override
    public void addMessage(String message, MessageType messageType) {
        JPanel messageWrapper = new JPanel();
        JTextArea messageText = new JTextArea(message);
        messageText.setLineWrap(true);
        messageText.setEditable(false);
        messageText.setMargin(new Insets(10, 10, 10, 10)); // Add a margin around the text area
        
        if(messageType == MessageType.SYSTEM) {
            messageWrapper.add(new JLabel(message));
        } else {
            messageWrapper.setLayout(new RelativeLayout());
            messageWrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            if(messageType == MessageType.OUTBOUND) messageWrapper.add(new JPanel(), 0.3f);
            messageWrapper.add(messageText, 0.7f);
            if(messageType == MessageType.INBOUND) messageWrapper.add(new JPanel(), 0.3f);
        }
        chatPanel.add(messageWrapper, BorderLayout.NORTH);
        chatPanel.revalidate();
        chatScrollToBottom();
        
        // Put back placeholder text if this member sent the message
        if(messageType == MessageType.OUTBOUND) {
            messageInput.setForeground(Color.gray);
            messageInput.setText(PLACEHOLDER_TEXT);
        }
    }
    
    /**
     * Method to allow automatic scrolling to the bottom of the chat when
     * a new message is received.
     */
    private void chatScrollToBottom() {
        JScrollBar verticalBar = chatScrollPane.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
    }
    
}
