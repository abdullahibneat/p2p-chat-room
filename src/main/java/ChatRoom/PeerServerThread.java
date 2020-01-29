package ChatRoom;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Server component for a peer.
 * 
 * Allows a peer to receive messages from others.
 *
 * @author Abdullah
 */
public class PeerServerThread extends Thread {
    
    PeerClient peer;
    ServerSocket server;
    
    /**
     * @param c Client this server should be bound to
     * @throws java.lang.Exception
     */
    public PeerServerThread(PeerClient c) throws Exception {
        peer = c;
        try {
            // Create a server
            server = new ServerSocket(peer.me.port);
            server.setSoTimeout(5); // Let server accept for 5ms instead of infinity
                                    // Without it the client can never join the thread.
            
            // Get IP address to share with others peers
            Socket s = new Socket();
            s.connect(new InetSocketAddress("google.com", 80));
            c.me.address = s.getLocalAddress().toString().substring(1);
            System.out.println("> Share your ADDRESS:PORT with other members: " + c.me.address + ":" + c.me.port);
        } catch (IOException ex) {
            throw new Exception("> Port not available, try another port.");
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
                
                Scanner s = new Scanner(conn.getInputStream());
                
                // Display the message on screen
                while(s.hasNextLine()) {
                    System.out.println("> " + s.nextLine());
                }
                
                s.close();
                
            } catch (IOException ex) {
                // Connection went wrong.
            }
        }
    }
}
