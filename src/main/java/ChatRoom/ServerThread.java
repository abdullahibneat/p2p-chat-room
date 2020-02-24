package ChatRoom;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server component for a peer.
 * 
 * Allows a peer to receive messages from others.
 *
 * @author Abdullah
 */
public class ServerThread extends Thread {
    
    private final Client peer;
    private final ServerSocket server;
    
    /**
     * @param c Client this server should be bound to
     * @throws ChatRoom.PortNotAvailbleException
     */
    public ServerThread(Client c) throws PortNotAvailbleException {
        peer = c;
        try {
            // Create a server
            server = new ServerSocket(peer.me.getPort());
            peer.postMessage("> Share your ADDRESS:PORT with other members: " + c.me.getAddress() + ":" + c.me.getPort());
        } catch (IOException e) {
            throw new PortNotAvailbleException("Port not available, try another port.");
        }
    }
    
    @Override
    public void run() {            
        // Create a pool of threads, to enable multiple peer to communicate
        // at the same time
        ExecutorService pool = Executors.newFixedThreadPool(500);
        while(true) {
            try {
                pool.execute(new Handler(peer, server.accept()));
            } catch (IOException ex) {
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
    
    private final Client peer;
    private final Socket conn;
    
    public Handler(Client c, Socket conn) {
        this.peer = c;
        this.conn = conn;
    }

    /**
     * Server
     * 
     * Listen for incoming connections from other peers.
     */
    @Override
    public void run() {
        try {
            // Receive object from other peer
            ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
            Object obj = in.readObject();

            // If string, it could be a normal message or command.
            if(obj instanceof String) {
                String message = (String)obj;

                if(message.startsWith("newMember")) newMember(message); // New member joined the network, add them to the list
                else if(message.startsWith("removeMember")) removeMember(Integer.parseInt(message.split(":")[1])); // Someone left the group, remove them from the list
                else if(message.startsWith("newCoordinator")) newCoordinator(Integer.parseInt(message.split(":")[1])); // Coordinator changed, update the list.
                else if(message.startsWith("unreachable")) unreachableMember(Integer.parseInt(message.split(":")[1]));
                else peer.postMessage(message); // Normal chat message
                
                peer.updateMembersList();
            }
            // If Member, someone is trying to join the network
            else if(obj instanceof Member) peer.incomingRequest((Member)obj, conn);
            
            conn.close();
        } catch(EOFException e) {
            // Someone is pinging, no file received.
        } catch (IOException e) {
            // Connection went wrong.
        } catch (ClassNotFoundException e) {
            System.out.println("!ClassNotFound");
        }
    }
    
    private void newMember(String newMemberString) {
        String[] newMemberArr = newMemberString.substring(10).split(":"); // FORMAT => username:id:address:port
        String userName = newMemberArr[0];
        int id = Integer.parseInt(newMemberArr[1]);
        String address = newMemberArr[2];
        int port = Integer.parseInt(newMemberArr[3]);
        // Because of multiple threads, it's possible that when a member joins the chat, the
        // same member receives a request to add himself to the chat.
        if(id != peer.me.getID()) {
            peer.postMessage("New member \"" + newMemberArr[0] + "\" joined!");
            peer.getMembers().add(new Member(userName, id, address, port));
        }
    }
    
    private void removeMember(int id) {
        peer.getMembers().removeIf(m -> m.getID() == id); // Find and remove member
    }
    
    private void newCoordinator(int id) {
        for(Member m: peer.getMembers()) {
            if(m.getID() == id) {
                m.setCoordinator();
                peer.postMessage(m.getUsername() + " is the new coordinator!");
                break;
            }
        }
    }
    
    private void unreachableMember(int id) {
        for(Member m: peer.getMembers()) if(m.getID() == id) { peer.getUnreachableMembers().add(m); break; }
    }
}
