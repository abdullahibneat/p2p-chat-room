package ChatRoom;

import java.io.Serializable;

/**
 * Class to store details of each peer.
 *
 * @author Abdullah
 */
public class PeerMember implements Serializable {
    private String userName;
    private String address;
    private int port;
    
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
     * Set the username
     * 
     * @param userName
     */
    public void setUsername(String userName) { this.userName = userName; }
    
    /**
     * Get the username
     * 
     * @return username
     */
    public String getUsername() { return userName; }
    
    /**
     * Set the host address
     * 
     * @param address
     */
    public void setAddress(String address) { this.address = address; }
    
    /**
     * Get the host address
     * 
     * @return address
     */
    public String getAddress() { return address; }
    
    /**
     * Set the port
     * 
     * @param port
     */
    public void setPort(int port) { this.port = port; }
    
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
