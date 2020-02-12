package ChatRoom;

import ChatRoomGUI.GUI;
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
public class PeerClient {
    private GUI gui;
    private PeerServerThread server; // Server
    protected ArrayList<PeerMember> members = new ArrayList<>(); // List of members
    protected final PeerMember me; // This peer's details
    protected boolean messageIsReady = false; // Set to true when message is ready to be sent.
    private CoordinatorThread coordinatorThread = null;
    private boolean nextCoordinator = false; // Check if this peer is the next coordinator
    private int newestMemberID = -1;
    private int oldestMemberID = -1; // Oldest member that is NOT a coordinator (i.e. next coordinator)
    
    /**
     * Creates a single client.
     * 
     * @param client Owner of this client
     * @param existingMemberAddress Host address of an existing member
     * @param existingMemberPort Port number of the existing member
     * @throws ChatRoom.PortNotAvailbleException
     * @throws ChatRoom.UnknownMemberException
     */
    public PeerClient(PeerMember client, String existingMemberAddress, int existingMemberPort) throws PortNotAvailbleException, UnknownMemberException {
        me = client;
        gui = new GUI();
        
        gui.messageSendButton.addActionListener(e -> {
            sendMessage(gui.messageInput.getText(), false);
            gui.messageInput.setText("");
        });
        
        gui.setVisible(true);
        initServer();
        initClient(existingMemberAddress, existingMemberPort);
    }
    
    /**
     * Initialise the server
     * 
     * @throws java.lang.Exception Port not available
     */
    private void initServer() throws PortNotAvailbleException {
        server = new PeerServerThread(this);
        server.start();
    }
    
    private void initClient(String existingMemberAddress, int existingMemberPort) throws UnknownMemberException {
        
        /**
         * CONNECT TO OTHER PEERS or CREATE A NEW NETWORK
         * 
         * The user must either enter a valid address:port combination of
         * an existing member, or leave the field empty to create a new network.
         */

        // If input is empty, create new netwrok
        if(existingMemberAddress.isEmpty()){
            me.setID(++newestMemberID); // First member id = 0
            me.setCoordinatorStatus(true); // Become coordinator
            coordinatorThread = new CoordinatorThread(this);
        } else {
            try {
                members = sendRequest(existingMemberAddress + ":" + existingMemberPort);
                updateMembersList();
                
                postMessage("Connected!");
            } catch(ClassNotFoundException e) {
                System.out.println("Received invalid response.");
            }
        }
    }
    
    private ArrayList<PeerMember> sendRequest(String addressPortString) throws UnknownMemberException, ClassNotFoundException {
        try {
            Socket conn = new Socket(addressPortString.split(":")[0], Integer.parseInt(addressPortString.split(":")[1]));
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
            throw new UnknownMemberException("Member at " + addressPortString + "does not exist.");
        }
    }
    
    public void globalAddMember(PeerMember newMember) {
        newMember.setID(++newestMemberID); // Assign new ID to the member
        sendMessage("newMember:"+newMember.getUsername()+":"+newestMemberID+":"+newMember.getAddress()+":"+newMember.getPort(), true); // FORMAT => username:id:address:port
        members.add(newMember);
        updateMembersList();
        postMessage("> Everyone notified of new member.");
    }
    
    public void globalRemoveMember(int id, String userName) {
        members.removeIf(m -> m.getID() == id); // Remove member
        updateMembersList();
        String message = me.getUsername() + ": " + "Member " + userName + " left.";
        postMessage(message);
        sendMessage("removeMember:"+id, true);
    }
    
    /**
     * Send a message to all peers in the list.
     * 
     * @param message Message to be sent
     * @param command Set to true if this is command
     */
    public void sendMessage(String message, boolean command) {
        if(!command) postMessage(message); // Normal message, show it to the sender
        for(PeerMember member: members) {
            try {
                Socket conn = new Socket(member.getAddress(), member.getPort());
                ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                out.writeObject(command? message : me.getUsername() + ": " + message);
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
    
    protected void updateMembersList() {
        gui.membersList.removeAll();
        
        // Reset lowest and highest ID
        oldestMemberID = me.getID();
        newestMemberID = me.getID();
        
        for(PeerMember member: members) {
            if(member.getID() > newestMemberID) newestMemberID = member.getID(); // Find the highest ID
            else if(member.getID() < oldestMemberID && !member.isCoordinator()) oldestMemberID = member.getID(); // Find the lowest ID
            MemberGUI m = new MemberGUI(member.getUsername() + "-" + member.getID());
            gui.membersList.add(m);
        }
        
        // Check if this peer is the next coordinator
        if(me.getID() == oldestMemberID && !me.isCoordinator() && !nextCoordinator) {
            coordinatorThread = new CoordinatorThread(this);
            nextCoordinator = true;
        }
        
        gui.revalidate();
    }
}
