package ChatRoom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testing group formation and connection.
 * 
 * Variable names for client follows the following naming convention:
 * [first letter of each word in method name]_c[nth member]
 * This is to ensure no two client instances with the same variable name in different
 * methods interfere with each other
 *
 * @author iAbdu
 */
public class GroupFormationTest {

    /**
     * Test if first member can start a network and be the coordinator.
     */
    @Test
    public void testNetowrkCreation() throws Exception {
        System.out.println("testNetowrkCreation()");
        
        TestClient nc_c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        
        assertEquals(nc_c1.online, true); // 12 is online only if network is successfully created.
        assertEquals(nc_c1.me.isCoordinator(), true);
        
        nc_c1.quit();
    }
    
    /**
     * Test if one member can join the network.
     */
    @Test
    public void testSingleMemberConnection() throws Exception {
        System.out.println("testSingleMemberConnection()");
        TestClient smc_c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        TestClient smc_c2 = TestClient.buildTestClient("m2", smc_c1.me);
        
        assertEquals(smc_c2.online, true); // c2 is online only if connection is established.
        
        assertEquals(smc_c1.getMembers().size(), smc_c2.getMembers().size()); // m1 and m2 sjould have the same number of members (1)
        assertEquals(smc_c1.getMembers().get(0), smc_c2.me); // the only member for c1 should be m2        
        assertEquals(smc_c2.getMembers().get(0), smc_c1.me); // the only member for c2 should be m1
        
        smc_c2.quit();
        smc_c1.quit();                
    }
    
    /**
     * Test if multiple members can join the network by connecting to the coordinator.
     */
    @Test
    public void testMultiMemberConnectionKnowingCoordinator() throws Exception {
        System.out.println("testMultiMemberConnectionKnowingCoordinator()");
        
        TestClient mmckc_c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        TestClient mmckc_c2 = TestClient.buildTestClient("m2", mmckc_c1.me);
        TestClient mmckc_c3 = TestClient.buildTestClient("m3", mmckc_c1.me);
        TestClient mmckc_c4 = TestClient.buildTestClient("m4", mmckc_c1.me);
        TestClient mmckc_c5 = TestClient.buildTestClient("m5", mmckc_c1.me);
        TestClient mmckc_c6 = TestClient.buildTestClient("m6", mmckc_c1.me);
        TestClient mmckc_c7 = TestClient.buildTestClient("m7", mmckc_c1.me);
        
        Member[] mmckc_allMembers = {mmckc_c1.me, mmckc_c2.me, mmckc_c3.me, mmckc_c4.me, mmckc_c5.me, mmckc_c6.me, mmckc_c7.me};
        
        Thread.sleep(5000);
        
        // Check if all members know each other.
        assertArrayEquals(Arrays.stream(mmckc_allMembers).filter(m -> !m.equals(mmckc_c1.me)).toArray(), mmckc_c1.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckc_allMembers).filter(m -> !m.equals(mmckc_c2.me)).toArray(), mmckc_c2.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckc_allMembers).filter(m -> !m.equals(mmckc_c3.me)).toArray(), mmckc_c3.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckc_allMembers).filter(m -> !m.equals(mmckc_c4.me)).toArray(), mmckc_c4.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckc_allMembers).filter(m -> !m.equals(mmckc_c5.me)).toArray(), mmckc_c5.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckc_allMembers).filter(m -> !m.equals(mmckc_c6.me)).toArray(), mmckc_c6.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckc_allMembers).filter(m -> !m.equals(mmckc_c7.me)).toArray(), mmckc_c7.getMembers().toArray());
        
        mmckc_c7.quit();
        mmckc_c6.quit();
        mmckc_c5.quit();
        mmckc_c4.quit();
        mmckc_c3.quit();
        mmckc_c2.quit();
        mmckc_c1.quit();
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
    public void testMultiMemberConnectionKnowingAnyExistingMember() throws Exception {
        System.out.println("testMultiMemberConnectionKnowingAnyExistingMember()");
        
        TestClient mmckaem_c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        TestClient mmckaem_c2 = TestClient.buildTestClient("m2", mmckaem_c1.me);
        TestClient mmckaem_c3 = TestClient.buildTestClient("m3", mmckaem_c2.me);
        TestClient mmckaem_c4 = TestClient.buildTestClient("m4", mmckaem_c2.me);
        TestClient mmckaem_c5 = TestClient.buildTestClient("m5", mmckaem_c3.me);
        TestClient mmckaem_c6 = TestClient.buildTestClient("m6", mmckaem_c1.me);
        TestClient mmckaem_c7 = TestClient.buildTestClient("m7", mmckaem_c4.me);
        
        Member[] mmckaem_allMembers = {mmckaem_c1.me, mmckaem_c2.me, mmckaem_c3.me, mmckaem_c4.me, mmckaem_c5.me, mmckaem_c6.me, mmckaem_c7.me};
        
        Thread.sleep(5000);
        
        // Check if all members know each other.
        assertArrayEquals(Arrays.stream(mmckaem_allMembers).filter(m -> !m.equals(mmckaem_c1.me)).toArray(), mmckaem_c1.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckaem_allMembers).filter(m -> !m.equals(mmckaem_c2.me)).toArray(), mmckaem_c2.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckaem_allMembers).filter(m -> !m.equals(mmckaem_c3.me)).toArray(), mmckaem_c3.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckaem_allMembers).filter(m -> !m.equals(mmckaem_c4.me)).toArray(), mmckaem_c4.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckaem_allMembers).filter(m -> !m.equals(mmckaem_c5.me)).toArray(), mmckaem_c5.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckaem_allMembers).filter(m -> !m.equals(mmckaem_c6.me)).toArray(), mmckaem_c6.getMembers().toArray());
        assertArrayEquals(Arrays.stream(mmckaem_allMembers).filter(m -> !m.equals(mmckaem_c7.me)).toArray(), mmckaem_c7.getMembers().toArray());
        
        mmckaem_c7.quit();
        mmckaem_c6.quit();
        mmckaem_c5.quit();
        mmckaem_c4.quit();
        mmckaem_c3.quit();
        mmckaem_c2.quit();
        mmckaem_c1.quit();
    }
    
    /**
     * Test that one one member leaves, everyone is notified and the chat is working as normal.
     * Based on previous scenario.
     */
    @Test
    public void testOneMemberLeaves() throws Exception {
        System.out.println("testOneMemberLeaves()");
        
        TestClient oml_c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        TestClient oml_c2 = TestClient.buildTestClient("c2", oml_c1.me);
        
        oml_c1.sendMessage("Hi m2!");
        oml_c2.sendMessage("hi! how are you?");
        oml_c1.sendMessage("I'm good");
        oml_c1.sendMessage("Remember to share your details with others so more poeple will join!");
        oml_c2.sendMessage("I shared my details already, more poeple will come don't worry :)");
        
        TestClient oml_c3 = TestClient.buildTestClient("c3", oml_c2.me);
        
        oml_c3.sendMessage("Yo guys!");
        oml_c2.sendMessage("Hey m3! You finally joined...");
        oml_c3.sendMessage("Yes! I'm so excited");
        oml_c1.sendMessage("Welcome c3! Nice to meet you");
        
        TestClient oml_c4 = TestClient.buildTestClient("c4", oml_c2.me);
        
        oml_c3.sendMessage("Nice to meet you too m1");
        oml_c1.sendMessage("Another member! How are you m4?");
        oml_c4.sendMessage("Hi everyone!");
        
        TestClient oml_c5 = TestClient.buildTestClient("c5", oml_c3.me);
        TestClient oml_c6 = TestClient.buildTestClient("c6", oml_c1.me);
        
        oml_c2.sendMessage("Woah, so many people are joining, hope this chat will not crash lol");
        oml_c1.sendMessage("Don't worry, I'm the best programmer in the world, so this program can handle anything XD");
        oml_c6.sendMessage("What's going on?");
        
        /**
         * m3 leaves
         */
        Member m3 = oml_c3.me;
        oml_c3.quit();
        
        oml_c5.sendMessage("Error 503: Service not available");
        oml_c1.sendMessage("Nice try m5");
        
        TestClient oml_c7 = TestClient.buildTestClient("c7", oml_c4.me);
        
        oml_c7.sendMessage("Hi guys, is this the cool network everyone's talking about?");
        
        // Check whether everyone removed m3 from their list
        assertTrue(oml_c1.getMembers().contains(m3));
        assertTrue(oml_c2.getMembers().contains(m3));
        assertTrue(oml_c4.getMembers().contains(m3));
        assertTrue(oml_c5.getMembers().contains(m3));
        assertTrue(oml_c6.getMembers().contains(m3));
        assertTrue(oml_c7.getMembers().contains(m3));
        
        oml_c7.quit();
        oml_c6.quit();
        oml_c5.quit();
        oml_c4.quit();
        oml_c2.quit();
        oml_c1.quit();
    }
}
