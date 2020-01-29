package ChatRoom;

import java.io.Serializable;

/**
 * Class to store details of each peer.
 *
 * @author Abdullah
 */
public class PeerMember implements Serializable {
    String userName;
    String address;
    int port;
    
    /**
     * Initialise a peer using their username.
     * 
     * @param userName Username of peer
     */
    public PeerMember(String userName) {
        this.userName = userName;
    }
    
    /**
     * Initialise a peer knowing their address and port.
     * 
     * @param address Host address
     * @param port Port
     */
    public PeerMember(String address, int port) {
        this.address = address;
        this.port = port;
    }
    
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
