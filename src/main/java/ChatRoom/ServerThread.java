package ChatRoom;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server component for a client.
 * 
 * Allows a member to receive messages from others.
 *
 * @author Abdullah
 */
public class ServerThread extends Thread {
    
    private final Client client;
    private final ServerSocket server;
    
    /**
     * Initialise a server thread for a given client.
     * 
     * @param c Client this server should be bound to
     * @throws ChatRoom.PortNotAvailbleException
     */
    public ServerThread(Client c) throws PortNotAvailbleException {
        client = c;
        try {
            // Create a server
            server = new ServerSocket(client.me.getPort());
            client.postMessage("> Share your ADDRESS:PORT with other members: " + c.me.getAddress() + ":" + c.me.getPort(), true);
        } catch (IOException e) {
            throw new PortNotAvailbleException("Port not available, try another port.");
        }
    }
    
    @Override
    public void run() {            
        // Create a pool of threads, to enable multiple members to communicate
        // at the same time
        ExecutorService pool = Executors.newFixedThreadPool(500);
        while(true) {
            try {
                pool.execute(new Handler(client, server.accept()));
            } catch (IOException e) {
                // Connection went wrong.
            }
        }
    }
}

/**
 * This handler creates a new thread every time a connection request is received.
 * Allows multiple parallel connections.
 */
class Handler implements Runnable {
    
    private final Client client;
    private final Socket conn;
    
    public Handler(Client c, Socket conn) {
        this.client = c;
        this.conn = conn;
    }

    /**
     * Server
     * 
     * Listen for incoming connections from other members.
     */
    @Override
    public void run() {
        try {
            // Receive object from other member
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
            Object obj = in.readObject();

            // If string, it could be a normal message or command.
            if(obj instanceof String) {
                String message = (String)obj;

                if(message.startsWith("newMember")) newMember(message); // New member joined the network, add them to the list. FORMAT => newMember:username:id:address:port
                else if(message.startsWith("removeMember")) removeMember(Integer.parseInt(message.split(":")[1])); // Someone left the group, remove them from the list
                else if(message.startsWith("newCoordinator")) newCoordinator(Integer.parseInt(message.split(":")[1])); // Coordinator changed, update the list.
                else if(message.startsWith("unreachable")) unreachableMember(Integer.parseInt(message.split(":")[1])); // Unreachable member
                else client.postMessage(message, false); // Normal chat message
                
                client.updateMembersList();
            }
            // If Member, someone is trying to join the network
            else if(obj instanceof Member) client.incomingRequest((Member)obj, conn);
            
            conn.close();
        } catch(EOFException e) {
            // Someone is pinging, no file received.
        } catch (IOException e) {
            // Connection went wrong.
        } catch (ClassNotFoundException e) {
            System.out.println("!ClassNotFound: " + e);
        }
    }
    
    /**
     * Method to add a new member to the list of members.
     * 
     * @param newMemberString Details of member in the format newMember:username:id:address:port
     */
    private void newMember(String newMemberString) {
        String[] newMemberArr = newMemberString.substring(10).split(":"); // Remove "newMember:", FORMAT => username:id:address:port
        String userName = newMemberArr[0];
        int id = Integer.parseInt(newMemberArr[1]);
        String address = newMemberArr[2];
        int port = Integer.parseInt(newMemberArr[3]);
        
        // Because of multiple threads, it's possible that when a member joins the chat, the
        // same member receives a request to add himself to the chat.
        if(id != client.me.getID()) {
            client.postMessage("New member \"" + newMemberArr[0] + "\" joined!", false);
            client.getMembers().add(new Member(userName, id, address, port));
        }
    }
    
    /**
     * Method to remove a member.
     * 
     * @param id ID of the member to be removed.
     */
    private void removeMember(int id) {
        client.getMembers().removeIf(m -> m.getID() == id); // Find and remove member
    }
    
    /**
     * Method to set a new coordinator.
     * 
     * @param id ID of the new coordinator.
     */
    private void newCoordinator(int id) {
        for(Member m: client.getMembers()) {
            if(m.getID() == id) {
                m.setCoordinator();
                client.postMessage(m.getUsername() + " is the new coordinator!", false);
                break;
            }
        }
    }
    
    /**
     * Method to get the details of an unreachable member.
     * 
     * @param id ID of the unreachable member.
     */
    private void unreachableMember(int id) {
        for(Member m: client.getMembers()) if(m.getID() == id) { client.getUnreachableMembers().add(m); break; }
    }
}
