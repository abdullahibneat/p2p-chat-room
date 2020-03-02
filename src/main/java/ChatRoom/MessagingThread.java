package ChatRoom;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Thread to handle the sending of messages for a client.
 * 
 * A separate thread was necessary to make the application feel responsive. Previously, when a member
 * tried to send a message to everyone, but some of the other members already disconnected, there
 * the application would hang once the "send" button was pressed, due to the Connection Timeout of sockets.
 *
 * @author Abdullah
 */
public class MessagingThread extends Thread {
    
    private final Client c;
    private final Message message;
    
    public MessagingThread(Client c, Message message) {
        this.c = c;
        this.message = message;
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
                    out.writeObject(message);
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
