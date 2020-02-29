package ChatRoomGUI;

import ChatRoom.Member;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A JPanel component to show details of members in the GUI.
 *
 * @author Abdullah
 */
public class MemberGUI extends JPanel {
    
    public MemberGUI(Member member) {
        init(member);
    }
    
    private void init(Member member) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        String isCoordinator =  member.isCoordinator()? " (Coordinator)" : "";
        add(new JLabel("<html><h3>" + member.getUsername() + isCoordinator + "</h3></html>"));
        add(new JLabel("" + member.getID()));
        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 0));
        setVisible(true);
    }
}
