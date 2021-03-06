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
public class Client {
    
    protected final Member me; // This member's details
    protected boolean online = false; // Set to true when member connected to netowrk
    
    private final ClientGUI gui;
    private final boolean showGUI; // Whether GUI should be visible or not
    private final ServerThread server; // Server
    private final List<Member> members = Collections.synchronizedList(new ArrayList<>());; // List of members
    private final List<Member> unreachableMembers = Collections.synchronizedList(new ArrayList<>());
    private CoordinatorThread coordinatorThread = null;
    private boolean nextCoordinator = false; // Check if this member is the next coordinator
    private int newestMemberID = -1;
    private int oldestMemberID = -1; // Oldest member that is NOT a coordinator (i.e. next coordinator)
    
    private final ArrayList<Message> allMessages = new ArrayList<>();
    
    /**
     * Creates a single client.
     * 
     * @param me Owner of this client
     * @param showGUI Whether GUI should be visible.
     * @param existingMemberAddress Host address of an existing member
     * @param existingMemberPort Port number of the existing member
     * @throws ChatRoom.PortNotAvailbleException
     * @throws ChatRoom.UnknownMemberException
     * @throws ChatRoom.InvalidUsernameException
     */
    public Client(Member me, boolean showGUI, String existingMemberAddress, int existingMemberPort) throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException {
        this.me = me;
        this.showGUI = showGUI;
        gui = new MainGUI(me.getUsername());
        gui.setVisibility(showGUI);
        
        // Disable input until connected to server
        gui.getMessageInput().setEnabled(false);
        gui.getSendButton().setEnabled(false);
        
        // Add action listener to the "Send" button
        gui.getSendButton().addActionListener(e -> {
            System.out.println("Send button pressed");
            String message = gui.getMessageInput().getText().trim();
            if(!message.isEmpty() && !message.equals(gui.getPlaceholderText())) sendMessage(message);
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
                        if(!message.isEmpty() && !message.equals(gui.getPlaceholderText())) sendMessage(message);
                        else gui.getMessageInput().setText(""); // Pressing enter creates new line, remove it from text area.
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

        // If input is empty, create new netwrok
        if(existingMemberAddress.isEmpty()){
            me.setID(++newestMemberID); // First member id = 0
            me.setCoordinator(); // Become coordinator
            coordinatorThread = new CoordinatorThread(this);
            coordinatorThread.start();
        } else {
            try {
                for(Member m: sendRequest(existingMemberAddress, existingMemberPort)) members.add(m);
                postMessage(new Message(me.getUsername(), "Connected!", MessageType.SYSTEM));
            } catch(ClassNotFoundException e) {
                System.out.println("Received invalid response.");
            }
        }
        online = true;
        
        // Connected, enable input
        gui.getMessageInput().setEnabled(true);
        gui.getSendButton().setEnabled(true);
        
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
            postMessage(new Message(me.getUsername(), "Sending request...", MessageType.SYSTEM));
            Socket conn = new Socket(address, port);
            ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
            out.writeObject(me);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
            List<Member> m = (List<Member>) in.readObject();
            conn.close();
            
            // Check if username is unique
            if(m.isEmpty()) throw new InvalidUsernameException("Username must be unique.");
            
            // Find the highest ID
            newestMemberID = m.get(m.size()-1).getID();
            
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
            ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
            
            ArrayList<Member> everyone = new ArrayList<>();
            everyone.add(me);

            boolean usernameUnique = !newMember.getUsername().equals(me.getUsername()); // New user's username MUST BE UNIQUE
            
            // If the username is unique
            while(usernameUnique) {
                for(Member m: getMembers()) {
                    usernameUnique = !m.getUsername().equals(newMember.getUsername());
                    everyone.add(m);
                }
                everyone.sort((m1, m2) -> { return m1.getID() - m2.getID(); }); // Sort list

                // Notify everyone of this new member
                globalAddMember(newMember);
                break;
            }
            if(!usernameUnique) everyone.clear(); // Empty list means username is NOT unique

            out.writeObject(everyone);
            out.flush();
            conn.close();
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
        postMessage(new Message(me.getUsername(), "New member \"" + newMember.getUsername() + "\" joined the chat!", MessageType.SYSTEM));
    }
    
    /**
     * Method to inform the network about a member leaving the group.
     * 
     * @param id ID of member who left
     * @param userName Username of member who left
     */
    protected void globalRemoveMember(int id, String userName) {
        System.out.println("globalRemoveMember(" + id + ", " + userName + ")");
        for(Member m: getMembers()) {
            if(m.getID() == id) {
                getMembers().remove(m);
                postMessage(new Message(me.getUsername(), "Member " + userName + " left.", MessageType.SYSTEM));
                updateMembersList();
                break;
            }
        }
        sendCommand("removeMember:"+id);
    }
    
    /**
     * Send a message to all members in the network.
     * 
     * @param message
     */
    protected void sendMessage(String message) {
        System.out.println("sendMessage(" + message + ")");
        sendMessage(new Message(me.getUsername(), message, MessageType.MESSAGE));
    }
    
    /**
     * Send a command to all members in the network.
     * 
     * @param command
     */
    protected void sendCommand(String command) {
        System.out.println("sendCommand(" + command + ")");
        sendMessage(new Message(me.getUsername(), command, MessageType.COMMAND));
    }
    
    /**
     * Send a String to all members in the list.
     * 
     * @param string Message to be sent
     * @param isCommand Set to true if this is command
     */
    private void sendMessage(Message message) {
        new MessagingThread(this, message);
        if(message.getMessageType() == MessageType.MESSAGE) postMessage(message);
    }
    
    /**
     * Method to add messages to the chat area
     * 
     * @param message Message to be added to the chat
     */
    protected void postMessage(Message message) {
        System.out.println("postMessage(" + message + ")");
        if(!showGUI) allMessages.add(message); // Store messages into ArrayList if GUI is hidden.
        gui.addMessage(message, message.getUsername().equals(me.getUsername()));
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
        gui.refreshMembersList();
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
                out.writeObject(new Message(me.getUsername(), "unreachable:" + m.getID(), MessageType.COMMAND));
                out.flush();
            } catch (IOException e) {
                System.out.println("error connecting to the coordinator: " + e);
            }
        }
    }
    
    /**
     * Method to get all messages if GUI is hidden.
     */
    public ArrayList<Message> getAllMessages() { return allMessages; }
    
    /**
     * Method to terminate application.
     */
    public void quit() {
        System.out.println("Preparing to terminate application");
        server.stopThread();
        if(coordinatorThread != null) coordinatorThread.stopThread();
        gui.terminate();
    }
}
