package ChatRoom;

import java.io.IOException;
import java.net.BindException;
import java.net.Socket;
import java.util.Iterator;


/**
 * Thread to take care of coordinator's duties:
 *      - Check if members are online
 *      - Inform others about offline members
 *
 * @author Abdullah
 */
public class CoordinatorThread extends Thread {
    
    private final Client peer;
    
    public CoordinatorThread(Client client) {
        this.peer = client;
    }
    
    /**
     * Thread to continuously check for members' status.
     */
    @Override
    public void run() {
        int currentMemberID = -1;
        String currentMemberUsername = "";
        System.out.println("Coordinator thread started");
        while(true) {
            // Two options. This peer is either:
            //      - Coordinator
            //      - Next coordinator
            
            // If this is the coordinator
            if(peer.me.isCoordinator()) {
                while(true) {
                    try {
                        // Try to connect to each unreachable member to make sure they're online
                        Iterator<Member> itr = peer.getUnreachableMembers().iterator();
                        while(itr.hasNext()) {
                            Member m = itr.next();
                            itr.remove();
                            currentMemberID = m.getID();
                            currentMemberUsername = m.getUsername();
                            System.out.println(currentMemberID + " might be unreachable, testing from coordinatorThread");
                            Socket s = new Socket(m.getAddress(), m.getPort());
                            s.close();
                        }
                    } catch (IOException e) {
                        System.out.println(e + " - removing member");
                        peer.globalRemoveMember(currentMemberID, currentMemberUsername);
                    }
                }
            } else {
                // I'm the next coordinator
                Member currentCoordinator = peer.getMembers().get(0);
                
                // When this peer is the second member, it might happen that
                // the 1st member (i.e. coordinator) is still sending the list
                // of members. Wait until member received the full list.
                while(!peer.online) {}
                
                // Continuously check if coordinator is online
                while(true) {
                    try {
                        Socket conn = new Socket(currentCoordinator.getAddress(), currentCoordinator.getPort());
                        conn.close();
                    } catch(BindException e) {
                        // java.net.BindException: Address already in use: connect
                        // Can be ignored
                    } catch(IOException e) {
                        // Coordinator left, take his role
                        peer.me.setCoordinator();
                        peer.globalRemoveMember(currentCoordinator.getID(), currentCoordinator.getUsername());
                        peer.sendMessage(currentCoordinator.getUsername() + " left, I'm the coordinator now!");
                        peer.sendCommand("newCoordinator:" + peer.me.getID());
                        break;
                    } finally {
                        try {
                            sleep(1000);
                        } catch(InterruptedException e) {
                            System.out.println("Error sleeping");
                        }
                    }
                }
            }
        }
    }
}
