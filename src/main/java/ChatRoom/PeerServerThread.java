package ChatRoom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
                
                ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
                
                Object obj = in.readObject();
                String objClass = obj.getClass().getName();
                
                if(objClass.equals("java.lang.String")) {
                    String message = (String)obj;
                    
                    if(message.startsWith("newMember")) {
                        System.out.println("> Add member " + message);
                    } else {
                        System.out.println("> " + message);
                    }
                }
                else if(objClass.equals("ChatRoom.PeerMember")) {
                    PeerMember m = (PeerMember)obj;
                    Scanner input = new Scanner(System.in);
                    System.out.print("> Connection request from " + m.userName + ". Add to list (y/n)? ");
                    String ans = input.nextLine().toLowerCase();
                    if(ans.equals("y")) {
                        peer.members.add(m);
                        ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                        out.writeObject(peer.me);
                        out.flush();
                        System.out.println(peer.members.size());
                        peer.globalAddMember(m);
                        System.out.println("> Send message to peer saying I've added them");
                    } else {
                        System.out.println("> Ignoring...");
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
