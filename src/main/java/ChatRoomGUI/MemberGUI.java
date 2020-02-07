package ChatRoomGUI;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Abdullah
 */
public class MemberGUI extends JPanel {
    
    private final JLabel memberName = new JLabel("Member Name Here");
    
    public MemberGUI() {
        init();
    }
    
    private void init() {
        setPreferredSize(new Dimension(0, 50));
        setMaximumSize(new Dimension(5000, 50));
        add(memberName);
        setVisible(true);
    }
    
    public void setMemberName(String name) {
        memberName.setText(name);
        revalidate();
        repaint();
    }
    
    public JLabel getMemberName() {
        return memberName;
    }
}
