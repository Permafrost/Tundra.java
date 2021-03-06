package permafrost.tundra.lang;

import org.junit.Test;
import static org.junit.Assert.*;

public class TableHelperTest {
    @Test
    public void testStringifyWithIntegerTable() throws Exception {
        Integer[][] table = new Integer[2][];

        table[0] = new Integer[2];
        table[1] = new Integer[2];

        table[0][0] = 1;
        table[0][1] = 2;
        table[1][0] = 3;
        table[1][1] = 4;

        assertEquals("[[1, 2], [3, 4]]", TableHelper.stringify(table));
    }

    @Test
    public void testStringifyWhenExcludingNullItems() throws Exception {
        String[][] table = new String[][]{{null, "2"}, {"3", null, "4"}, null, {null, "7"}};
        assertEquals("[[2], [3, 4], [7]]", TableHelper.stringify(table, ", ", Sanitization.REMOVE_NULLS));
    }

    @Test
    public void testStringifyWhenIncludingNullItems() throws Exception {
        String[][] table = new String[][]{{null, "2"}, {"3", null, "4"}, null, {null, "7"}};
        assertEquals("[[null, 2], [3, null, 4], null, [null, 7]]", TableHelper.stringify(table, ", "));
    }


    @Test
    public void testToStringTableWithNull() throws Exception {
        assertArrayEquals(null, TableHelper.toStringTable(null));
    }

    @Test
    public void testToStringTableWithIntegerTable() throws Exception {
        Integer[][] table = new Integer[2][];

        table[0] = new Integer[2];
        table[1] = new Integer[2];

        table[0][0] = 1;
        table[0][1] = 2;
        table[1][0] = 3;
        table[1][1] = 4;

        assertArrayEquals(new String[][] { { "1", "2" }, { "3", "4" } }, TableHelper.toStringTable(table));
    }
}