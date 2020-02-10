package ChatRoom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * Server component for a peer.
 * 
 * Allows a peer to receive messages from others.
 *
 * @author Abdullah
 */
public class PeerServerThread extends Thread {
    
    private final PeerClient peer;
    private final ServerSocket server;
    
    /**
     * @param c Client this server should be bound to
     * @throws ChatRoom.PortNotAvailbleException
     */
    public PeerServerThread(PeerClient c) throws PortNotAvailbleException {
        peer = c;
        try {
            // Create a server
            server = new ServerSocket(peer.me.getPort());
            server.setSoTimeout(5); // Let server accept for 5ms instead of infinity
                                    // Without it the client can never join the thread.
            
            peer.postMessage("> Share your ADDRESS:PORT with other members: " + c.me.getAddress() + ":" + c.me.getPort());
        } catch (IOException ex) {
            throw new PortNotAvailbleException("Port not available, try another port.");
        }
    }
    
    /**
     * Server
     * 
     * Listen for incoming connections from other peers.
     */
    @Override
    public void run() {
        while(peer.online) {
            try {
                Socket conn = server.accept();
                
                ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
                
                Object obj = in.readObject();
                String objClass = obj.getClass().getName();
                
                if(objClass.equals("java.lang.String")) {
                    String message = (String)obj;
                    
                    if(message.startsWith("newMember")) {
                        // New member joined the network, add them to the list
                        String[] newMemberArr = message.substring(10).split(":"); // FORMAT => username:id:address:port
                        peer.postMessage("> New member \"" + newMemberArr[0] + "\" joined!");
                        peer.members.add(new PeerMember(newMemberArr[0], Integer.parseInt(newMemberArr[1]), newMemberArr[2], Integer.parseInt(newMemberArr[3])));
                        peer.lastID = Integer.parseInt(newMemberArr[1]); // Update lastID to match this member's ID
                        peer.updateMembersList();
                    } else {
                        peer.postMessage(message);
                    }
                }
                else if(objClass.equals("ChatRoom.PeerMember")) {
                    PeerMember m = (PeerMember)obj;
                    int ans = JOptionPane.showConfirmDialog(null, "> Connection request from " + m.getUsername() + ". Add to list?");
                    if(ans == JOptionPane.YES_OPTION) {
                        ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                        
                        // Send list of all members
                        ArrayList<PeerMember> everyone = new ArrayList<>();
                        for(PeerMember existingMember: peer.members) everyone.add(existingMember);
                        everyone.add(peer.me);
                        out.writeObject(everyone);
                        
                        out.flush();
                        peer.globalAddMember(m);
                    } else {
                        peer.postMessage("> Ignoring...");
                    }
                }
                
                conn.close();
            } catch (IOException ex) {
                // Connection went wrong.
            } catch (ClassNotFoundException ex) {
                System.out.println("!ClassNotFound");
            }
        }
    }
}
