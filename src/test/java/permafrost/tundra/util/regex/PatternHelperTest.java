package permafrost.tundra.util.regex;

import org.junit.Test;
import permafrost.tundra.lang.StringHelper;
import static org.junit.Assert.*;

public class PatternHelperTest {
    @Test
    public void testQuoteWithNull() throws Exception {
        assertEquals(null, PatternHelper.quote((String)null));
        assertEquals(null, PatternHelper.quote((String[])null));
    }


    @Test
    public void testQuoteWithReservedCharacters() throws Exception {
        assertTrue(StringHelper.match("$1.00", PatternHelper.quote("$1.00")));
    }

    @Test
    public void testQuoteWithArray() throws Exception {
        String[] strings = { "$1.00", "$2.00" };
        assertTrue(StringHelper.match("$1.00", PatternHelper.quote(strings)));
        assertTrue(StringHelper.match("$2.00", PatternHelper.quote(strings)));
        assertTrue(!StringHelper.match("$3.00", PatternHelper.quote(strings)));
    }
}