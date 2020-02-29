package ChatRoom;

import javax.swing.AbstractButton;
import javax.swing.text.JTextComponent;

/**
 * Interface with methods that MUST be present for the GUI of this application.
 *
 * @author iAbdu
 */
public interface ClientGUI {
    public AbstractButton getSendButton();
    public JTextComponent getMessageInput();
    
    public void addMember(Member newMember);
    public void clearMembersList();
    public void revalidateMembersList();
    public void addMessage(String message, boolean myMessage);
}
