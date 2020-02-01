package ChatRoom;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Class to store details of each peer.
 *
 * @author Abdullah
 */
public class PeerMember implements Serializable {
    private String userName;
    private final String address;
    private final int port;
    
    /**
     * Initialise a peer knowing their username and port, and figure out the address.
     * 
     * @param userName Username
     * @param port Port
     * @throws ChatRoom.InvalidUsernameException
     * @throws ChatRoom.NoInternetException
     */
    public PeerMember(String userName, int port) throws InvalidUsernameException, NoInternetException {
        
        try {
            // Check username format
            if(!(userName.isEmpty() || userName.contains(" "))) {
                this.userName = userName;
            } else throw new InvalidUsernameException("Username cannot be empty or contain spaces.");
            
            this.port = port;
            
            // Get this peer's host address
            Socket s = new Socket();
            s.connect(new InetSocketAddress("google.com", 80));
            this.address = s.getLocalAddress().toString().substring(1); // Substring to remove the "/" from the front
        } catch (IOException ex) {
            throw new NoInternetException("Could not get this machine's host address automatically.");
        }
    }
    
    /**
     * Initialise a peer knowing their username, address and port.
     * 
     * @param userName Username
     * @param address Host address
     * @param port Port
     */
    public PeerMember(String userName, String address, int port) {
        this.userName = userName;
        this.address = address;
        this.port = port;
    }
    
    /**
     * Get the username
     * 
     * @return username
     */
    public String getUsername() { return userName; }
    
    /**
     * Get the host address
     * 
     * @return address
     */
    public String getAddress() { return address; }
    
    /**
     * Get the port
     * 
     * @return port
     */
    public int getPort() { return port; }
    
    /**
     * Display member details on screen
     * 
     * @return Details as string.
     */
    @Override
    public String toString() {
        return      "> Details:"
                + "\n>    Username: " + userName
                + "\n>    Address:  " + address
                + "\n>    Port:     " + port;
    }
}
