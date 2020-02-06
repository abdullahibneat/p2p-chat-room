package ChatRoom;

import ChatRoomGUI.GUI;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

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
    protected boolean online = true; // Sets server status (true = launch server, false = stop server)
    protected boolean messageIsReady = false; // Set to true when message is ready to be sent.
    
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
            sendMessage(gui.messageInput.getText());
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
            postMessage("You're the coordinator");
        } else {
            try {
                members = sendRequest(existingMemberAddress + ":" + existingMemberPort);
                postMessage("Connected!");
            } catch(ClassNotFoundException e) {
                System.out.println("Received invalid response.");
            }
        }
        
        try {
            // User wants to quit, wait for server thread to finish.
            server.join();
        } catch (InterruptedException e) {
            System.out.println("Server was interrupted");
        }
        
        System.out.println("Connection terminated.");
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
            return m;
        } catch (IOException e) {
            throw new UnknownMemberException("Member at " + addressPortString + "does not exist.");
        }
    }
    
    public void globalAddMember(PeerMember newMember) {
        for(PeerMember m: members) {
            try {
                Socket conn = new Socket(m.getAddress(), m.getPort());
                ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                out.writeObject("newMember:"+newMember.getUsername()+":"+newMember.getAddress()+":"+newMember.getPort());
                out.flush();
                conn.close();
            } catch (IOException ex) {
                System.out.println("Could not send message to member " + m.getUsername());
            }
        }
        members.add(newMember);
        postMessage("> Everyone notified of new member.");
    }
    
    /**
     * Send a message to all peers in the list.
     * 
     * @param message Message to be sent
     */
    public void sendMessage(String message) {
        postMessage(message);
        for(PeerMember member: members) {
            try {
                Socket conn = new Socket(member.getAddress(), member.getPort());
                ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                out.writeObject(me.getUsername() + ": " + message);
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
        gui.chatText.setText(gui.chatText.getText() + "\n" + message);
    }
}
