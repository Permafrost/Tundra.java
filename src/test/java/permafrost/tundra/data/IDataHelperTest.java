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
        String[] expected = {"a", "b", "c"};
        assertArrayEquals(expected, IDataHelper.getKeys(document));
    }

    @Test
    public void testGetKeysWithNullIData() throws Exception {
        String[] expected = new String[0];
        assertArrayEquals(expected, IDataHelper.getKeys((IData) null));
    }

    @Test
    public void testGetKeysWithNullPatternString() throws Exception {
        String[] expected = {"a", "b"};
        String patternString = "[ab]";
        assertArrayEquals(expected, IDataHelper.getKeys(document, patternString));
    }

    @Test
    public void testGetKeysWithPatternString() throws Exception {
        String[] expected = {"a", "b", "c"};
        String patternString = null;
        assertArrayEquals(expected, IDataHelper.getKeys(document, patternString));
    }

    @Test
    public void testGetKeysWithPattern() throws Exception {
        String[] expected = {"a", "b"};
        Pattern pattern = Pattern.compile("[ab]");
        assertArrayEquals(expected, IDataHelper.getKeys(document, pattern));
    }

    @Test
    public void testGetKeysWithNullPattern() throws Exception {
        String[] expected = {"a", "b", "c"};
        Pattern pattern = null;
        assertArrayEquals(expected, IDataHelper.getKeys(document, pattern));
    }

    @Test
    public void testGetValues() throws Exception {
        String[] expected = {"1", "2", "3"};
        assertArrayEquals(expected, IDataHelper.getValues(document));
    }

    @Test
    public void testGetValuesWithNullArgument() throws Exception {
        String[] expected = new String[0];
        assertArrayEquals(expected, IDataHelper.getValues(null));
    }

    @Test
    public void testMergeWithNullArgument() throws Exception {
        IData merge = IDataHelper.merge((IData) null);
        assertNotNull(merge);
        assertEquals(0, IDataHelper.size(merge));
    }

    @Test
    public void testMergeWithOneArgument() throws Exception {
        IData merge = IDataHelper.merge(document);
        assertNotNull(merge);
        assertEquals(new IDataMap(document), new IDataMap(merge));
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

    @Test
    public void testSizeWithNullArgument() throws Exception {
        assertEquals(0, IDataHelper.size(null));
    }

    @Test
    public void testSizeWithEmptyArgument() throws Exception {
        assertEquals(0, IDataHelper.size(new IDataMap()));
    }

    @Test
    public void testSizeWithIDataContainingOneElement() throws Exception {
        IDataMap map = new IDataMap();
        map.put("1", "a");
        assertEquals(1, IDataHelper.size(map));
    }

    @Test
    public void testSizeWithIDataContainingMultipleElements() throws Exception {
        IDataMap map = new IDataMap();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        map.put("4", "d");
        assertEquals(4, IDataHelper.size(map));
    }

    @Test
    public void testRemoveElementThatDoesNotExist() throws Exception {
        Object value = IDataHelper.remove(new IDataMap(), "1");
        assertTrue(value == null);
    }

    @Test
    public void testRemoveFromNullIData() throws Exception {
        Object value = IDataHelper.remove(null, "1");
        assertTrue(value == null);
    }

    @Test
    public void testRemoveElementThatExists() throws Exception {
        Object value = IDataHelper.remove(document, "a");
        assertEquals("1", value);
        assertEquals(2, IDataHelper.size(document));
        assertEquals(null, IDataHelper.get(document, "a"));
    }

    @Test
    public void testRenameWithNullArgument() throws Exception {
        IDataHelper.rename(null, "a", "z"); // should not throw exception
    }

    @Test
    public void testRenameElement() throws Exception {
        IDataHelper.rename(document, "a", "z"); // should not throw exception
        assertEquals(3, IDataHelper.size(document));
        assertEquals("1", IDataHelper.get(document, "z"));
        assertEquals(null, IDataHelper.get(document, "a"));
    }

    @Test
    public void testCopyWithNullArgument() throws Exception {
        IDataHelper.copy(null, "a", "z"); // should not throw exception
    }

    @Test
    public void testCopyElement() throws Exception {
        IDataHelper.copy(document, "a", "z");
        assertEquals(4, IDataHelper.size(document));
        assertEquals("1", IDataHelper.get(document, "z"));
        assertEquals("1", IDataHelper.get(document, "a"));
    }

    @Test
    public void testClearWithNullIData() throws Exception {
        IDataHelper.clear(null); // should not throw exception
    }

    @Test
    public void testClearWithNullIDataAndPreserveKeys() throws Exception {
        IDataHelper.clear(null, "a"); // should not throw exception
    }

    @Test
    public void testClearWithPreserveKeys() throws Exception {
        IDataHelper.clear(document, "a", "b");
        assertEquals(2, IDataHelper.size(document));
        assertEquals("1", IDataHelper.get(document, "a"));
        assertEquals("2", IDataHelper.get(document, "b"));
        assertEquals(null, IDataHelper.get(document, "c"));
    }

    @Test
    public void testClearWithPreserveKeyThatDoesNotExist() throws Exception {
        IDataHelper.clear(document, "a", "z");
        assertEquals(1, IDataHelper.size(document));
        assertEquals(null, IDataHelper.get(document, "z"));
        assertEquals("1", IDataHelper.get(document, "a"));
    }

    @Test
    public void testGetWithNullIData() throws Exception {
        assertEquals(null, IDataHelper.get(null, "a"));
    }

    @Test
    public void testGetWithSimpleKey() throws Exception {
        assertEquals("1", IDataHelper.get(document, "a"));
        assertEquals("2", IDataHelper.get(document, "b"));
        assertEquals("3", IDataHelper.get(document, "c"));
    }

    @Test
    public void testGetKeyWithPath() throws Exception {
        IDataMap child = new IDataMap();
        child.put("b", "2");

        IDataMap parent = new IDataMap();
        parent.put("a", child);

        assertEquals("2", IDataHelper.get(parent, "a/b"));
    }

    @Test
    public void testGetKeyWithArrayIndex() throws Exception {
        String[] array = {"1", "2", "3"};

        IDataMap parent = new IDataMap();
        parent.put("a", array);

        assertEquals("1", IDataHelper.get(parent, "a[0]"));
        assertEquals("2", IDataHelper.get(parent, "a[1]"));
        assertEquals("3", IDataHelper.get(parent, "a[2]"));
        assertEquals("3", IDataHelper.get(parent, "a[-1]"));
        assertEquals("2", IDataHelper.get(parent, "a[-2]"));
        assertEquals("1", IDataHelper.get(parent, "a[-3]"));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetKeyWithArrayIndexOutOfBounds() throws Exception {
        String[] array = {"1", "2", "3"};

        IDataMap parent = new IDataMap();
        parent.put("a", array);

        Object value = IDataHelper.get(parent, "a[3]");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetKeyWithNegativeArrayIndexOutOfBounds() throws Exception {
        String[] array = {"1", "2", "3"};

        IDataMap parent = new IDataMap();
        parent.put("a", array);

        Object value = IDataHelper.get(parent, "a[-4]");
    }

    @Test
    public void testGetKeyWithIndex() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "1");
        cursor.insertAfter("a", "2");
        cursor.destroy();

        assertEquals("1", IDataHelper.get(document, "a(0)"));
        assertEquals("2", IDataHelper.get(document, "a(1)"));
    }

    @Test
    public void testGetKeyWithIndexOutOfBounds() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "1");
        cursor.insertAfter("a", "2");
        cursor.destroy();

        assertEquals(null, IDataHelper.get(document, "a(2)"));
    }

    @Test
    public void testGetKeyWithPathAndArrayIndex() throws Exception {
        String[] array = {"1", "2", "3"};
        IDataMap child = new IDataMap();
        child.put("b", array);
        IDataMap parent = new IDataMap();
        parent.put("a", child);

        assertEquals("1", IDataHelper.get(parent, "a/b[0]"));
        assertEquals("2", IDataHelper.get(parent, "a/b[1]"));
        assertEquals("3", IDataHelper.get(parent, "a/b[2]"));
        assertEquals("3", IDataHelper.get(parent, "a/b[-1]"));
        assertEquals("2", IDataHelper.get(parent, "a/b[-2]"));
        assertEquals("1", IDataHelper.get(parent, "a/b[-3]"));
    }

    @Test
    public void testGetKeyWithPathAndIndex() throws Exception {
        IData child = IDataFactory.create();
        IDataCursor cursor = child.getCursor();
        cursor.insertAfter("b", "1");
        cursor.insertAfter("b", "2");
        cursor.insertAfter("b", "3");
        cursor.insertAfter("b", "4");
        cursor.destroy();

        IDataMap parent = new IDataMap();
        parent.put("a", child);

        assertEquals("1", IDataHelper.get(parent, "a/b"));
        assertEquals("1", IDataHelper.get(parent, "a/b(0)"));
        assertEquals("2", IDataHelper.get(parent, "a/b(1)"));
        assertEquals("3", IDataHelper.get(parent, "a/b(2)"));
        assertEquals("4", IDataHelper.get(parent, "a/b(3)"));
    }

    @Test
    public void testDropWithNullIData() throws Exception {
        IDataHelper.drop(null, "a/b(2)"); // should not throw exception
    }

    @Test
    public void testDropWithArrayIndex() throws Exception {
        String[] array = {"1", "2", "3"};
        String[] expected = {"1", "3"};

        IDataMap parent = new IDataMap();
        parent.put("a", array);

        IDataHelper.drop(parent, "a[1]");

        assertArrayEquals(expected, (String[]) IDataHelper.get(parent, "a"));
    }

    @Test
    public void testDropWithSimpleKey() throws Exception {
        IDataHelper.drop(document, "a");
        assertEquals(2, IDataHelper.size(document));
        assertEquals(null, IDataHelper.get(document, "a"));
    }

    @Test
    public void testDropKeyWithPathAndIndex() throws Exception {
        IData child = IDataFactory.create();
        IDataCursor cursor = child.getCursor();
        cursor.insertAfter("b", "1");
        cursor.insertAfter("b", "2");
        cursor.insertAfter("b", "3");
        cursor.insertAfter("b", "4");
        cursor.destroy();

        IDataMap parent = new IDataMap();
        parent.put("a", child);

        IDataHelper.drop(parent, "a/b(2)");
        assertEquals(3, IDataHelper.size(child));
        assertEquals("4", IDataHelper.get(parent, "a/b(2)"));
    }

    @Test
    public void testDropWithPathAndArrayIndex() throws Exception {
        String[] array = {"1", "2", "3"};
        String[] expected = {"1", "3"};

        IDataMap child = new IDataMap();
        child.put("b", array);
        IDataMap parent = new IDataMap();
        parent.put("a", child);

        IDataHelper.drop(parent, "a/b[1]");

        assertArrayEquals(expected, (String[]) IDataHelper.get(parent, "a/b"));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testDropWithArrayIndexOutOfBounds() throws Exception {
        String[] array = {"1", "2", "3"};

        IDataMap parent = new IDataMap();
        parent.put("a", array);

        IDataHelper.drop(parent, "a[3]");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testDropWithNegativeArrayIndexOutOfBounds() throws Exception {
        String[] array = {"1", "2", "3"};

        IDataMap parent = new IDataMap();
        parent.put("a", array);

        IDataHelper.drop(parent, "a[-4]");
    }

    @Test
    public void testDropWithIndexOutOfBounds() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "1");
        cursor.insertAfter("a", "2");
        cursor.destroy();

        IDataHelper.drop(document, "a(2)");

        assertEquals(2, IDataHelper.size(document));
        assertEquals("1", IDataHelper.get(document, "a(0)"));
        assertEquals("1", IDataHelper.get(document, "a"));
        assertEquals("2", IDataHelper.get(document, "a(1)"));
    }

    @Test
    public void testPutWithNullIData() throws Exception {
        IData document = IDataHelper.put(null, "a", "1");
        assertEquals(1, IDataHelper.size(document));
        assertEquals("1", IDataHelper.get(document, "a"));
    }

    @Test
    public void testPutWithSimpleKey() throws Exception {
        IDataMap map = new IDataMap();
        map.put("a", "1");

        IData document = IDataHelper.put(map, "b", "2");
        assertEquals(2, IDataHelper.size(document));
        assertEquals("1", IDataHelper.get(document, "a"));
        assertEquals("2", IDataHelper.get(document, "b"));
    }

    @Test
    public void testPutWithPath() throws Exception {
        document = IDataHelper.put(document, "a/b", "1");
        assertEquals("1", IDataHelper.get(document, "a/b"));
    }

    @Test
    public void testPutWithArrayIndex() throws Exception {
        String[] expected = {"1"};
        document = IDataHelper.put(document, "z[0]", "1");
        assertArrayEquals(expected, (String[]) IDataHelper.get(document, "z"));
    }

    @Test
    public void testPutWithPathAndArrayIndex() throws Exception {
        String[] expected = {"1"};
        document = IDataHelper.put(document, "a/b[0]", "1");
        assertArrayEquals(expected, (String[]) IDataHelper.get(document, "a/b"));
    }

    @Test
    public void testPutWithIndex() throws Exception {
        document = IDataHelper.put(document, "a(2)", "4");
        assertEquals(5, IDataHelper.size(document));
        assertEquals("1", IDataHelper.get(document, "a(0)"));
        assertEquals(null, IDataHelper.get(document, "a(1)"));
        assertEquals("4", IDataHelper.get(document, "a(2)"));
    }

    @Test
    public void testPutWithPathAndIndex() throws Exception {
        document = IDataHelper.put(document, "y/z(2)", "4");
        assertEquals(4, IDataHelper.size(document));
        assertEquals(null, IDataHelper.get(document, "y/z(0)"));
        assertEquals(null, IDataHelper.get(document, "y/z(1)"));
        assertEquals("4", IDataHelper.get(document, "y/z(2)"));
    }
}