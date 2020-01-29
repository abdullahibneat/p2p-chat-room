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
    PeerMember me;
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
            System.out.print("> Enter a username: ");
            String userName = input.nextLine();
            if(!(userName.isEmpty() || userName.contains(" "))) {
                me = new PeerMember(userName);
                break;
            }
            System.out.println("> Username cannot be empty or contain spaces.");
        }
        
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
        
        while(true) {
            System.out.print("> Enter the address:port of an existing member: ");
            String existingMember = input.nextLine();
            if(existingMember.isEmpty()){
                System.out.println("> You're the coordinator");
                break;
            } else {
                try {
                    addMember(existingMember);
                    sendMessage("Add me to your conctacts: " + me.address + ":" + me.port);
                    System.out.println("> Connected!");
                    break;
                } catch(Exception e) {
                    System.out.println("> " + e.getMessage());
                }
            }
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
                        "> Leave the chat\n>\n" +
                        "> /details [username]\n" +
                        "> Lists your details unless a username is specified");
            } else if(message.equals("/quit")) {
                online = false;
                break;
            } else if (message.startsWith("/add ")) {
                try {
                    addMember(message.substring(5));
                } catch(Exception e) {
                    System.out.println("> " + e.getMessage());
                }
            } else if(message.startsWith("/details")) {
                if(message.length() > 9) {  // i.e. /details username
                    boolean found = false;
                    String userName = message.substring(9);
                    
                    for(PeerMember m: members) {
                        if(m.userName.equals(userName)) {
                            found = true;
                            System.out.println(m);
                            break;
                        }
                    }
                    if(!found) System.out.println("> Member does not exist");
                } else {
                    System.out.println(me);
                }
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
                conn.close();
            } catch (IOException e) {
                System.out.println("Member does not exist");
            }
        }
    }
    
    private void addMember(String addressPortString) throws Exception {
        String[] addressPortArr = addressPortString.split(":");
        if(addressPortArr.length != 2) throw new Exception("Invalid format");
        
        try {
            String peerAddress = addressPortArr[0];
            int peerPort = Integer.parseInt(addressPortArr[1]);
            Socket testConn = new Socket(peerAddress, peerPort);
            testConn.close();
            PeerMember newMember = new PeerMember(peerAddress, peerPort);
            members.add(newMember);
            System.out.println("> Member added to your list.");
        } catch(NumberFormatException e) {
            throw new Exception("Port must be an integer number.");
        } catch(IOException e) {
            throw new Exception("Member does not exist");
        }
    }
}
