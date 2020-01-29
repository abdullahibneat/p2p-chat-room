package ChatRoom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

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
    PeerServerThread server; // Server
    ArrayList<PeerMember> members = new ArrayList<>(); // List of members
    PeerMember me; // This peer's details
    boolean online = true; // Sets server status (true = launch server, false = stop server)
    
    public static void main(String[] args) {
        try {
            PeerClient p = new PeerClient();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Creates a single client.
     * 
     * @throws java.lang.Exception
     */
    public PeerClient() throws Exception {
        
        // Get user input from console.
        Scanner input = new Scanner(System.in);
        
        /**
         * 1. SELECT A USERNAME
         * 
         * Get a username from the user.
         * It must not be empty or contain spaces.
         */
        while(true) {
            System.out.print("> Enter a username: ");
            String userName = input.nextLine();
            if(!(userName.isEmpty() || userName.contains(" "))) {
                me = new PeerMember(userName);
                break;
            }
            System.out.println("> Username cannot be empty or contain spaces.");
        }
        
        /**
         * 2. SELECT A PORT
         * 
         * Select a port where the server will listen.
         * Must be available.
         */
        while(true) {
            try {
                System.out.print("> Enter a port: ");
                me.port = Integer.parseInt(input.nextLine());
                server = new PeerServerThread(this); // Start server
                server.start();
                break;
            } catch(NumberFormatException e) {
                System.out.println("> Port must be an integer number.");
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        /**
         * 3. CONNECT TO OTHER PEERS / CREATE A NEW NETWORK
         * 
         * The user must either enter a valid address:port combination of
         * an existing member, or leave the field empty to create a new network.
         */
        while(true) {
            System.out.print("> Enter the address:port of an existing member: ");
            String existingMember = input.nextLine();
            
            // If input is empty, create new netwrok
            if(existingMember.isEmpty()){
                System.out.println("> You're the coordinator");
                break;
            }
            
            try {
                PeerMember m = sendRequest(existingMember);
                members.add(m);
                System.out.println("> Connected!");
                break;
            } catch(IOException | ClassNotFoundException e) {
                System.out.println("> " + e.getMessage());
            }
        }
        
        /**
         * 4. SEND MESSAGES
         * 
         * This is the actual client. Type a message to send to other peers,
         * or type special commands to perform certain actions.
         */
        while(true) {
            String message = input.nextLine();
            if(message.startsWith(">")) continue;
            
            // Check if user typed a command
            if(message.equals("/help")) {
                System.out.println(
                        "> Available commands:\n>\n" +
                        "> /add ADDRESS:PORT\n" +
                        "> Adds a member to your list of members\n>\n" +
                        "> /quit\n" +
                        "> Leave the chat\n>\n" +
                        "> /details [username]\n" +
                        "> Lists your details unless a username is specified");
            } else if(message.equals("/quit")) {
                online = false;
                break;
            } else if (message.startsWith("/add ")) {
                try {
                    System.out.println("> TODO: AddMember");
                } catch(Exception e) {
                    System.out.println("> " + e.getMessage());
                }
            } else if(message.startsWith("/details")) {
                if(message.length() > 9) {  // i.e. /details username
                    boolean found = false;
                    String userName = message.substring(9);
                    
                    for(PeerMember m: members) {
                        if(m.userName.equals(userName)) {
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
                    System.out.println("> " + m.userName);
                }
            } else {
                // Not a command, send the message
                sendMessage(message);
            }
        }
        
        // User wants to quit, wait for server thread to finish.
        server.join();
        
        System.out.println("Connection terminated.");
    }
    
    private PeerMember sendRequest(String addressPortString) throws IOException, ClassNotFoundException {
        Socket conn = new Socket(addressPortString.split(":")[0], Integer.parseInt(addressPortString.split(":")[1]));
        ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
        out.writeObject(me);
        out.flush();
        ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
        PeerMember m = (PeerMember) in.readObject();
        conn.close();
        return m;
    }
    
    public void globalAddMember(PeerMember newMember) throws IOException {
        for(PeerMember m: members) {
            if(!m.userName.equals(newMember.userName)) {
                Socket conn = new Socket(m.address, m.port);
                ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                out.writeObject("newMember:"+m.userName+":"+m.address+":"+m.port);
                out.flush();
                conn.close();
            }
        }
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
                Socket conn = new Socket(member.address, member.port);
                ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                out.writeObject(me.userName + ": " + message);
                out.flush();
                conn.close();
            } catch (IOException e) {
                System.out.println("Member does not exist");
            }
        }
    }
}
