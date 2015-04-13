package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class IDataHelperTest {
    IData document = IDataFactory.create();

    @Before
    public void setUp() throws Exception {
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "a", "1");
        IDataUtil.put(cursor, "b", "2");
        IDataUtil.put(cursor, "c", "3");
        cursor.destroy();
    }

    @Test
    public void testGetKeys() throws Exception {
        String[] expected = { "a", "b", "c" };
        assertArrayEquals(expected, IDataHelper.getKeys(document));
    }

    @Test
    public void testGetKeysWithNullIData() throws Exception {
        String[] expected = new String[0];
        assertArrayEquals(expected, IDataHelper.getKeys((IData)null));
    }

    @Test
    public void testGetKeysWithNullPatternString() throws Exception {
        String[] expected = { "a", "b" };
        String patternString = "[ab]";
        assertArrayEquals(expected, IDataHelper.getKeys(document, patternString));
    }

    @Test
    public void testGetKeysWithPatternString() throws Exception {
        String[] expected = { "a", "b", "c" };
        String patternString = null;
        assertArrayEquals(expected, IDataHelper.getKeys(document, patternString));
    }

    @Test
    public void testGetKeysWithPattern() throws Exception {
        String[] expected = { "a", "b" };
        Pattern pattern = Pattern.compile("[ab]");
        assertArrayEquals(expected, IDataHelper.getKeys(document, pattern));
    }

    @Test
    public void testGetKeysWithNullPattern() throws Exception {
        String[] expected = { "a", "b", "c" };
        Pattern pattern = null;
        assertArrayEquals(expected, IDataHelper.getKeys(document, pattern));
    }

    @Test
    public void testGetValues() throws Exception {
        String[] expected = { "1", "2", "3" };
        assertArrayEquals(expected, IDataHelper.getValues(document));
    }

    @Test
    public void testGetValuesWithNullArgument() throws Exception {
        String[] expected = new String[0];
        assertArrayEquals(expected, IDataHelper.getValues(null));
    }

    @Test
    public void testMergeWithNullArgument() throws Exception {
        IData merge = IDataHelper.merge((IData)null);
        assertNotNull(merge);
        assertEquals(0, IDataHelper.size(merge));
    }

    @Test
    public void testMergeWithOneArgument() throws Exception {
        IData merge = IDataHelper.merge(document);
        assertNotNull(merge);
        assertEquals(new IterableIData(document), new IterableIData(merge));
    }

    @Test
    public void testMergeWithTwoArguments() throws Exception {
        IData secondDocument = IDataFactory.create();
        IDataCursor cursor = secondDocument.getCursor();
        IDataUtil.put(cursor, "d", "4");
        cursor.destroy();

        IData merge = IDataHelper.merge(document, secondDocument);
        assertNotNull(merge);
        assertEquals(4, IDataHelper.size(merge));

        cursor = merge.getCursor();
        assertEquals("1", IDataUtil.getString(cursor, "a"));
        assertEquals("2", IDataUtil.getString(cursor, "b"));
        assertEquals("3", IDataUtil.getString(cursor, "c"));
        assertEquals("4", IDataUtil.getString(cursor, "d"));
        cursor.destroy();
    }
}