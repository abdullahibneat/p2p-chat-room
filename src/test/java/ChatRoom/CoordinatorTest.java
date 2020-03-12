package ChatRoom;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test coordinator status.
 *
 * @author iAbdu
 */
public class CoordinatorTest {

    /**
     * Test if first member can start a network and be the coordinator.
     */
    @Test
    public void testFirstMemberIsCoordinator() throws Exception {
        System.out.println("testFirstMemberIsCoordinator()");
        
        TestClient fmic_c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        
        assertEquals(fmic_c1.me.isCoordinator(), true);
        
        fmic_c1.quit();
    }
    
    /**
     * Test if someone else becomes coordinator, when first coordinator leaves.
     */
    @Test
    public void testFirstCoordinatorLeaves() throws Exception {
        System.out.println("testCoordinatorLeaves()");
        
        TestClient c1 = TestClient.buildTestClient("m1", TestClient.EMPTY_MEMBER);
        TestClient c2 = TestClient.buildTestClient("m2", c1.me);
        TestClient c3 = TestClient.buildTestClient("m3", c2.me);
        
        // First coordinator leaves
        c1.quit();
        
        Thread.sleep(5000);
        
        assertEquals(true, c2.me.isCoordinator());
        
        // Ensure group can still communicate
        c3.sendMessage("Can you read this message m2?");
        
        assertEquals(c3.getAllMessages().get(c3.getAllMessages().size() - 1), c2.getAllMessages().get(c2.getAllMessages().size() - 1));
    }
}
