package ChatRoom;

import ChatRoomGUI.MainGUI;
import ChatRoomGUI.MemberGUI;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;

/**
 * Class responsible for creating a new peer.
 * 
 * A peer has two components: a server and a client.
 * The server is launched in a separate thread, and its job is to listen
 * for incoming messages from other peers.
 * The client sends messages to other peers. This is built in into this
 * class.
 *
 * @author Abdullah
 */
public final class PeerClient {
    private MainGUI gui;
    private PeerServerThread server; // Server
    protected ArrayList<PeerMember> members = new ArrayList<>(); // List of members
    protected final PeerMember me; // This peer's details
    protected boolean online = false; // Set to true when peer connected to netowrk
    private CoordinatorThread coordinatorThread = null;
    private boolean nextCoordinator = false; // Check if this peer is the next coordinator
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
     */
    public PeerClient(PeerMember me, String existingMemberAddress, int existingMemberPort) throws PortNotAvailbleException, UnknownMemberException {
        this.me = me;
        gui = new MainGUI();
        
        // Add action listener to the "Send" button
        gui.messageSendButton.addActionListener(e -> {
            sendMessage(gui.messageInput.getText());
            gui.messageInput.setText("");
        });
        
        gui.setVisible(true);
        
        // Start the server
        server = new PeerServerThread(this);
        
        /**
         * CONNECT TO OTHER PEERS or CREATE A NEW NETWORK
         * 
         * The user must either enter a valid address:port combination of
         * an existing member, or leave the field empty to create a new network.
         */

        // If input is empty, create new netwrok
        if(existingMemberAddress.isEmpty()){
            me.setID(++newestMemberID); // First member id = 0
            me.setCoordinator(); // Become coordinator
            coordinatorThread = new CoordinatorThread(this);
        } else {
            try {
                members = sendRequest(existingMemberAddress, existingMemberPort);
                updateMembersList();
                postMessage("Connected!");
            } catch(ClassNotFoundException e) {
                System.out.println("Received invalid response.");
            }
        }
        online = true;
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
    private ArrayList<PeerMember> sendRequest(String address, int port) throws UnknownMemberException, ClassNotFoundException {
        try {
            postMessage("Sending request...");
            Socket conn = new Socket(address, port);
            ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
            out.writeObject(me);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
            ArrayList<PeerMember> m = (ArrayList<PeerMember>) in.readObject();
            conn.close();
            
            // Assign this peer's ID
            for(PeerMember member: m) {
                if(member.getID() > newestMemberID) newestMemberID = member.getID(); // Find the highest ID
            }            
            me.setID(++newestMemberID);
            
            return m;
        } catch (IOException e) {
            throw new UnknownMemberException("Member at " + address + "does not exist.");
        }
    }
    
    /**
     * Method to inform existing members about a new member.
     * 
     * @param newMember Details of the new member
     */
    public void globalAddMember(PeerMember newMember) {
        newMember.setID(++newestMemberID); // Assign new ID to the member
        sendCommand("newMember:"+newMember.getUsername()+":"+newestMemberID+":"+newMember.getAddress()+":"+newMember.getPort()); // FORMAT => username:id:address:port
        members.add(newMember);
        updateMembersList();
        postMessage("> Everyone notified of new member.");
    }
    
    /**
     * Method to inform the network about a member leaving the group.
     * 
     * @param id ID of member who left
     * @param userName Username of member who left
     */
    public void globalRemoveMember(int id, String userName) {
        members.removeIf(m -> m.getID() == id); // Remove member
        updateMembersList();
        sendCommand("removeMember:"+id);
        sendMessage("Member " + userName + " left.");
    }
    
    /**
     * Send a message to all members in the network.
     * 
     * @param message
     */
    public void sendMessage(String message) { sendMessage(message, false); }
    
    /**
     * Send a command to all members in the network.
     * 
     * @param command
     */
    public void sendCommand(String command) { sendMessage(command, true); }
    
    /**
     * Send a String to all peers in the list.
     * 
     * @param string Message to be sent
     * @param isCommand Set to true if this is command
     */
    private void sendMessage(String string, boolean isCommand) {
        if(!isCommand) postMessage(string); // Normal message, show it to the sender
        for(PeerMember member: members) {
            try {
                Socket conn = new Socket(member.getAddress(), member.getPort());
                ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                out.writeObject(isCommand? string : me.getUsername() + ": " + string);
                out.flush();
                conn.close();
            } catch (IOException e) {
                System.out.println("Could not send message to member " + member.getUsername());
            }
        }
    }
    
    /**
     * Method to add messages to the chat area
     * 
     * @param message Message to be added to the chat
     */
    protected void postMessage(String message) {
        gui.chatPanel.add(new JLabel(message));
        gui.revalidate();
    }
    
    /**
     * Method to update the side panel showing the list of members.
     */
    protected void updateMembersList() {
        gui.membersList.removeAll();
        
        // Reset lowest and highest ID
        oldestMemberID = me.getID();
        newestMemberID = me.getID();
        
        for(PeerMember member: members) {
            if(member.getID() > newestMemberID) newestMemberID = member.getID(); // Find the highest ID
            else if(member.getID() < oldestMemberID && !member.isCoordinator()) oldestMemberID = member.getID(); // Find the lowest ID
            String level = member.isCoordinator()? "coordinator" : "member";
            MemberGUI m = new MemberGUI(member.getUsername() + "-" + member.getID() + "-" + level);
            gui.membersList.add(m);
        }
        
        // Check if this peer is the next coordinator
        if(me.getID() == oldestMemberID && !me.isCoordinator() && !nextCoordinator) {
            coordinatorThread = new CoordinatorThread(this);
            nextCoordinator = true;
        }
        
        gui.membersList.revalidate();
        gui.membersList.repaint();
    }
}
