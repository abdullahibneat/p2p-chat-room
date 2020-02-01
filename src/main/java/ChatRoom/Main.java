package ChatRoom;

import ChatRoomGUI.GUI;
import javax.swing.JOptionPane;

/**
 * Main class
 *
 * @author Abdullah
 */
public class Main {
    public static void main(String[] args) {
        try {
            String myUsername = JOptionPane.showInputDialog("My username");
            int myPort = Integer.parseInt(JOptionPane.showInputDialog("My port"));
            PeerMember m = new PeerMember(myUsername, myPort);
            GUI gui = new GUI();
            gui.setVisible(true);
            String host = JOptionPane.showInputDialog("Other Member host");
            int port = Integer.parseInt(JOptionPane.showInputDialog("Other member port"));
            PeerClient c = new PeerClient(m, host, port, gui.messageInput, gui.chatText, gui.messageSendButton);
        } catch (Exception ex) {
            System.out.println("Port not available.");
        }
    }
}
