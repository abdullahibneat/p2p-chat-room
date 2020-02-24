/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatRoom;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author iAbdu
 */
public class MessagingThread extends Thread {
    
    Client c;
    String message;
    boolean isCommand;
    
    public MessagingThread(Client c, String message, boolean isCommand) {
        this.c = c;
        this.message = message;
        this.isCommand = isCommand;
        this.start();
    }
    
    @Override
    public void run() {
        synchronized(c.getMembers()) {
            for(Member member: c.getMembers()) {
                try {
                    Socket conn = new Socket(member.getAddress(), member.getPort());
                    conn.setSoTimeout(1);
                    ObjectOutputStream out = new ObjectOutputStream(conn.getOutputStream());
                    out.writeObject(isCommand? message : c.me.getUsername() + ": " + message);
                    out.flush();
                    conn.close();
                } catch (IOException e) {
                    System.out.println("Asking coordinator to remove");
                    c.unreachableMember(member);
                }
            }
        }
    }
}
