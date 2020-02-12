package ChatRoom;

import java.io.IOException;
import java.net.Socket;


/**
 * Thread to take care of coordinator's duties:
 *      - Check if members are online
 *      - Inform others about offline members
 *
 * @author Abdullah
 */
public class CoordinatorThread extends Thread {
    
    private final PeerClient peer;
    
    public CoordinatorThread(PeerClient client) {
        this.peer = client;
        this.start();
    }
    
    @Override
    public void run() {
        int currentMemberID = -1;
        String currentMemberUsername = "";
        System.out.println("Coordinator thread started");
        while(true) {
            // If this is the coordinator
            if(peer.me.isCoordinator()) {
                try {
                    for(PeerMember m: peer.members) {
                        currentMemberID = m.getID();
                        currentMemberUsername = m.getUsername();
                        Socket s = new Socket(m.getAddress(), m.getPort());
                        s.close();
                    }
                    sleep(1000); // Check every second.
                } catch(IOException e) {
                    peer.globalRemoveMember(currentMemberID, currentMemberUsername);
                } catch (InterruptedException ex) {
                    // Peer disconnected
                }
            } else {
                // I'm the next coordinator
                // Find current coordinator
                PeerMember currentCoordinator = null;
                for(PeerMember m: peer.members) {
                    if(m.isCoordinator()) {
                        currentCoordinator = m;
                        break;
                    }
                }
                while(true) {
                    try {
                        Socket conn = new Socket(currentCoordinator.getAddress(), currentCoordinator.getPort());
                        conn.close();
                        sleep(1000); // Check every second.
                    } catch(IOException e) {
                        // Coordinator left, take his role
                        peer.me.setCoordinatorStatus(true);
                        peer.globalRemoveMember(currentCoordinator.getID(), currentCoordinator.getUsername());
                        peer.sendMessage(currentCoordinator.getUsername() + " left, I'm the coordinator now!", false);
                        peer.sendMessage("newCoordinator:" + peer.me.getID(), true);
                        break;
                    } catch(InterruptedException e) {
                        // Peer disconnected
                    }
                }
            }
        }
    }
}
