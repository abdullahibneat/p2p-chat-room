package ChatRoom;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Class to store details of each member.
 *
 * @author Abdullah
 */
public class Member implements Serializable {
    private int id = -1;
    private String userName;
    private final String address;
    private final int port;
    private boolean coordinator = false;
    
    /**
     * Initialise a member knowing their username and port, and figure out the address.
     * 
     * @param userName Username
     * @param port Port
     * @throws ChatRoom.InvalidUsernameException
     * @throws java.net.UnknownHostException
     */
    public Member(String userName, int port) throws InvalidUsernameException, UnknownHostException {
        // Check username format
        if(!(userName.isEmpty() || userName.contains(" "))) {
            this.userName = userName;
        } else throw new InvalidUsernameException("Username cannot be empty or contain spaces.");

        this.port = port;

        // Get this member's host address
        this.address = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Address: " + this.address
        );
    }
    
    /**
     * Initialise a member knowing their username, address and port.
     * 
     * @param userName Username
     * @param id Unique ID
     * @param address Host address
     * @param port Port
     */
    public Member(String userName, int id, String address, int port) {
        this.userName = userName;
        this.id = id;
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
     * Get the ID
     * 
     * @return id
     */
    public int getID() { return id; }
    
    /**
     * Set the ID
     * Only allowed on first creation.
     * 
     * @param id Unique ID
     */
    public void setID(int id) {
        if(this.id == -1) this.id = id;
    }
    
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
     * Set coordinator status
     * Once coordinator is set, it can't be reverted.
     */
    public void setCoordinator() {
        coordinator = true;
    }
    
    /**
     * Get coordinator status
     * 
     * @return Coordinator status
     */
    public boolean isCoordinator() { return coordinator; }
    
    /**
     * Display member details on screen
     * 
     * @return Details as string.
     */
    @Override
    public String toString() {
        return "Member{id: " + id + ", username: " + userName + ", address: " + address + ", port: " + port + ", isCoordinator: " + coordinator + "}";
    }
}
