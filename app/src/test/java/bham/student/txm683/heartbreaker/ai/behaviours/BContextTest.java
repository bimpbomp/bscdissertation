package bham.student.txm683.heartbreaker.ai.behaviours;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BContextTest {

    @Test
    public void pairHasType() {
        BContext bContext = new BContext();

        bContext.addPair(BKeyType.VIEW_RANGE, 100);

        assertTrue(bContext.pairHasType(BKeyType.VIEW_RANGE));

        bContext.addPair(BKeyType.VIEW_RANGE, "Str");

        assertFalse(bContext.pairHasType(BKeyType.VIEW_RANGE));
    }
}