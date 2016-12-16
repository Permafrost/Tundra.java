package permafrost.tundra.lang;

import org.junit.Test;
import static org.junit.Assert.*;

public class TableHelperTest {
    @Test
    public void testStringifyWithIntegerTable() throws Exception {
        Integer[][] table = new Integer[2][];

        table[0] = new Integer[2];
        table[1] = new Integer[2];

        table[0][0] = new Integer("1");
        table[0][1] = new Integer("2");
        table[1][0] = new Integer("3");
        table[1][1] = new Integer("4");

        assertEquals("[[1, 2], [3, 4]]", TableHelper.stringify(table));
    }

    @Test public void testStringifyWhenExcludingNullItems() throws Exception {
        String[][] table = new String[][]{{null, "2"}, {"3", null, "4"}, null, {null, "7"}};
        assertEquals("[[2], [3, 4], [7]]", TableHelper.stringify(table, ", ", false));
    }

    @Test public void testStringifyWhenIncludingNullItems() throws Exception {
        String[][] table = new String[][]{{null, "2"}, {"3", null, "4"}, null, {null, "7"}};
        assertEquals("[[null, 2], [3, null, 4], null, [null, 7]]", TableHelper.stringify(table, ", ", true));
    }
}