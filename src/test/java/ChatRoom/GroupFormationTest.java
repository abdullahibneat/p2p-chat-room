package ChatRoom;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;

/**
 * Testing the core functionality of this program.
 * 
 * MUST ADD Thread.sleep() after sending messages, as messages are sent so rapidly
 * the tests might pull messages before they are added to the ArrayList.
 *
 * @author iAbdu
 */
public class GroupFormationTest {
    
    private Client c1;
    private Client c2;
    private Client c3;
    private Client c4;
    private Client c5;
    private Client c6;
    private Client c7;
    
    private Member m1;
    private Member m2;
    private Member m3;
    private Member m4;
    private Member m5;
    private Member m6;
    private Member m7;
    
    public GroupFormationTest() throws InvalidUsernameException, UnknownHostException {
        m1 = new Member("m1", 123);
        m2 = new Member("m2", 456);
        m3 = new Member("m3", 789);
        m4 = new Member("m4", 987);
        m5 = new Member("m5", 654);
        m6 = new Member("m6", 321);
        m7 = new Member("m7", 132);
    }
    
    @After
    public void tearDown() throws InvalidUsernameException, UnknownHostException {
        // Reset clients
        if(c1 != null) c1.quit();
        if(c2 != null) c2.quit();
        if(c3 != null) c3.quit();
        if(c4 != null) c4.quit();
        if(c5 != null) c5.quit();
        if(c6 != null) c6.quit();
        if(c7 != null) c7.quit();
        
        // Reset members
        m1 = new Member("m1", 123);
        m2 = new Member("m2", 456);
        m3 = new Member("m3", 789);
        m4 = new Member("m4", 987);
        m5 = new Member("m5", 654);
        m6 = new Member("m6", 321);
        m7 = new Member("m7", 132);
    }

    /**
     * Test if first member can start a network and be the coordinator.
     */
    @Test
    public void testNetowrkCreation() throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException {
        c1 = new Client(m1, false, "", 0);
        assertEquals(c1.online, true); // 12 is online only if network is successfully created.
        assertEquals(m1.isCoordinator(), true);
    }
    
    /**
     * Test if one member can join the network.
     */
    @Test
    public void testSingleMemberConnection() throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException {
        c1 = new Client(m1, false, "", 0);
        c2 = new Client(m2, false, m1.getAddress(), m1.getPort());
        
        assertEquals(c2.online, true); // c2 is online only if connection is established.
        
        assertEquals(c1.getMembers().size(), c2.getMembers().size()); // m1 and m2 sjould have the same number of members (1)
        assertEquals(c1.getMembers().get(0), c2.me); // the only member for c1 should be m2        
        assertEquals(c2.getMembers().get(0), c1.me); // the only member for c2 should be m1
    }
    
    /**
     * Test if two members can send messages to each other.
     */
    @Test
    public void testTwoMembersCommunication() throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException, InterruptedException {
        c1 = new Client(m1, false, "", 0);
        c2 = new Client(m2, false, m1.getAddress(), m1.getPort());
        
        c1.sendMessage("Hello from m1");
        Thread.sleep(250);
        c2.sendMessage("Hello back from m2");
        Thread.sleep(250);
        c2.sendMessage("How are you?");
        Thread.sleep(250);
        c1.sendMessage("I'm good");
        Thread.sleep(250);
        
        // Get all messages from c1
        ArrayList<Message> c1_messages = new ArrayList<>();
        for(Message m: c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c1_messages.add(m);
        
        // Get all messages from c2
        ArrayList<Message> c2_messages = new ArrayList<>();
        for(Message m: c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c2_messages.add(m);
        
        assertArrayEquals(c1_messages.toArray(), c2_messages.toArray());
    }
    
    /**
     * Test if multiple members can join the network by connecting to the coordinator.
     */
    @Test
    public void testMultiMemberConnectionKnowingCoordinator() throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException {
        c1 = new Client(m1, false, "", 0);
        c2 = new Client(m2, false, m1.getAddress(), m1.getPort());
        c3 = new Client(m3, false, m1.getAddress(), m1.getPort());
        c4 = new Client(m4, false, m1.getAddress(), m1.getPort());
        c5 = new Client(m5, false, m1.getAddress(), m1.getPort());
        c6 = new Client(m6, false, m1.getAddress(), m1.getPort());
        c7 = new Client(m7, false, m1.getAddress(), m1.getPort());
        
        Member[] allMembers = {c1.me, c2.me, c3.me, c4.me, c5.me, c6.me, c7.me};
        
        // Check if all members know each other.
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c1.me)).toArray(), c1.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c2.me)).toArray(), c2.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c3.me)).toArray(), c3.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c4.me)).toArray(), c4.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c5.me)).toArray(), c5.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c6.me)).toArray(), c6.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c7.me)).toArray(), c7.getMembers().toArray());
    }
    
    /**
     * Test if multiple member can join by knowing any other member in the group (not necessarily the coordinator).
     * 
     * Simulating the following:
     * 
     *                       m1 (coordinator)
     *                              ^
     *                              |
     *                          +---+---+
     *                         m2       m6
     *                          ^
     *                          |
     *                      +---+---+
     *                     m4       m3
     *                      ^       ^
     *                      |       |
     *                      +       +
     *                     m7       m5
     * 
     */
    @Test
    public void testMultiMemberConnectionKnowingAnyExistingMember() throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException {
        c1 = new Client(m1, false, "", 0);
        c2 = new Client(m2, false, m1.getAddress(), m1.getPort());
        c3 = new Client(m3, false, m2.getAddress(), m2.getPort());
        c4 = new Client(m4, false, m2.getAddress(), m2.getPort());
        c5 = new Client(m5, false, m3.getAddress(), m3.getPort());
        c6 = new Client(m6, false, m1.getAddress(), m1.getPort());
        c7 = new Client(m7, false, m4.getAddress(), m4.getPort());
        
        Member[] allMembers = {c1.me, c2.me, c3.me, c4.me, c5.me, c6.me, c7.me};
        
        // Check if all members know each other.
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c1.me)).toArray(), c1.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c2.me)).toArray(), c2.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c3.me)).toArray(), c3.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c4.me)).toArray(), c4.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c5.me)).toArray(), c5.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c6.me)).toArray(), c6.getMembers().toArray());
        assertArrayEquals(Arrays.stream(allMembers).filter(m -> !m.equals(c7.me)).toArray(), c7.getMembers().toArray());
    }
    
    /**
     * Testing whether multiple members can communicate after they are ALL connected to the chat.
     * Using the diagram above.
     */
    @Test
    public void testMultiMemberCommunicationAfterAllConnected() throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException, InterruptedException {
        ArrayList<Message> c1_messages = new ArrayList<>();
        ArrayList<Message> c2_messages = new ArrayList<>();
        ArrayList<Message> c3_messages = new ArrayList<>();
        ArrayList<Message> c4_messages = new ArrayList<>();
        ArrayList<Message> c5_messages = new ArrayList<>();
        ArrayList<Message> c6_messages = new ArrayList<>();
        ArrayList<Message> c7_messages = new ArrayList<>();
        
        c1 = new Client(m1, false, "", 0);
        c2 = new Client(m2, false, m1.getAddress(), m1.getPort());
        c3 = new Client(m3, false, m2.getAddress(), m2.getPort());
        c4 = new Client(m4, false, m2.getAddress(), m2.getPort());
        c5 = new Client(m5, false, m3.getAddress(), m3.getPort());
        c6 = new Client(m6, false, m1.getAddress(), m1.getPort());
        c7 = new Client(m7, false, m4.getAddress(), m4.getPort());
        
        c1.sendMessage("Hello from m1");
        Thread.sleep(250);
        c2.sendMessage("Hello from m2");
        Thread.sleep(250);
        c3.sendMessage("Hello from m3");
        Thread.sleep(250);
        c4.sendMessage("Hello from m4");
        Thread.sleep(250);
        c5.sendMessage("Hello from m5");
        Thread.sleep(250);
        c6.sendMessage("Hello from m6");
        Thread.sleep(250);
        c7.sendMessage("Hello from m7");
        Thread.sleep(250);
        
        for(Message m: c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c1_messages.add(m);
        for(Message m: c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c2_messages.add(m);
        for(Message m: c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c3_messages.add(m);
        for(Message m: c4.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c4_messages.add(m);
        for(Message m: c5.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c5_messages.add(m);
        for(Message m: c6.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c6_messages.add(m);
        for(Message m: c7.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c7_messages.add(m);
        
        // Test if they all received the same messages
        assertArrayEquals(c1_messages.toArray(), c2_messages.toArray());
        assertArrayEquals(c3_messages.toArray(), c4_messages.toArray());
        assertArrayEquals(c5_messages.toArray(), c6_messages.toArray());
        assertArrayEquals(c7_messages.toArray(), c4_messages.toArray());
        assertArrayEquals(c5_messages.toArray(), c2_messages.toArray());
        assertArrayEquals(c3_messages.toArray(), c2_messages.toArray());
        assertArrayEquals(c1_messages.toArray(), c4_messages.toArray());
    }
    
    /**
     * Testing whether multiple members can send messages between each other as people join the chat.
     * Using the diagram above.
     */
    @Test
    public void testMultiMemberCommunicationRandomJoin() throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException, InterruptedException {
        
        ArrayList<Message> c1_messages = new ArrayList<>();
        ArrayList<Message> c2_messages = new ArrayList<>();
        ArrayList<Message> c3_messages = new ArrayList<>();
        ArrayList<Message> c4_messages = new ArrayList<>();
        ArrayList<Message> c5_messages = new ArrayList<>();
        ArrayList<Message> c6_messages = new ArrayList<>();
        ArrayList<Message> c7_messages = new ArrayList<>();
        
        c1 = new Client(m1, false, "", 0);
        c2 = new Client(m2, false, m1.getAddress(), m1.getPort());
        
        // Send 5 messages
        c1.sendMessage("Hi m2!");
        Thread.sleep(250);
        c2.sendMessage("hi! how are you?");
        Thread.sleep(250);
        c1.sendMessage("I'm good");
        Thread.sleep(250);
        c1.sendMessage("Remember to share your details with others so more poeple will join!");
        Thread.sleep(250);
        c2.sendMessage("I shared my details already, more poeple will come don't worry :)");
        Thread.sleep(250);
        
        for(Message m: c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c1_messages.add(m);
        for(Message m: c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c2_messages.add(m);
        
        assertArrayEquals(c1_messages.toArray(), c2_messages.toArray());
        
        c3 = new Client(m3, false, m2.getAddress(), m2.getPort());
        
        // Send 4 messages
        c3.sendMessage("Yo guys!");
        Thread.sleep(250);
        c2.sendMessage("Hey m3! You finally joined...");
        Thread.sleep(250);
        c3.sendMessage("Yes! I'm so excited");
        Thread.sleep(250);
        c1.sendMessage("Welcome c3! Nice to meet you");
        Thread.sleep(250);
        
        // New members cannot see previous messages, so check if messages sent after last member
        // joined are the same
        c1_messages.clear();
        c2_messages.clear();
        for(Message m: c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c1_messages.add(m);
        for(Message m: c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c2_messages.add(m);
        for(Message m: c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c3_messages.add(m);
        
        assertArrayEquals(c3_messages.toArray(), c1_messages.subList(5, c1_messages.size()).toArray()); // Ignore first 5 messages (those prior to m3 connecting)
        assertArrayEquals(c3_messages.toArray(), c2_messages.subList(5, c2_messages.size()).toArray());
        
        c4 = new Client(m4, false, m2.getAddress(), m2.getPort());
        
        // Send 3 messages
        c3.sendMessage("Nice to meet you too m1");
        Thread.sleep(250);
        c1.sendMessage("Another member! How are you m4?");
        Thread.sleep(250);
        c4.sendMessage("Hi everyone!");
        Thread.sleep(250);
        
        c1_messages.clear();
        c2_messages.clear();
        c3_messages.clear();
        for(Message m: c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c1_messages.add(m);
        for(Message m: c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c2_messages.add(m);
        for(Message m: c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c3_messages.add(m);
        for(Message m: c4.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c4_messages.add(m);
        
        assertArrayEquals(c4_messages.toArray(), c1_messages.subList(9, c1_messages.size()).toArray()); // Ignore first 9 messages
        assertArrayEquals(c4_messages.toArray(), c2_messages.subList(9, c2_messages.size()).toArray());
        assertArrayEquals(c4_messages.toArray(), c3_messages.subList(4, c3_messages.size()).toArray()); // Ignore first 4 messages because m3 received
                                                                                                        // 4 messages between joining the chat and m4 connecting
        
        c5 = new Client(m5, false, m3.getAddress(), m3.getPort());
        c6 = new Client(m6, false, m1.getAddress(), m1.getPort());
        
        // Send 5 messages
        c2.sendMessage("Woah, so many people are joining, hope this chat will not crash lol");
        Thread.sleep(250);
        c1.sendMessage("Don't worry, I'm the best programmer in the world, so this program can handle anything XD");
        Thread.sleep(250);
        c6.sendMessage("What's going on?");
        Thread.sleep(250);
        c5.sendMessage("Error 503: Service not available");
        Thread.sleep(250);
        c1.sendMessage("Nice try m5");
        Thread.sleep(250);
        
        c1_messages.clear();
        c2_messages.clear();
        c3_messages.clear();
        c4_messages.clear();
        for(Message m: c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c1_messages.add(m);
        for(Message m: c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c2_messages.add(m);
        for(Message m: c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c3_messages.add(m);
        for(Message m: c4.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c4_messages.add(m);
        for(Message m: c5.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c5_messages.add(m);
        for(Message m: c6.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c6_messages.add(m);
        
        // Test against c5 first
        assertArrayEquals(c5_messages.toArray(), c1_messages.subList(12, c1_messages.size()).toArray());
        assertArrayEquals(c5_messages.toArray(), c2_messages.subList(12, c2_messages.size()).toArray());
        assertArrayEquals(c5_messages.toArray(), c3_messages.subList(7, c3_messages.size()).toArray());
        assertArrayEquals(c5_messages.toArray(), c4_messages.subList(3, c4_messages.size()).toArray());
        // Test against c6
        assertArrayEquals(c6_messages.toArray(), c1_messages.subList(12, c1_messages.size()).toArray());
        assertArrayEquals(c6_messages.toArray(), c2_messages.subList(12, c2_messages.size()).toArray());
        assertArrayEquals(c6_messages.toArray(), c3_messages.subList(7, c3_messages.size()).toArray());
        assertArrayEquals(c6_messages.toArray(), c4_messages.subList(3, c4_messages.size()).toArray());
        
        c7 = new Client(m7, false, m4.getAddress(), m4.getPort());
        
        // Send 2 messages
        c7.sendMessage("Hi guys, is this the cool network everyone's talking about?");
        Thread.sleep(250);
        c3.sendMessage("Yes, you're in the right place.");
        Thread.sleep(250);
        
        c1_messages.clear();
        c2_messages.clear();
        c3_messages.clear();
        c4_messages.clear();
        c5_messages.clear();
        c6_messages.clear();
        for(Message m: c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c1_messages.add(m);
        for(Message m: c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c2_messages.add(m);
        for(Message m: c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c3_messages.add(m);
        for(Message m: c4.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c4_messages.add(m);
        for(Message m: c5.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c5_messages.add(m);
        for(Message m: c6.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c6_messages.add(m);
        for(Message m: c7.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c7_messages.add(m);
        
        assertArrayEquals(c7_messages.toArray(), c1_messages.subList(17, c1_messages.size()).toArray());
        assertArrayEquals(c7_messages.toArray(), c2_messages.subList(17, c2_messages.size()).toArray());
        assertArrayEquals(c7_messages.toArray(), c3_messages.subList(12, c3_messages.size()).toArray());
        assertArrayEquals(c7_messages.toArray(), c4_messages.subList(8, c4_messages.size()).toArray());
        assertArrayEquals(c7_messages.toArray(), c5_messages.subList(5, c5_messages.size()).toArray());
        assertArrayEquals(c7_messages.toArray(), c6_messages.subList(5, c6_messages.size()).toArray());
    }
    
    /**
     * Test that one one member leaves, everyone is notified and the chat is working as normal.
     * Based on previous scenario.
     */
    @Test
    @Ignore
    public void testOneMemberLeaves() throws PortNotAvailbleException, UnknownMemberException, InvalidUsernameException, InterruptedException {
        
        c1 = new Client(m1, false, "", 0);
        c2 = new Client(m2, false, m1.getAddress(), m1.getPort());
        
        c1.sendMessage("Hi m2!");
        Thread.sleep(250);
        c2.sendMessage("hi! how are you?");
        Thread.sleep(250);
        c1.sendMessage("I'm good");
        Thread.sleep(250);
        c1.sendMessage("Remember to share your details with others so more poeple will join!");
        Thread.sleep(250);
        c2.sendMessage("I shared my details already, more poeple will come don't worry :)");
        Thread.sleep(250);
        
        c3 = new Client(m3, false, m2.getAddress(), m2.getPort());
        
        c3.sendMessage("Yo guys!");
        Thread.sleep(250);
        c2.sendMessage("Hey m3! You finally joined...");
        Thread.sleep(250);
        c3.sendMessage("Yes! I'm so excited");
        Thread.sleep(250);
        c1.sendMessage("Welcome c3! Nice to meet you");
        Thread.sleep(250);
        
        c4 = new Client(m4, false, m2.getAddress(), m2.getPort());
        
        c3.sendMessage("Nice to meet you too m1");
        Thread.sleep(250);
        c1.sendMessage("Another member! How are you m4?");
        Thread.sleep(250);
        c4.sendMessage("Hi everyone!");
        Thread.sleep(250);
        
        c5 = new Client(m5, false, m3.getAddress(), m3.getPort());
        c6 = new Client(m6, false, m1.getAddress(), m1.getPort());
        
        c2.sendMessage("Woah, so many people are joining, hope this chat will not crash lol");
        Thread.sleep(250);
        c1.sendMessage("Don't worry, I'm the best programmer in the world, so this program can handle anything XD");
        Thread.sleep(250);
        c6.sendMessage("What's going on?");
        Thread.sleep(250);
        
        /**
         * m3 leaves
         */
        m3 = c3.me;
        c3.quit();
        
        c5.sendMessage("Error 503: Service not available");
        Thread.sleep(250);
        c1.sendMessage("Nice try m5");
        Thread.sleep(250);
        
        c7 = new Client(m7, false, m4.getAddress(), m4.getPort());
        
        c7.sendMessage("Hi guys, is this the cool network everyone's talking about?");
        Thread.sleep(250);
        
        // Check whether everyone removed m3 from their list
        assertTrue(!(
                c1.getMembers().contains(m3) &&
                c2.getMembers().contains(m3) &&
                c3.getMembers().contains(m3) &&
                c4.getMembers().contains(m3) &&
                c5.getMembers().contains(m3) &&
                c6.getMembers().contains(m3) &&
                c7.getMembers().contains(m3)));
    }
}
