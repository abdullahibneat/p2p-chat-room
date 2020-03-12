package ChatRoom;

import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

/**
 * Testing group communication.
 * 
 * Variable names for client follows the following naming convention:
 * [first letter of each word in method name]_c[nth member]
 * This is to ensure no two client instances with the same variable name in different
 * methods interfere with each other
 *
 * @author iAbdu
 */
public class MessagingTest {
    
    /**
     * Test if two members can send messages to each other.
     */
    @Test
    public void testTwoMembersCommunication() throws Exception {
        System.out.println("testTwoMembersCommunication()");
        
        TestClient tmc_c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        TestClient tmc_c2 = TestClient.buildTestClient("m2", tmc_c1.me);
        
        tmc_c1.sendMessage("Hello from m1");
        tmc_c2.sendMessage("Hello back from m2");
        tmc_c2.sendMessage("How are you?");
        tmc_c1.sendMessage("I'm good");
        
        // Get all messages from c1
        ArrayList<Message> c1_messages = new ArrayList<>();
        for(Message m: tmc_c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c1_messages.add(m);
        
        // Get all messages from c2
        ArrayList<Message> c2_messages = new ArrayList<>();
        for(Message m: tmc_c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) c2_messages.add(m);
        
        assertArrayEquals(c1_messages.toArray(), c2_messages.toArray());
        
        tmc_c2.quit();
        tmc_c1.quit();
    }
    
    /**
     * Testing whether multiple members can communicate after they are ALL connected to the chat.
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
     */
    @Test
    public void testMultiMemberCommunicationAfterAllConnected() throws Exception {
        System.out.println("testMultiMemberCommunicationAfterAllConnected()");
        
        ArrayList<Message> mmcaac_c1_messages = new ArrayList<>();
        ArrayList<Message> mmcaac_c2_messages = new ArrayList<>();
        ArrayList<Message> mmcaac_c3_messages = new ArrayList<>();
        ArrayList<Message> mmcaac_c4_messages = new ArrayList<>();
        ArrayList<Message> mmcaac_c5_messages = new ArrayList<>();
        ArrayList<Message> mmcaac_c6_messages = new ArrayList<>();
        ArrayList<Message> mmcaac_c7_messages = new ArrayList<>();
        
        TestClient mmcaac_c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        TestClient mmcaac_c2 = TestClient.buildTestClient("m2", mmcaac_c1.me);
        TestClient mmcaac_c3 = TestClient.buildTestClient("m3", mmcaac_c2.me);
        TestClient mmcaac_c4 = TestClient.buildTestClient("m4", mmcaac_c2.me);
        TestClient mmcaac_c5 = TestClient.buildTestClient("m5", mmcaac_c3.me);
        TestClient mmcaac_c6 = TestClient.buildTestClient("m6", mmcaac_c1.me);
        TestClient mmcaac_c7 = TestClient.buildTestClient("m7", mmcaac_c4.me);
        
        mmcaac_c1.sendMessage("Hello from m1");
        mmcaac_c2.sendMessage("Hello from m2");
        mmcaac_c3.sendMessage("Hello from m3");
        mmcaac_c4.sendMessage("Hello from m4");
        mmcaac_c5.sendMessage("Hello from m5");
        mmcaac_c6.sendMessage("Hello from m6");
        mmcaac_c7.sendMessage("Hello from m7");
        
        for(Message m: mmcaac_c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcaac_c1_messages.add(m);
        for(Message m: mmcaac_c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcaac_c2_messages.add(m);
        for(Message m: mmcaac_c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcaac_c3_messages.add(m);
        for(Message m: mmcaac_c4.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcaac_c4_messages.add(m);
        for(Message m: mmcaac_c5.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcaac_c5_messages.add(m);
        for(Message m: mmcaac_c6.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcaac_c6_messages.add(m);
        for(Message m: mmcaac_c7.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcaac_c7_messages.add(m);
        
        // Test if they all received the same messages
        assertArrayEquals(mmcaac_c1_messages.toArray(), mmcaac_c2_messages.toArray());
        assertArrayEquals(mmcaac_c3_messages.toArray(), mmcaac_c4_messages.toArray());
        assertArrayEquals(mmcaac_c5_messages.toArray(), mmcaac_c6_messages.toArray());
        assertArrayEquals(mmcaac_c7_messages.toArray(), mmcaac_c4_messages.toArray());
        assertArrayEquals(mmcaac_c5_messages.toArray(), mmcaac_c2_messages.toArray());
        assertArrayEquals(mmcaac_c3_messages.toArray(), mmcaac_c2_messages.toArray());
        assertArrayEquals(mmcaac_c1_messages.toArray(), mmcaac_c4_messages.toArray());
        
        mmcaac_c7.quit();
        mmcaac_c6.quit();
        mmcaac_c5.quit();
        mmcaac_c4.quit();
        mmcaac_c3.quit();
        mmcaac_c2.quit();
        mmcaac_c1.quit();
    }
    
    /**
     * Testing whether multiple members can send messages between each other as people join the chat.
     * Using the diagram above.
     */
    @Test
    public void testMultiMemberCommunicationRandomJoin() throws Exception {
        System.out.println("testMultiMemberCommunicationRandomJoin()");
        
        ArrayList<Message> mmcrj_c1_messages = new ArrayList<>();
        ArrayList<Message> mmcrj_c2_messages = new ArrayList<>();
        ArrayList<Message> mmcrj_c3_messages = new ArrayList<>();
        ArrayList<Message> mmcrj_c4_messages = new ArrayList<>();
        ArrayList<Message> mmcrj_c5_messages = new ArrayList<>();
        ArrayList<Message> mmcrj_c6_messages = new ArrayList<>();
        ArrayList<Message> mmcrj_c7_messages = new ArrayList<>();
        
        TestClient mmcrj_c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        TestClient mmcrj_c2 = TestClient.buildTestClient("m2", mmcrj_c1.me);
        
        // Send 5 messages
        mmcrj_c1.sendMessage("Hi m2!");
        mmcrj_c2.sendMessage("hi! how are you?");
        mmcrj_c1.sendMessage("I'm good");
        mmcrj_c1.sendMessage("Remember to share your details with others so more poeple will join!");
        mmcrj_c2.sendMessage("I shared my details already, more poeple will come don't worry :)");
        
        for(Message m: mmcrj_c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c1_messages.add(m);
        for(Message m: mmcrj_c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c2_messages.add(m);
        
        assertArrayEquals(mmcrj_c1_messages.toArray(), mmcrj_c2_messages.toArray());
        
        TestClient mmcrj_c3 = TestClient.buildTestClient("m3", mmcrj_c2.me);
        
        // Send 4 messages
        mmcrj_c3.sendMessage("Yo guys!");
        mmcrj_c2.sendMessage("Hey m3! You finally joined...");
        mmcrj_c3.sendMessage("Yes! I'm so excited");
        mmcrj_c1.sendMessage("Welcome c3! Nice to meet you");
        
        // New members cannot see previous messages, so check if messages sent after last member
        // joined are the same
        mmcrj_c1_messages.clear();
        mmcrj_c2_messages.clear();
        for(Message m: mmcrj_c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c1_messages.add(m);
        for(Message m: mmcrj_c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c2_messages.add(m);
        for(Message m: mmcrj_c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c3_messages.add(m);
        
        assertArrayEquals(mmcrj_c3_messages.toArray(), mmcrj_c1_messages.subList(5, mmcrj_c1_messages.size()).toArray()); // Ignore first 5 messages (those prior to m3 connecting)
        assertArrayEquals(mmcrj_c3_messages.toArray(), mmcrj_c2_messages.subList(5, mmcrj_c2_messages.size()).toArray());
        
        TestClient mmcrj_c4 = TestClient.buildTestClient("m4", mmcrj_c2.me);
        System.out.println("hi");
        // Send 3 messages
        mmcrj_c3.sendMessage("Nice to meet you too m1");
        mmcrj_c1.sendMessage("Another member! How are you m4?");
        mmcrj_c4.sendMessage("Hi everyone!");
        
        mmcrj_c1_messages.clear();
        mmcrj_c2_messages.clear();
        mmcrj_c3_messages.clear();
        for(Message m: mmcrj_c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c1_messages.add(m);
        for(Message m: mmcrj_c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c2_messages.add(m);
        for(Message m: mmcrj_c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c3_messages.add(m);
        for(Message m: mmcrj_c4.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c4_messages.add(m);
        
        assertArrayEquals(mmcrj_c4_messages.toArray(), mmcrj_c1_messages.subList(9, mmcrj_c1_messages.size()).toArray()); // Ignore first 9 messages
        assertArrayEquals(mmcrj_c4_messages.toArray(), mmcrj_c2_messages.subList(9, mmcrj_c2_messages.size()).toArray());
        assertArrayEquals(mmcrj_c4_messages.toArray(), mmcrj_c3_messages.subList(4, mmcrj_c3_messages.size()).toArray()); // Ignore first 4 messages because m3 received
                                                                                                        // 4 messages between joining the chat and m4 connecting
        
        TestClient mmcrj_c5 = TestClient.buildTestClient("m5", mmcrj_c3.me);
        TestClient mmcrj_c6 = TestClient.buildTestClient("m6", mmcrj_c1.me);
        
        // Send 5 messages
        mmcrj_c2.sendMessage("Woah, so many people are joining, hope this chat will not crash lol");
        mmcrj_c1.sendMessage("Don't worry, I'm the best programmer in the world, so this program can handle anything XD");
        mmcrj_c6.sendMessage("What's going on?");
        mmcrj_c5.sendMessage("Error 503: Service not available");
        mmcrj_c1.sendMessage("Nice try m5");
        
        mmcrj_c1_messages.clear();
        mmcrj_c2_messages.clear();
        mmcrj_c3_messages.clear();
        mmcrj_c4_messages.clear();
        for(Message m: mmcrj_c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c1_messages.add(m);
        for(Message m: mmcrj_c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c2_messages.add(m);
        for(Message m: mmcrj_c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c3_messages.add(m);
        for(Message m: mmcrj_c4.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c4_messages.add(m);
        for(Message m: mmcrj_c5.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c5_messages.add(m);
        for(Message m: mmcrj_c6.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c6_messages.add(m);
        
        // Test against c5 first
        assertArrayEquals(mmcrj_c5_messages.toArray(), mmcrj_c1_messages.subList(12, mmcrj_c1_messages.size()).toArray());
        assertArrayEquals(mmcrj_c5_messages.toArray(), mmcrj_c2_messages.subList(12, mmcrj_c2_messages.size()).toArray());
        assertArrayEquals(mmcrj_c5_messages.toArray(), mmcrj_c3_messages.subList(7, mmcrj_c3_messages.size()).toArray());
        assertArrayEquals(mmcrj_c5_messages.toArray(), mmcrj_c4_messages.subList(3, mmcrj_c4_messages.size()).toArray());
        // Test against c6
        assertArrayEquals(mmcrj_c6_messages.toArray(), mmcrj_c1_messages.subList(12, mmcrj_c1_messages.size()).toArray());
        assertArrayEquals(mmcrj_c6_messages.toArray(), mmcrj_c2_messages.subList(12, mmcrj_c2_messages.size()).toArray());
        assertArrayEquals(mmcrj_c6_messages.toArray(), mmcrj_c3_messages.subList(7, mmcrj_c3_messages.size()).toArray());
        assertArrayEquals(mmcrj_c6_messages.toArray(), mmcrj_c4_messages.subList(3, mmcrj_c4_messages.size()).toArray());
        
        TestClient mmcrj_c7 = TestClient.buildTestClient("m7", mmcrj_c4.me);
        
        // Send 2 messages
        mmcrj_c7.sendMessage("Hi guys, is this the cool network everyone's talking about?");
        mmcrj_c3.sendMessage("Yes, you're in the right place.");
        
        mmcrj_c1_messages.clear();
        mmcrj_c2_messages.clear();
        mmcrj_c3_messages.clear();
        mmcrj_c4_messages.clear();
        mmcrj_c5_messages.clear();
        mmcrj_c6_messages.clear();
        for(Message m: mmcrj_c1.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c1_messages.add(m);
        for(Message m: mmcrj_c2.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c2_messages.add(m);
        for(Message m: mmcrj_c3.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c3_messages.add(m);
        for(Message m: mmcrj_c4.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c4_messages.add(m);
        for(Message m: mmcrj_c5.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c5_messages.add(m);
        for(Message m: mmcrj_c6.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c6_messages.add(m);
        for(Message m: mmcrj_c7.getAllMessages()) if(m.getMessageType() == MessageType.MESSAGE) mmcrj_c7_messages.add(m);
        
        assertArrayEquals(mmcrj_c7_messages.toArray(), mmcrj_c1_messages.subList(17, mmcrj_c1_messages.size()).toArray());
        assertArrayEquals(mmcrj_c7_messages.toArray(), mmcrj_c2_messages.subList(17, mmcrj_c2_messages.size()).toArray());
        assertArrayEquals(mmcrj_c7_messages.toArray(), mmcrj_c3_messages.subList(12, mmcrj_c3_messages.size()).toArray());
        assertArrayEquals(mmcrj_c7_messages.toArray(), mmcrj_c4_messages.subList(8, mmcrj_c4_messages.size()).toArray());
        assertArrayEquals(mmcrj_c7_messages.toArray(), mmcrj_c5_messages.subList(5, mmcrj_c5_messages.size()).toArray());
        assertArrayEquals(mmcrj_c7_messages.toArray(), mmcrj_c6_messages.subList(5, mmcrj_c6_messages.size()).toArray());
        
        mmcrj_c7.quit();
        mmcrj_c6.quit();
        mmcrj_c5.quit();
        mmcrj_c4.quit();
        mmcrj_c3.quit();
        mmcrj_c2.quit();
        mmcrj_c1.quit();
    }
}
