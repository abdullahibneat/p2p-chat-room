package ChatRoomGUI;

import ChatRoom.ClientGUI;
import ChatRoom.Member;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
    private final JPanel leftPanel = new JPanel();
    private final JPanel rightPanel = new JPanel();
    private final JPanel chatPanel = new JPanel();
    private final JButton sendButton = new JButton("Send");
    private final JTextArea messageInput = new JTextArea();
    
    public MainGUI() {
        init();
    }
    
    private void init() {
        setTitle("Chat Room");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 500));
        
        // Configure JSplitPane
        sp.setOneTouchExpandable(true);
        sp.setDividerLocation(250);
        sp.setLeftComponent(initializeLeftPanel());
        sp.setRightComponent(initializeRightPanel());
        
        add(sp);
        
        setVisible(true);
    }
    
    private JScrollPane initializeLeftPanel() {
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        JScrollPane sp = new JScrollPane(leftPanel);
        sp.setMinimumSize(new Dimension(250, 0)); // Same as initial divider location of the JSplitPane
        return sp;
    }
    
    private JPanel initializeRightPanel() {
        RelativeLayout rightPanelLayout = new RelativeLayout(RelativeLayout.Y_AXIS);
        rightPanelLayout.setFill(true);
        rightPanel.setLayout(rightPanelLayout);
        
        JPanel topRight = new JPanel(new BorderLayout()); // Use BorderLayout so that messages can cover entire width (by adding to North)
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        topRight.add(chatPanel, BorderLayout.NORTH);
        
        rightPanel.add(new JScrollPane(topRight), 0.8f);
        
        RelativeLayout inputPanelLayout = new RelativeLayout();
        inputPanelLayout.setFill(true);
        JPanel inputPanel = new JPanel(inputPanelLayout);
        
        messageInput.setLineWrap(true);
        messageInput.setMargin(new Insets(5, 5, 5, 5));
        
        inputPanel.add(new JScrollPane(messageInput), 0.8f);
        inputPanel.add(sendButton, 0.2f);
        
        rightPanel.add(inputPanel, 0.2f);
        return rightPanel;
    }

    @Override
    public AbstractButton getSendButton() {
        return sendButton;
    }

    @Override
    public JTextComponent getMessageInput() {
        return messageInput;
    }

    @Override
    public void addMember(Member newMember) {
        String level =  newMember.isCoordinator()? "coordinator" : "member";
        leftPanel.add(new MemberGUI(newMember.getUsername() + "-" + newMember.getID() + "-" + level));
    }

    @Override
    public void clearMembersList() {
        leftPanel.removeAll();
    }
    
    @Override
    public void revalidateMembersList() {
        leftPanel.revalidate();
    }

    @Override
    public void addMessage(String message, boolean myMessage) {
        JPanel messageWrapper = new JPanel(new RelativeLayout());
        messageWrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JTextArea messageText = new JTextArea(message);
        messageText.setLineWrap(true);
        messageText.setEditable(false);
        if(myMessage) messageWrapper.add(new JPanel(), 0.3f);
        messageWrapper.add(messageText, 0.7f);
        if(!myMessage) messageWrapper.add(new JPanel(), 0.3f);
        chatPanel.add(messageWrapper, BorderLayout.NORTH);
        chatPanel.revalidate();
    }
    
}
