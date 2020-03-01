package ChatRoomGUI;

import ChatRoom.Member;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A JPanel component to show details of members in the GUI.
 *
 * @author Abdullah
 */
public class MemberGUI extends JPanel {
    
    private final Member member;
    
    public MemberGUI(Member member) {
        this.member = member;
        init();
        
        // Add mouse listener to show details when panel is clicked
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showInfo();
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
    }
    
    /**
     * Initialise the panel
     */
    private void init() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        String isCoordinator =  member.isCoordinator()? " (Coordinator)" : "";
        add(new JLabel("<html><h3>" + member.getUsername() + isCoordinator + "</h3></html>"));
        add(new JLabel("" + member.getID()));
        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 0));
        setVisible(true);
    }
    
    /**
     * Display member details when panel is clicked.
     */
    private void showInfo() {
        JOptionPane.showMessageDialog(this, "Username: " + member.getUsername()
                                        + "\nID: " + member.getID()
                                        + "\nAddress: " + member.getAddress()
                                        + "\nPort: " + member.getPort()
                                        + "\nGroup: " + (member.isCoordinator() ? "coordinator" : "member"));
    }
}
