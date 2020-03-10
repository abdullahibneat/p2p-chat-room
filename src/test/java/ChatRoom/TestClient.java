package ChatRoom;

/**
 * A Client version used for tests.
 * 
 * The main difference is the adding of a Thread.sleep() when sending messages,
 * without which testing messaging by repeatedly calling the sendMessage() method
 * would not send the message to all clients by the time assertion methods are called.
 *
 * @author iAbdu
 */
public class TestClient extends Client {
    
    /**
     * An empty member that can be used to initialise a network.
     * Enables a member to become the coordinator.
     */
    public static final Member EMPTY_MEMBER = new Member("empty", 0, "", 0);
    
    /**
     * Creates a TestClient connecting member "m" with "other".
     * GUI is hidden.
     * 
     * @param m The member whom this client belongs to
     * @param other The other member to connect to. Use TestClient.EMPTY_MEMBER for first client.
     */
    public TestClient(Member m, Member other) throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException {
        super(m, false, other.getAddress(), other.getPort());
    }

    @Override
    protected void sendMessage(String message) {
        try {
            super.sendMessage(message);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println("TestClient interrupted");
        }
    }
    
    /**
     * Builds a test client with dynamic port selection.
     * 
     * @param username Username to assign to this client's owner
     * @param other The other member to connect to. Use TestClient.EMPTY_MEMBER for first client.
     * @return TestClient
     * @throws ChatRoom.UnknownMemberException Other member does not exist.
     * @throws ChatRoom.InvalidUsernameException Username is not unique.
     */
    public static TestClient buildTestClient(String username, Member other) throws UnknownMemberException, InvalidUsernameException {
        Member thisMember = new Member(username, -1, "localhost", 1);
        while(true) {
            try {
                return new TestClient(thisMember, other);
            } catch(PortNotAvailbleException e) {
                thisMember = new Member(thisMember.getUsername(), -1, "localhost", thisMember.getPort() + 1);
            }
        }
    }
}
