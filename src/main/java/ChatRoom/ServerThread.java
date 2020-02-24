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

                if(message.startsWith("newMember")) {
                    // New member joined the network, add them to the list
                    String[] newMemberArr = message.substring(10).split(":"); // FORMAT => username:id:address:port
                    peer.postMessage("New member \"" + newMemberArr[0] + "\" joined!");
                    peer.getMembers().add(new Member(newMemberArr[0], Integer.parseInt(newMemberArr[1]), newMemberArr[2], Integer.parseInt(newMemberArr[3])));
                } else if(message.startsWith("removeMember")) {
                    // Someone left the group, remove them from the list
                    int removeID = Integer.parseInt(message.split(":")[1]);
                    peer.getMembers().removeIf(m -> m.getID() == removeID); // Find and remove member
                } else if(message.startsWith("newCoordinator")) {
                    // Coordinator changed, update the list.
                    int newCoordinatorID = Integer.parseInt(message.split(":")[1]);
                    for(Member m: peer.getMembers()) {
                        if(m.getID() == newCoordinatorID) {
                            m.setCoordinator();
                            peer.postMessage(m.getUsername() + " is the new coordinator!");
                            break;
                        }
                    }
                } else if(message.startsWith("unreachable")) {
                    int id = Integer.parseInt(message.split(":")[1]);
                    for(Member m: peer.getMembers()) if(m.getID() == id) { peer.getUnreachableMembers().add(m); break; }
                } else {
                    // Normal chat message
                    peer.postMessage(message);
                }
                peer.updateMembersList();
            }
            // If Member, someone is trying to join the network
            else if(obj instanceof Member){
                Member m = (Member)obj;
                peer.incomingRequest(m, conn);
            }
            conn.close();
        } catch(EOFException e) {
            // Someone is pinging, no file received.
        } catch (IOException e) {
            // Connection went wrong.
        } catch (ClassNotFoundException e) {
            System.out.println("!ClassNotFound");
        }
    }
}
