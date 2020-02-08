package ChatRoomGUI;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Abdullah
 */
public class MemberGUI extends JPanel {
    
    public MemberGUI(String memberName) {
        init(memberName);
    }
    
    private void init(String memberName) {
        setPreferredSize(new Dimension(0, 50));
        setMaximumSize(new Dimension(5000, 50));
        add(new JLabel(memberName));
        setVisible(true);
    }
}
