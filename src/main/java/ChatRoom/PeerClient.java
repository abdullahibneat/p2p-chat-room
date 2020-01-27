package ChatRoom;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Abdullah
 */
public class PeerClient {
    int PORT;
    PeerServerThread server;
    ArrayList<PeerMember> members = new ArrayList<>();
    boolean online = true;
    
    public static void main(String[] args) {
        try {
            PeerClient p = new PeerClient();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public PeerClient() throws Exception {
        Scanner input = new Scanner(System.in);
        
        while(true) {
            try {
                System.out.print("> Enter a port: ");
                PORT = Integer.parseInt(input.nextLine());
                server = new PeerServerThread(this);
                server.start();
                break;
            } catch(NumberFormatException e) {
                System.out.println("> Port must be an integer number.");
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        System.out.print("> Enter the address:port of an existing member: ");
        String existingMember = input.nextLine();
        if(existingMember.isEmpty()){
            System.out.println("> You're the coordinator");
        } else {
            members.add(new PeerMember(existingMember.split(":")[0], Integer.parseInt(existingMember.split(":")[1])));
            sendMessage("Hello world!");
        }
        
        while(true) {
            String message = input.nextLine();
            if(message.startsWith(">")) continue;
            if(message.equals("/help")) {
                System.out.println(
                        "> Available commands:\n>\n" +
                        "> /add ADDRESS:PORT\n" +
                        "> Adds a member to your list of members\n>\n" +
                        "> /quit\n" +
                        "> Leave the chat");
            } else if(message.equals("/quit")) {
                online = false;
                break;
            } else if (message.startsWith("/add ")) {
                String[] newContact = message.substring(5).split(":");
                members.add(new PeerMember(newContact[0], Integer.parseInt(newContact[1])));
            } else {
                sendMessage(message);
            }
        }
        
        server.join();
        
        System.out.println("Connection terminated.");
    }
    
    private void sendMessage(String message) {
        for(PeerMember member: members) {
            try {
                Socket conn = new Socket(member.address, member.port);
                PrintWriter out = new PrintWriter(conn.getOutputStream(), true);  // "true" because it allows flushing (i.e. sends message immediately, and clears the stream
                out.println(message);                                               // so further messages can be sent later)
            } catch (IOException e) {
                System.out.println("Member does not exist");
            }
        }
    }
}
