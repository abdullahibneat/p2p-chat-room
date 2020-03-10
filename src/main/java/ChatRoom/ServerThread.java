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
    
    private volatile boolean run = true;
    
    private final Client client;
    private final ServerSocket server;
    private final ExecutorService pool = Executors.newFixedThreadPool(500);;
    
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
            client.postMessage(new Message(client.me.getUsername(), "Share your ADDRESS:PORT with other members: " + c.me.getAddress() + ":" + c.me.getPort(), MessageType.SYSTEM));
        } catch (IOException e) {
            throw new PortNotAvailbleException("Port not available, try another port.");
        }
    }
    
    @Override
    public void run() {            
        // Create a pool of threads, to enable multiple members to communicate
        // at the same time
        while(run) {
            try {
                pool.execute(new Handler(client, server.accept()));
            } catch (IOException e) {
                // Connection went wrong.
            }
        }
    }
    
    /**
     * Method to terminate this thread.
     */
    protected void stopThread() {
        run = false;
        pool.shutdown();
        try {
            server.close();
        } catch(IOException e) {}
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
            if(obj instanceof Message) {
                
                Message message = (Message)obj;
                
                if(message.getMessageType() == MessageType.COMMAND) {
                    String command = message.getContent().split(":")[0];
                    String param = message.getContent().split(":", 2)[1];
                    
                    switch(command) {
                        case "newMember": // New member joined the network, add them to the list. FORMAT => newMember:username:id:address:port
                            newMember(param);
                            break;
                        case "removeMember": // Someone left the group, remove them from the list
                            removeMember(Integer.parseInt(param));
                            break;
                        case "newCoordinator": // Coordinator changed, update the list.
                            newCoordinator(Integer.parseInt(param));
                            break;
                        case "unreachable": // Unreachable member
                            unreachableMember(Integer.parseInt(param));
                            break;
                        default:
                            System.out.println("Unknown command: " + command);
                    }
                } else {
                    client.postMessage(message); // Normal chat message
                }
                
                client.updateMembersList();
            }
            // If Member, someone is trying to join the network
            else if(obj instanceof Member) client.incomingRequest((Member)obj, conn);
            
            else System.out.println("unexpected object " + obj.getClass().getSimpleName() + " " + obj);
            
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
        String[] newMemberArr = newMemberString.split(":"); // Remove "newMember:", FORMAT => username:id:address:port
        String userName = newMemberArr[0];
        int id = Integer.parseInt(newMemberArr[1]);
        String address = newMemberArr[2];
        int port = Integer.parseInt(newMemberArr[3]);
        
        // Because of multiple threads, it's possible that when a member joins the chat, the
        // same member receives a request to add himself to the chat.
        if(id != client.me.getID()) {
            client.postMessage(new Message(client.me.getUsername(), "New member \"" + newMemberArr[0] + "\" joined the chat!", MessageType.SYSTEM));
            try {
                client.getMembers().add(new Member(userName, id, address, port));
            } catch(InvalidUsernameException e) {}  // Should never reach this catch, since this new member's client will NOT reach the
                                                    // code for sending a request UNLESS the username is formatted right from the beginning.
        }
    }
    
    /**
     * Method to remove a member.
     * 
     * @param id ID of the member to be removed.
     */
    private void removeMember(int id) {        
        // Find and remove member
        for(Member m: client.getMembers()) {
            if(m.getID() == id) {
                client.getMembers().remove(m);
                client.postMessage(new Message(client.me.getUsername(), "Member " + m.getUsername() + " left.", MessageType.SYSTEM));
                client.updateMembersList();
                break;
            }
        }
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
                client.postMessage(new Message(client.me.getUsername(), m.getUsername() + " is the new coordinator!", MessageType.SYSTEM));
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
