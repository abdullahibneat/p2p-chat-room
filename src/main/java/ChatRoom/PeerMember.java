package ChatRoom;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Abdullah
 */
public class PeerMember {
    String userName;
    String address;
    int port;
    
    public PeerMember() {}
    
    public PeerMember(String address, int port) {
        this.address = address;
        this.port = port;
    }
}
