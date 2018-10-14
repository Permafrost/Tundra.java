package permafrost.tundra.server;

import com.wm.lang.ns.NSName;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeHelperTest {

    @Test
    public void isSibling() {
        NSName self, sibling, cousin;

        self = NSName.create("a.b.c:1");
        sibling = NSName.create("a.b.c:2");
        cousin = NSName.create("a.b.d:1");

        assertTrue(NodeHelper.isSibling(self, sibling));
        assertFalse(NodeHelper.isSibling(self, cousin));

        self = NSName.create("a.b.c");
        sibling = NSName.create("a.b.d");
        cousin = NSName.create("a.c.d");

        assertTrue(NodeHelper.isSibling(self, sibling));
        assertFalse(NodeHelper.isSibling(self, cousin));
    }
}