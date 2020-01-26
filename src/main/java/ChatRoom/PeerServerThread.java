package ChatRoom;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
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
    
    public PeerServerThread(PeerClient c) {
        peer = c;
        port = peer.PORT;
    }
    
    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            System.out.println("> Server running");
        } catch (IOException ex) {
            System.out.println("> Port not available");
        }
        while(true && peer.online) {
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
