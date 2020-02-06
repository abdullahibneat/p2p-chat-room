package ChatRoom;

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
            String host = JOptionPane.showInputDialog("Other Member host");
            int port = Integer.parseInt(JOptionPane.showInputDialog("Other member port"));
            PeerClient c = new PeerClient(m, host, port);
        } catch (InvalidUsernameException ex) {
            System.out.println("Invalid username.");
        } catch (NoInternetException ex) {
            System.out.println("No internet.");
        } catch (PortNotAvailbleException ex) {
            System.out.println("Port not available.");
        } catch (UnknownMemberException ex) {
            System.out.println("Could not connect to member.");
        }
    }
}
