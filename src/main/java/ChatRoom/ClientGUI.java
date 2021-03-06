package ChatRoom;

import javax.swing.AbstractButton;
import javax.swing.text.JTextComponent;

/**
 * Interface with methods that MUST be present for the GUI of this application.
 *
 * @author iAbdu
 */
public interface ClientGUI {
    /**
     * Method to get the instance of the send button.
     * 
     * @return The send button.
     */
    public AbstractButton getSendButton();
    
    /**
     * Method to get the instance of the text component where user can type messages to be sent.
     * 
     * @return The text field.
     */
    public JTextComponent getMessageInput();
    
    /**
     * Method to get the placeholder text.
     * 
     * @return Placeholder text.
     */
    public String getPlaceholderText();
    
    /**
     * Method to add a member to the panel of members.
     * 
     * @param newMember Details of member to be added.
     */
    public void addMember(Member newMember);
    
    /**
     * Method to clear all entries from the panel of members.
     */
    public void clearMembersList();
    
    /**
     * Method to refresh the panel of members.
     */
    public void refreshMembersList();
    
    /**
     * Method to add a chat to the chat panel.
     * 
     * @param message The message to be added.
     * @param myMessage Whether the message is sent by the client.
     */
    public void addMessage(Message message, boolean myMessage);
    
    /**
     * Method to set the GUI's visibility.
     * 
     * @param visible Visibility setting
     */
    public void setVisibility(boolean visible);
    
    /**
     * Method to dispose of the GUI.
     */
    public void terminate();
}
