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
            try {
                if(peer.members.size() > 0) {
                    for(PeerMember m: peer.members) {
                        currentMemberID = m.getID();
                        currentMemberUsername = m.getUsername();
                        Socket s = new Socket(m.getAddress(), m.getPort());
                        s.close();
                    }
                }
                sleep(1000); // Check every second.
            } catch(IOException e) {
                System.out.println(currentMemberUsername + " disconnected");
                peer.globalRemoveMember(currentMemberID, currentMemberUsername);
            } catch (InterruptedException ex) {
                // Coordinator not online anymore.
            }
        }
    }
    
}
