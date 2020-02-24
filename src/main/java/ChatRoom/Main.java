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
            Member m = new Member(myUsername, myPort);
            String host = JOptionPane.showInputDialog("Other Member host");
            int port = host.isEmpty()? 0 : Integer.parseInt(JOptionPane.showInputDialog("Other member port")); // Show port dialog only if host is specified.
            Client c = new Client(m, host, port);
        } catch (InvalidUsernameException ex) {
            JOptionPane.showMessageDialog(null, "Invalid username." + ex);
            System.exit(0);
        } catch (NoInternetException ex) {
            JOptionPane.showMessageDialog(null, "No internet.");
            System.exit(0);
        } catch (PortNotAvailbleException ex) {
            JOptionPane.showMessageDialog(null, "Port not available.");
            System.exit(0);
        } catch (UnknownMemberException ex) {
            JOptionPane.showMessageDialog(null, "Could not connect to member, or request was denied.");
            System.exit(0);
        }
    }
}
