package ChatRoom;

import ChatRoomGUI.MainGUI;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Class responsible for creating a new client for a member.
 * 
 * A member has two components: a server and a client.
 * The server is launched in a separate thread, and its job is to listen
 * for incoming messages from other members.
 * The client sends messages to other members.
 *
 * @author Abdullah
 */
public final class Client {
    
    protected final Member me; // This member's details
    protected boolean online = false; // Set to true when member connected to netowrk
    
    private final ClientGUI gui;
    private final ServerThread server; // Server
    private final List<Member> members; // List of members
    private final List<Member> unreachableMembers = Collections.synchronizedList(new ArrayList<>());
    private CoordinatorThread coordinatorThread = null;
    private boolean nextCoordinator = false; // Check if this member is the next coordinator
    private int newestMemberID = -1;
    private int oldestMemberID = -1; // Oldest member that is NOT a coordinator (i.e. next coordinator)
    
    
    /**
     * Creates a single client.
     * 
     * @param me Owner of this client
     * @param existingMemberAddress Host address of an existing member
     * @param existingMemberPort Port number of the existing member
     * @throws ChatRoom.PortNotAvailbleException
     * @throws ChatRoom.UnknownMemberException
     * @throws ChatRoom.InvalidUsernameException
     */
    public Client(Member me, String existingMemberAddress, int existingMemberPort) throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException {
        this.me = me;
        gui = new MainGUI();
        
        // Disable input until connected to server
        gui.getMessageInput().setEnabled(false);
        gui.getSendButton().setEnabled(false);
        
        // Add action listener to the "Send" button
        gui.getSendButton().addActionListener(e -> {
            System.out.println("Send button pressed");
                    String message = gui.getMessageInput().getText().trim();
            if(!message.equals(gui.getPlaceholderText())) sendMessage(message);
        });
        
        // Send messages when enter key is pressed
        gui.getMessageInput().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                // If enter key is pressed...
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // .. if SHIFT is held down, add new line
                    if(e.isShiftDown()) gui.getMessageInput().setText(gui.getMessageInput().getText() + "\n");
                    
                    // if SHIFT is NOT held down, send the message
                    else {
                        System.out.println("Enter key pressed");
                        String message = gui.getMessageInput().getText().trim();
                        if(!message.equals(gui.getPlaceholderText())) sendMessage(message);
                    }
                }
            }
        });
        
        // Start the server
        server = new ServerThread(this);
        server.start(); // Start server
        
        /**
         * CONNECT TO OTHER MEMBERS or CREATE A NEW NETWORK
         * 
         * The user must either enter a valid address:port combination of
         * an existing member, or leave the field empty to create a new network.
         */
        
        // Need to make members ArrayList final, so create a temporary ArrayList
        List<Member> tempMembers = Collections.synchronizedList(new ArrayList<>());

        // If input is empty, create new netwrok
        if(existingMemberAddress.isEmpty()){
            me.setID(++newestMemberID); // First member id = 0
            me.setCoordinator(); // Become coordinator
            coordinatorThread = new CoordinatorThread(this);
        } else {
            try {
                tempMembers = sendRequest(existingMemberAddress, existingMemberPort);
                postMessage("Connected!", MessageType.SYSTEM);
            } catch(ClassNotFoundException e) {
                System.out.println("Received invalid response.");
            }
        }
        members = tempMembers;
        online = true;
        
        // Connected, enable input
        gui.getMessageInput().setEnabled(true);
        gui.getSendButton().setEnabled(true);
        
        // Start CoordinatorThread is this member is the coordinator
        // Late start because otherwise "members" in null, so start after "members" has been assigned.
        if(coordinatorThread != null) coordinatorThread.start();
        updateMembersList();
    }
    
    /**
     * Method to retrieve the full list of members.
     * 
     * @return List of members.
     */
    protected synchronized List<Member> getMembers() {
        System.out.println("getMembers()");
        return members;
    }
    
    /**
     * Method to send a request to be added to an existing network.
     * 
     * @param address Host of another member
     * @param port Port of the other member
     * @return List of all members in the network
     * @throws ChatRoom.UnknownMemberException
     * @throws java.lang.ClassNotFoundException
     */
    private List<Member> sendRequest(String address, int port) throws UnknownMemberException, ClassNotFoundException, InvalidUsernameException {
        System.out.println("sendRequest(" + address + ", " + port + ")");
        try {
            postMessage("Sending request...", MessageType.SYSTEM);
            Socket conn = new Socket(address, port);
            ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
            out.writeObject(me);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
            List<Member> m = (List<Member>) in.readObject();
            conn.close();
            
            // Check if username is unique
            if(m.isEmpty()) throw new InvalidUsernameException("Username must be unique.");
            
            // Assign this member's ID
            for(Member member: m) {
                if(member.getID() > newestMemberID) newestMemberID = member.getID(); // Find the highest ID
            }
            me.setID(++newestMemberID);
            
            return m;
        } catch (IOException e) {
            throw new UnknownMemberException("Member at " + address + "does not exist.");
        }
    }
    
    /**
     * Method to handle incoming connection requests from new members trying to join the network.
     * 
     * @param newMember Member trying to join.
     * @param conn Connection to the member.
     */
    protected void incomingRequest(Member newMember, Socket conn) {
        System.out.println("incomingRequest(" + newMember + ", " + conn + ")");
        try {
            int ans = JOptionPane.showConfirmDialog(null, "Connection request from " + newMember.getUsername() + ". Add to list?");
            if(ans == JOptionPane.YES_OPTION) {
                ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                
                // New user's username MUST BE UNIQUE
                boolean usernameUnique = true;

                // Send list of all members in the network
                ArrayList<Member> everyone = new ArrayList<>();
                for(Member existingMember: getMembers()) {
                    // Check is username is already in use
                    if(existingMember.getUsername().toLowerCase().equals(newMember.getUsername().toLowerCase())) {
                        usernameUnique = false;
                        break;
                    }
                    everyone.add(existingMember);
                }
                everyone.add(me);
                if(me.getUsername().toLowerCase().equals(newMember.getUsername().toLowerCase())) {
                    usernameUnique = false;
                    everyone.clear();
                }
                
                out.writeObject(everyone);
                out.flush();
                conn.close();

                // Notify everyone of this new member
                globalAddMember(newMember);
            } else {
                postMessage("Ignoring...", MessageType.SYSTEM);
            }
        } catch(IOException e) {}
    }
    
    /**
     * Method to inform existing members about a new member.
     * 
     * @param newMember Details of the new member
     */
    protected void globalAddMember(Member newMember) {
        System.out.println("globalAddMember(" + newMember + ")");
        newMember.setID(++newestMemberID); // Assign new ID to the member
        sendCommand("newMember:"+newMember.getUsername()+":"+newestMemberID+":"+newMember.getAddress()+":"+newMember.getPort()); // FORMAT => username:id:address:port
        getMembers().add(newMember);
        updateMembersList();
        postMessage("Everyone notified of new member.", MessageType.SYSTEM);
    }
    
    /**
     * Method to inform the network about a member leaving the group.
     * 
     * @param id ID of member who left
     * @param userName Username of member who left
     */
    protected void globalRemoveMember(int id, String userName) {
        System.out.println("globalRemoveMember(" + id + ", " + userName + ")");
        getMembers().removeIf(m -> m.getID() == id); // Remove member
        updateMembersList();
        sendCommand("removeMember:"+id);
        postMessage("Member " + userName + " left.", MessageType.SYSTEM);
    }
    
    /**
     * Send a message to all members in the network.
     * 
     * @param message
     */
    protected void sendMessage(String message) {
        System.out.println("sendMessage(" + message + ")");
        postMessage(message, MessageType.OUTBOUND);
        sendMessage(message, false);
    }
    
    /**
     * Send a command to all members in the network.
     * 
     * @param command
     */
    protected void sendCommand(String command) {
        System.out.println("sendCommand(" + command + ")");
        sendMessage(command, true);
    }
    
    /**
     * Send a String to all members in the list.
     * 
     * @param string Message to be sent
     * @param isCommand Set to true if this is command
     */
    private void sendMessage(String string, boolean isCommand) {
        new MessagingThread(this, string, isCommand);
    }
    
    /**
     * Method to add messages to the chat area
     * 
     * @param message Message to be added to the chat
     * @param messageType Whether this message is a SYSTEM, INBOUND or OUTBOUND.
     */
    protected void postMessage(String message, MessageType messageType) {
        System.out.println("postMessage(" + message + ")");
        gui.addMessage(message, messageType);
    }
    
    /**
     * Method to update the side panel showing the list of members.
     */
    protected synchronized void updateMembersList() {
        System.out.println("updateMembersList()");
        gui.clearMembersList();
        
        // Reset lowest and highest ID
        oldestMemberID = me.getID();
        newestMemberID = me.getID();
        
        for(Member member: getMembers()) {
            if(member.getID() > newestMemberID) newestMemberID = member.getID(); // Find the highest ID
            else if(member.getID() < oldestMemberID && !member.isCoordinator()) oldestMemberID = member.getID(); // Find the lowest ID
            gui.addMember(member);
        }
        
        // Check if this member is the next coordinator
        if(me.getID() == oldestMemberID && !me.isCoordinator() && !nextCoordinator) {
            coordinatorThread = new CoordinatorThread(this);
            coordinatorThread.start();
            nextCoordinator = true;
        }
        
        System.out.println("Me: " + me.getID() + ", oldest: " + oldestMemberID + ", newest: " + newestMemberID);
        gui.revalidateMembersList();
    }
    
    /**
     * Method to get the list of all members this client is having trouble to connect to.
     * 
     * @return List of unreachable members
     */
    protected synchronized List<Member> getUnreachableMembers() {
        return unreachableMembers;
    }
    
    /**
     * Method to handle a member this client is having trouble connecting to.
     * 
     * @param m Member having issues to connect with.
     */
    protected synchronized void unreachableMember(Member m) {
        System.out.println("unreachableMember(" + m + ")");
        if(me.isCoordinator()) unreachableMembers.add(m);
        else {
            Member coordinator = members.get(0);
            try (Socket conn = new Socket(coordinator.getAddress(), coordinator.getPort())) {
                ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                out.writeObject("unreachable:" + m.getID());
                out.flush();
            } catch (IOException e) {
                System.out.println("error connecting to the coordinator: " + e);
            }
        }
    }
}
