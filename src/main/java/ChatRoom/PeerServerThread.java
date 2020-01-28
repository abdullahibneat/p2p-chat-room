package ChatRoom;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
/**
 *
 * @author Abdullah
 */
public class PeerServerThread extends Thread {
    
    PeerClient peer;
    int port;
    ServerSocket server;
    
    public PeerServerThread(PeerClient c) throws Exception {
        peer = c;
        port = peer.PORT;
        try {
            server = new ServerSocket(port);
            server.setSoTimeout(5); // Let server accept for 5ms instead of infinity
            
            // Display IP address to share with others
            Socket s = new Socket();
            s.connect(new InetSocketAddress("google.com", 80));
            System.out.println("> Share your ADDRESS:PORT with other members: " + s.getLocalAddress().toString().substring(1) + ":" + port);
        } catch (IOException ex) {
            throw new Exception("> Port not available, try another port.");
        }
    }
    
    @Override
    public void run() {
        while(peer.online) {
            try {
                Socket conn = server.accept();
                
                Scanner s = new Scanner(conn.getInputStream());
                
                System.out.println("> " + s.nextLine());
                
                s.close();
                
            } catch (IOException ex) {
                //
            }
        }
    }
}
