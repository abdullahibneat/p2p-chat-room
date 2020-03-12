package ChatRoom;

import org.junit.Test;

/**
 * Test all exceptions.
 *
 * @author iAbdu
 */
public class ExceptionsTest {
    
    /**
     * Test whether the program detects a port is already in use.
     */
    @Test(expected = PortNotAvailbleException.class)
    public void testPortNotAvailable() throws Exception {
        Member pna_m1 = new Member("m1", 135); // 135 is a port used by Windows OS.
        Client pna_c = new Client(pna_m1, false, "", 0);
    }
    
    /**
     * Test username format for the first member only. It can't be left empty or include spaces.
     */
    @Test(expected = InvalidUsernameException.class)
    public void testInvalidUsernameFirstMember() throws Exception {
        TestClient iufm_m1 = TestClient.buildTestClient("", TestClient.EMPTY_MEMBER); // Empty username
        TestClient iufm_m2 = TestClient.buildTestClient("m 1", TestClient.EMPTY_MEMBER); // Contains space
    }
    
    /**
     * Test if other members' usernames are checked, even if they don't connect through the coordinator.
     */
    @Test(expected = InvalidUsernameException.class)
    public void testInvalidUsernameAnyMember() throws Exception {
        TestClient iuam_c1 = TestClient.buildTestClient("john", TestClient.EMPTY_MEMBER);
        TestClient iuam_c2 = TestClient.buildTestClient("adam", iuam_c1.me);
        TestClient iuam_c3 = TestClient.buildTestClient("john", iuam_c2.me); // Same as first member
        TestClient iuam_c4 = TestClient.buildTestClient("joHN", iuam_c2.me); // Same as first member
        TestClient iuam_c5 = TestClient.buildTestClient("Jason", iuam_c2.me);
        TestClient iuam_c6 = TestClient.buildTestClient("Bob Smith", iuam_c3.me); // Contains spaces
    }
    
    /**
     * Test whether the program detects an attempt to connect to a network through a non-existing member.
     * Note the other member's username makes no difference, it's just the port number and host address.
     */
    @Test(expected = UnknownMemberException.class)
    public void testUnknownOtherMember() throws Exception {
        TestClient uom_c1 = TestClient.buildTestClient("m3", new Member("NotExistingMember", 987)); // No user exists at port 987 (localhost)
        TestClient uom_c2 = TestClient.buildTestClient("m4", new Member("AgainNonExisiting", -1, "https://google.com/", 456)); // Non-existing user in a different system.
    }
    
}
