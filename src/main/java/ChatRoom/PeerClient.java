package ChatRoom;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

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
    private PeerServerThread server; // Server
    protected ArrayList<PeerMember> members = new ArrayList<>(); // List of members
    protected final PeerMember me; // This peer's details
    protected boolean online = true; // Sets server status (true = launch server, false = stop server)
    protected boolean messageIsReady = false; // Set to true when message is ready to be sent.
    
    // Swing components
    private final JTextArea messageInput;
    protected final JTextPane chat;
    private final JButton sendMessage;
    private final JTextField membersBar;
    
    /**
     * Creates a single client.
     * 
     * @param client Owner of this client
     * @param existingMemberAddress Host address of an existing member
     * @param existingMemberPort Port number of the existing member
     * @param messageInput TextArea to get message input from member
     * @param chat TextArea to display all messages
     * @param sendMessage Button to send a new message
     * @param membersBar Side bar where members are listed
     * @throws java.lang.Exception
     */
    public PeerClient(PeerMember client, String existingMemberAddress, int existingMemberPort, JTextArea messageInput, JTextPane chat, JButton sendMessage, JTextField membersBar) throws Exception {
        me = client;
        this.messageInput = messageInput;
        this.chat = chat;
        this.sendMessage = sendMessage;
        this.membersBar = membersBar;
        
        sendMessage.addActionListener((ActionEvent e) -> {
            String message = messageInput.getText();
            if(message.startsWith(">")) return;

            // Check if user typed a command
            if(message.equals("/help")) {
                System.out.println(
                        "> Available commands:\n>\n" +
                        "> /list\n" +
                        "> Lists all the members connected to the network\n>\n" +
                        "> /quit\n" +
                        "> Leave the chat\n>\n" +
                        "> /details [username]\n" +
                        "> Lists your details unless a username is specified");
            } else if(message.equals("/quit")) {
                System.out.println("User wants to quit");
                online = false;
            } else if(message.startsWith("/details")) {
                if(message.length() > 9) {  // i.e. /details username
                    boolean found = false;
                    String userName = message.substring(9);

                    for(PeerMember m: members) {
                        if(m.getUsername().equals(userName)) {
                            found = true;
                            System.out.println(m);
                            break;
                        }
                    }
                    if(!found) System.out.println("> Member does not exist");
                } else {
                    System.out.println(me);
                }
            } else if(message.equals("/list")) {
                System.out.println("> Your have " + members.size() + " member(s):");
                for(PeerMember m: members) {
                    System.out.println("> " + m.getUsername());
                }
            } else {
                // Not a command, send the message
                chat.setText(chat.getText() + "\n" + message); // Show message in chat
                sendMessage(message); // Send message to all members
            }
            messageInput.setText("");
        });
        
        initServer();
        initClient(existingMemberAddress, existingMemberPort);
    }
    
    /**
     * Initialise the server
     * 
     * @throws java.lang.Exception Port not available
     */
    private void initServer() throws Exception {
        server = new PeerServerThread(this);
        server.start();
    }
    
    private void initClient(String existingMemberAddress, int existingMemberPort) {
        
        /**
         * CONNECT TO OTHER PEERS or CREATE A NEW NETWORK
         * 
         * The user must either enter a valid address:port combination of
         * an existing member, or leave the field empty to create a new network.
         */

        // If input is empty, create new netwrok
        if(existingMemberAddress.isEmpty()){
            writeToChat("You're the coordinator");
            updateMembersList();
        } else {
            try {
                members = sendRequest(existingMemberAddress + ":" + existingMemberPort);
                writeToChat("Connected!");
                updateMembersList();
            } catch(IOException | ClassNotFoundException e) {
                writeToChat("Received invalid response.");
            }
        }
        
        try {
            // User wants to quit, wait for server thread to finish.
            server.join();
        } catch (InterruptedException e) {
            writeToChat("Server was interrupted");
        }
        
        writeToChat("Connection terminated.");
    }
    
    protected void updateMembersList() {
        if(members.isEmpty()) {
            membersBar.setText("No members connected");
            return;
        }
        String membersList = "";
        for(PeerMember m: members) membersList += m.getUsername() + "\n";
        membersBar.setText(membersList);
    }
    
    protected void writeToChat(String message) {
        chat.setText(chat.getText() + "\n" + message);
    }
    
    private ArrayList<PeerMember> sendRequest(String addressPortString) throws IOException, ClassNotFoundException {
        Socket conn = new Socket(addressPortString.split(":")[0], Integer.parseInt(addressPortString.split(":")[1]));
        ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
        out.writeObject(me);
        out.flush();
        ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
        ArrayList<PeerMember> m = (ArrayList<PeerMember>) in.readObject();
        conn.close();
        return m;
    }
    
    public void globalAddMember(PeerMember newMember) throws IOException {
        for(PeerMember m: members) {
            Socket conn = new Socket(m.getAddress(), m.getPort());
            ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
            out.writeObject("newMember:"+newMember.getUsername()+":"+newMember.getAddress()+":"+newMember.getPort());
            out.flush();
            conn.close();
        }
        members.add(newMember);
        updateMembersList();
        System.out.println("> Everyone notified of new member.");
    }
    
    /**
     * Send a message to all peers in the list.
     * 
     * @param message Message to be sent
     */
    public void sendMessage(String message) {
        for(PeerMember member: members) {
            try {
                Socket conn = new Socket(member.getAddress(), member.getPort());
                ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                out.writeObject(me.getUsername() + ": " + message);
                out.flush();
                conn.close();
            } catch (IOException e) {
                System.out.println("Member does not exist");
            }
        }
    }
}
