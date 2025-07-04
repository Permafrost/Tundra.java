package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import org.junit.Before;
import org.junit.Test;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.Sanitization;
import permafrost.tundra.server.SystemHelper;
import permafrost.tundra.xml.dom.DocumentHelper;
import permafrost.tundra.xml.sax.InputSourceHelper;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void testGetAbsoluteXPath() throws Exception {
        String xmldata = "<test><a>1</a><b>2</b></test>";
        IDataMap pipeline = new IDataMap();

        pipeline.put("node", DocumentHelper.parse(InputSourceHelper.normalize(InputStreamHelper.normalize(xmldata))));

        Object value = IDataHelper.get(pipeline, new IDataMap(), "/node/test/a");

        assertEquals("1", value);
    }

    @Test
    public void testGetRelativeXPath() throws Exception {
        String xmldata = "<test><a>1</a><b>2</b></test>";
        IDataMap scope = new IDataMap();

        scope.put("node", DocumentHelper.parse(InputSourceHelper.normalize(InputStreamHelper.normalize(xmldata))));

        Object value = IDataHelper.get(null, scope, "node/test/a");

        assertEquals("1", value);
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
    public void testSizeWithMissingKey() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "a1");
        cursor.insertAfter("b", "b1");
        cursor.destroy();

        assertEquals(0, IDataHelper.size(IDataFactory.create(), "c"));
    }

    @Test
    public void testSizeWithKeyOccursOnce() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "a1");
        cursor.insertAfter("b", "b1");
        cursor.destroy();

        assertEquals(1, IDataHelper.size(document, "a"));
    }

    @Test
    public void testSizeWithKeyThatOccursMultipleTimes() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "a1");
        cursor.insertAfter("a", "a2");
        cursor.insertAfter("a", "a3");
        cursor.insertAfter("b", "b1");
        cursor.destroy();

        assertEquals(3, IDataHelper.size(document, "a"));
    }


    @Test
    public void testSizeWithFullyQualifiedKeyThatOccursMultipleTimes() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "a1");
        cursor.insertAfter("a", "a2");
        cursor.insertAfter("a", "a3");
        cursor.insertAfter("b", "b1");
        cursor.destroy();

        IDataMap parent = new IDataMap();
        parent.put("d", document);

        assertEquals(3, IDataHelper.size(parent, "d/a"));
    }

    @Test
    public void testSizeWithFullyQualifiedNthKeyThatOccursMultipleTimes() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "a1");
        cursor.insertAfter("a", "a2");
        cursor.insertAfter("a", "a3");
        cursor.insertAfter("b", "b1");
        cursor.destroy();

        IDataMap parent = new IDataMap();
        parent.put("d", document);

        assertEquals(1, IDataHelper.size(parent, "d/a(1)"));
    }

    @Test
    public void testRemoveElementThatDoesNotExist() throws Exception {
        Object value = IDataHelper.remove(new IDataMap(), "1");
        assertTrue(value == null);
    }

    @Test
    public void testRemoveFromNullIData() throws Exception {
        Object value = IDataHelper.remove((IData)null, "1");
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
        assertEquals(null, IDataHelper.get((IData)null, "a"));
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
        String[] array = { "1", "2", "3" };

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
        String[] array = { "1", "2", "3" };

        IDataMap parent = new IDataMap();
        parent.put("a", array);

        Object value = IDataHelper.get(parent, "a[3]");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetKeyWithNegativeArrayIndexOutOfBounds() throws Exception {
        String[] array = { "1", "2", "3" };

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
        String[] array = { "1", "2", "3" };
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
    public void testGetKeyWithNoIndexFromPathWithIDataArray() throws Exception {
        String[] array = { "1", "2", "3" };
        String[] expected = { "1", "3" };

        IDataMap child1 = new IDataMap();
        child1.put("a", "1");
        child1.put("b", "1");

        IDataMap child2 = new IDataMap();
        child2.put("a", "2");
        child2.put("b", "2");

        IDataMap parent = new IDataMap();
        parent.put("c", new IData[] { child1, child2 });

        Object value = IDataHelper.get(parent, "c/a");

        assertNotNull(value);
        assertEquals(String[].class, value.getClass());

        String[] arrayValue = (String[])value;

        assertEquals(2, arrayValue.length);
        assertEquals("1", arrayValue[0]);
        assertEquals("2", arrayValue[1]);
    }

    @Test
    public void testDropWithNullIData() throws Exception {
        IDataHelper.drop(null, "a/b(2)"); // should not throw exception
    }

    @Test
    public void testDropWithArrayIndex() throws Exception {
        String[] array = { "1", "2", "3" };
        String[] expected = { "1", "3" };

        IDataMap parent = new IDataMap();
        parent.put("a", array);

        IDataHelper.drop(parent, "a[1]");

        assertArrayEquals(expected, (String[])IDataHelper.get(parent, "a"));
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
        String[] array = { "1", "2", "3" };
        String[] expected = { "1", "3" };

        IDataMap child = new IDataMap();
        child.put("b", array);
        IDataMap parent = new IDataMap();
        parent.put("a", child);

        IDataHelper.drop(parent, "a/b[1]");

        assertArrayEquals(expected, (String[])IDataHelper.get(parent, "a/b"));
    }

    @Test
    public void testDropOfKeyInIDataArray() throws Exception {
        String[] array = { "1", "2", "3" };
        String[] expected = { "1", "3" };

        IDataMap child1 = new IDataMap();
        child1.put("a", "1");
        child1.put("b", "1");

        IDataMap expected1 = new IDataMap();
        expected1.put("b", "1");

        IDataMap child2 = new IDataMap();
        child2.put("a", "2");
        child2.put("b", "2");

        IDataMap expected2 = new IDataMap();
        expected2.put("a", "2");

        IDataMap parent = new IDataMap();
        parent.put("c", new IData[] { child1, child2 });

        IDataHelper.drop(parent, "c/a");

        assertEquals(null, child1.get("a"));
        assertEquals("1", child1.get("b"));
        assertEquals(null, child2.get("a"));
        assertEquals("2", child2.get("b"));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testDropWithArrayIndexOutOfBounds() throws Exception {
        String[] array = { "1", "2", "3" };

        IDataMap parent = new IDataMap();
        parent.put("a", array);

        IDataHelper.drop(parent, "a[3]");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testDropWithNegativeArrayIndexOutOfBounds() throws Exception {
        String[] array = { "1", "2", "3" };

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
        IData document = IDataHelper.put((IData)null, "a", "1");
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
        String[] expected = { "1" };
        document = IDataHelper.put(document, "z[0]", "1");
        assertArrayEquals(expected, (String[])IDataHelper.get(document, "z"));
    }

    @Test
    public void testPutWithPathAndArrayIndex() throws Exception {
        String[] expected = { "1" };
        document = IDataHelper.put(document, "a/b[0]", "1");
        assertArrayEquals(expected, (String[])IDataHelper.get(document, "a/b"));
    }


    @Test
    public void testPutWithLiteralKeyThatIncludesPathAndArrayIndex() throws Exception {
        document = IDataHelper.put(document, "a/b[0]", "1", true);
        assertEquals("1", (String)IDataHelper.get(document, "a/b[0]", true));
        assertEquals("1", (String)IDataHelper.get(document, "a/b[0]", false));
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

    @Test
    public void testRenameWithNullSource() throws Exception {
        IDataHelper.rename(document, null, "d");
        assertEquals(null, IDataHelper.get(document, null));
        assertEquals(null, IDataHelper.get(document, "d"));
    }

    @Test
    public void testRenameWithNullTarget() throws Exception {
        IDataHelper.rename(document, "c", null);
        assertEquals("3", IDataHelper.get(document, "c"));
        assertEquals(null, IDataHelper.get(document, null));
    }

    @Test
    public void testRenameWithNullSourceAndTarget() throws Exception {
        IDataHelper.rename(document, null, null);
        assertEquals(null, IDataHelper.get(document, null));
    }

    @Test
    public void testRenameWithEqualSourceAndTarget() throws Exception {
        IDataHelper.rename(document, "c", "c");
        assertEquals("3", IDataHelper.get(document, "c"));
    }

    @Test
    public void testRename() throws Exception {
        assertEquals("3", IDataHelper.get(document, "c"));
        IDataHelper.rename(document, "c", "d");
        assertEquals(null, IDataHelper.get(document, "c"));
        assertEquals("3", IDataHelper.get(document, "d"));
    }

    @Test
    public void testCopyWithNullSource() throws Exception {
        IDataHelper.copy(document, null, "d");
        assertEquals(null, IDataHelper.get(document, null));
        assertEquals(null, IDataHelper.get(document, "d"));
    }

    @Test
    public void testCopyWithNullTarget() throws Exception {
        IDataHelper.copy(document, "c", null);
        assertEquals("3", IDataHelper.get(document, "c"));
        assertEquals(null, IDataHelper.get(document, null));
    }

    @Test
    public void testCopyWithNullSourceAndTarget() throws Exception {
        IDataHelper.copy(document, null, null);
        assertEquals(null, IDataHelper.get(document, null));
    }

    @Test
    public void testCopyWithEqualSourceAndTarget() throws Exception {
        IDataHelper.copy(document, "c", "c");
        assertEquals("3", IDataHelper.get(document, "c"));
    }

    @Test
    public void testCopy() throws Exception {
        IDataHelper.copy(document, "c", "d");
        assertEquals("3", IDataHelper.get(document, "c"));
        assertEquals("3", IDataHelper.get(document, "d"));
    }

    @Test
    public void testSortWithMultipleKeys() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<IDataXMLCoder version=\"1.0\">\n" +
                "  <record javaclass=\"com.wm.data.ISMemDataImpl\">\n" +
                "    <array name=\"array\" type=\"record\" depth=\"1\">\n" +
                "      <record javaclass=\"com.wm.util.Values\">\n" +
                "        <value name=\"string\">a</value>\n" +
                "        <value name=\"integer\">26</value>\n" +
                "        <value name=\"decimal\">43.2</value>\n" +
                "        <value name=\"datetime\">02-03-2015</value>\n" +
                "        <value name=\"duration\">P1Y</value>\n" +
                "      </record>\n" +
                "      <record javaclass=\"com.wm.util.Values\">\n" +
                "        <value name=\"string\">z</value>\n" +
                "        <value name=\"integer\">99</value>\n" +
                "        <value name=\"decimal\">99.9</value>\n" +
                "        <value name=\"datetime\">01-01-2014</value>\n" +
                "        <value name=\"duration\">P1D</value>\n" +
                "      </record>\n" +
                "      <record javaclass=\"com.wm.util.Values\">\n" +
                "        <value name=\"string\">a</value>\n" +
                "        <value name=\"integer\">25</value>\n" +
                "        <value name=\"decimal\">64.345</value>\n" +
                "        <value name=\"datetime\">01-01-2014</value>\n" +
                "        <value name=\"duration\">PT1S</value>\n" +
                "      </record>\n" +
                "    </array>\n" +
                "  </record>\n" +
                "</IDataXMLCoder>\n";

        IDataMap map = new IDataMap(new IDataXMLParser().parse(InputStreamHelper.normalize(xml)));
        IData[] array = (IData[])map.get("array");

        IDataComparisonCriterion c1 = new IDataComparisonCriterion("string", "string", false);
        IDataComparisonCriterion c2 = new IDataComparisonCriterion("integer", "integer", false);

        IData[] result = IDataHelper.sort(array, c1, c2);

        assertEquals(3, result.length);
        IDataMap first = new IDataMap(result[0]);
        assertEquals("a", first.get("string"));
        assertEquals("25", first.get("integer"));

        IDataMap second = new IDataMap(result[1]);
        assertEquals("a", second.get("string"));
        assertEquals("26", second.get("integer"));

        IDataMap third = new IDataMap(result[2]);
        assertEquals("z", third.get("string"));
        assertEquals("99", third.get("integer"));
    }

    @Test
    public void testSortWithMultipleStringKeysAscending() throws Exception {
        IData[] array = new IData[6];

        IDataMap item1 = new IDataMap();
        item1.put("key1", "z");
        item1.put("key2", "z");

        IDataMap item2 = new IDataMap();
        item2.put("key1", "z");
        item2.put("key2", "y");

        IDataMap item3 = new IDataMap();
        item3.put("key1", "z");
        item3.put("key2", "x");

        IDataMap item4 = new IDataMap();
        item4.put("key1", "a");
        item4.put("key2", "z");

        IDataMap item5 = new IDataMap();
        item5.put("key1", "a");
        item5.put("key2", "y");

        IDataMap item6 = new IDataMap();
        item6.put("key1", "a");
        item6.put("key2", "x");

        array[0] = item1;
        array[1] = item2;
        array[2] = item3;
        array[3] = item4;
        array[4] = item5;
        array[5] = item6;

        IDataComparisonCriterion c1 = new IDataComparisonCriterion("key1", "string", false);
        IDataComparisonCriterion c2 = new IDataComparisonCriterion("key2", "string", false);

        IData[] result = IDataHelper.sort(array, c1, c2);

        assertEquals(6, result.length);

        IDataMap first = IDataMap.of(result[0]);
        assertEquals("a", first.get("key1"));
        assertEquals("x", first.get("key2"));

        IDataMap second = IDataMap.of(result[1]);
        assertEquals("a", second.get("key1"));
        assertEquals("y", second.get("key2"));

        IDataMap third = IDataMap.of(result[2]);
        assertEquals("a", third.get("key1"));
        assertEquals("z", third.get("key2"));

        IDataMap fourth = IDataMap.of(result[3]);
        assertEquals("z", fourth.get("key1"));
        assertEquals("x", fourth.get("key2"));

        IDataMap fifth = IDataMap.of(result[4]);
        assertEquals("z", fifth.get("key1"));
        assertEquals("y", fifth.get("key2"));

        IDataMap sixth = IDataMap.of(result[5]);
        assertEquals("z", sixth.get("key1"));
        assertEquals("z", sixth.get("key2"));
    }

    @Test
    public void testSortWithMultipleStringKeysDescending() throws Exception {
        IData[] array = new IData[6];

        IDataMap item1 = new IDataMap();
        item1.put("key1", "z");
        item1.put("key2", "z");

        IDataMap item2 = new IDataMap();
        item2.put("key1", "z");
        item2.put("key2", "y");

        IDataMap item3 = new IDataMap();
        item3.put("key1", "z");
        item3.put("key2", "x");

        IDataMap item4 = new IDataMap();
        item4.put("key1", "a");
        item4.put("key2", "z");

        IDataMap item5 = new IDataMap();
        item5.put("key1", "a");
        item5.put("key2", "y");

        IDataMap item6 = new IDataMap();
        item6.put("key1", "a");
        item6.put("key2", "x");

        array[0] = item1;
        array[1] = item2;
        array[2] = item3;
        array[3] = item4;
        array[4] = item5;
        array[5] = item6;

        IDataComparisonCriterion c1 = new IDataComparisonCriterion("key1", "string", true);
        IDataComparisonCriterion c2 = new IDataComparisonCriterion("key2", "string", true);

        IData[] result = IDataHelper.sort(array, c1, c2);

        assertEquals(6, result.length);

        IDataMap first = IDataMap.of(result[0]);
        assertEquals("z", first.get("key1"));
        assertEquals("z", first.get("key2"));

        IDataMap second = IDataMap.of(result[1]);
        assertEquals("z", second.get("key1"));
        assertEquals("y", second.get("key2"));

        IDataMap third = IDataMap.of(result[2]);
        assertEquals("z", third.get("key1"));
        assertEquals("x", third.get("key2"));

        IDataMap fourth = IDataMap.of(result[3]);
        assertEquals("a", fourth.get("key1"));
        assertEquals("z", fourth.get("key2"));

        IDataMap fifth = IDataMap.of(result[4]);
        assertEquals("a", fifth.get("key1"));
        assertEquals("y", fifth.get("key2"));

        IDataMap sixth = IDataMap.of(result[5]);
        assertEquals("a", sixth.get("key1"));
        assertEquals("x", sixth.get("key2"));
    }

    @Test
    public void testSortWithMultipleStringKeysAscendingDescending() throws Exception {
        IData[] array = new IData[6];

        IDataMap item1 = new IDataMap();
        item1.put("key1", "z");
        item1.put("key2", "z");

        IDataMap item2 = new IDataMap();
        item2.put("key1", "z");
        item2.put("key2", "y");

        IDataMap item3 = new IDataMap();
        item3.put("key1", "z");
        item3.put("key2", "x");

        IDataMap item4 = new IDataMap();
        item4.put("key1", "a");
        item4.put("key2", "z");

        IDataMap item5 = new IDataMap();
        item5.put("key1", "a");
        item5.put("key2", "y");

        IDataMap item6 = new IDataMap();
        item6.put("key1", "a");
        item6.put("key2", "x");

        array[0] = item1;
        array[1] = item2;
        array[2] = item3;
        array[3] = item4;
        array[4] = item5;
        array[5] = item6;

        IDataComparisonCriterion c1 = new IDataComparisonCriterion("key1", "string", false);
        IDataComparisonCriterion c2 = new IDataComparisonCriterion("key2", "string", true);

        IData[] result = IDataHelper.sort(array, c1, c2);

        assertEquals(6, result.length);

        IDataMap first = IDataMap.of(result[0]);
        assertEquals("a", first.get("key1"));
        assertEquals("z", first.get("key2"));

        IDataMap second = IDataMap.of(result[1]);
        assertEquals("a", second.get("key1"));
        assertEquals("y", second.get("key2"));

        IDataMap third = IDataMap.of(result[2]);
        assertEquals("a", third.get("key1"));
        assertEquals("x", third.get("key2"));

        IDataMap fourth = IDataMap.of(result[3]);
        assertEquals("z", fourth.get("key1"));
        assertEquals("z", fourth.get("key2"));

        IDataMap fifth = IDataMap.of(result[4]);
        assertEquals("z", fifth.get("key1"));
        assertEquals("y", fifth.get("key2"));

        IDataMap sixth = IDataMap.of(result[5]);
        assertEquals("z", sixth.get("key1"));
        assertEquals("x", sixth.get("key2"));
    }

    @Test
    public void testSortWithMultipleStringKeysDescendingAscending() throws Exception {
        IData[] array = new IData[6];

        IDataMap item1 = new IDataMap();
        item1.put("key1", "z");
        item1.put("key2", "z");

        IDataMap item2 = new IDataMap();
        item2.put("key1", "z");
        item2.put("key2", "y");

        IDataMap item3 = new IDataMap();
        item3.put("key1", "z");
        item3.put("key2", "x");

        IDataMap item4 = new IDataMap();
        item4.put("key1", "a");
        item4.put("key2", "z");

        IDataMap item5 = new IDataMap();
        item5.put("key1", "a");
        item5.put("key2", "y");

        IDataMap item6 = new IDataMap();
        item6.put("key1", "a");
        item6.put("key2", "x");

        array[0] = item1;
        array[1] = item2;
        array[2] = item3;
        array[3] = item4;
        array[4] = item5;
        array[5] = item6;

        IDataComparisonCriterion c1 = new IDataComparisonCriterion("key1", "string", true);
        IDataComparisonCriterion c2 = new IDataComparisonCriterion("key2", "string", false);

        IData[] result = IDataHelper.sort(array, c1, c2);

        assertEquals(6, result.length);

        IDataMap first = IDataMap.of(result[0]);
        assertEquals("z", first.get("key1"));
        assertEquals("x", first.get("key2"));

        IDataMap second = IDataMap.of(result[1]);
        assertEquals("z", second.get("key1"));
        assertEquals("y", second.get("key2"));

        IDataMap third = IDataMap.of(result[2]);
        assertEquals("z", third.get("key1"));
        assertEquals("z", third.get("key2"));

        IDataMap fourth = IDataMap.of(result[3]);
        assertEquals("a", fourth.get("key1"));
        assertEquals("x", fourth.get("key2"));

        IDataMap fifth = IDataMap.of(result[4]);
        assertEquals("a", fifth.get("key1"));
        assertEquals("y", fifth.get("key2"));

        IDataMap sixth = IDataMap.of(result[5]);
        assertEquals("a", sixth.get("key1"));
        assertEquals("z", sixth.get("key2"));
    }

    @Test
    public void testSortWithIDataArrayCriteria() throws Exception {
        IData[] array = new IData[6];

        IDataMap item1 = new IDataMap();
        item1.put("key1", "z");
        item1.put("key2", "z");

        IDataMap item2 = new IDataMap();
        item2.put("key1", "z");
        item2.put("key2", "y");

        IDataMap item3 = new IDataMap();
        item3.put("key1", "z");
        item3.put("key2", "x");

        IDataMap item4 = new IDataMap();
        item4.put("key1", "a");
        item4.put("key2", "z");

        IDataMap item5 = new IDataMap();
        item5.put("key1", "a");
        item5.put("key2", "y");

        IDataMap item6 = new IDataMap();
        item6.put("key1", "a");
        item6.put("key2", "x");

        array[0] = item1;
        array[1] = item2;
        array[2] = item3;
        array[3] = item4;
        array[4] = item5;
        array[5] = item6;

        IData[] criteria = new IData[2];

        IDataMap criteria1 = new IDataMap();
        criteria1.put("key", "key1");
        criteria1.put("type", "string");
        criteria1.put("descending?", "true");

        IDataMap criteria2 = new IDataMap();
        criteria2.put("key", "key2");
        criteria2.put("type", "string");
        criteria2.put("descending?", "false");

        criteria[0] = criteria1;
        criteria[1] = criteria2;

        IData[] result = IDataHelper.sort(array, criteria);

        assertEquals(6, result.length);

        IDataMap first = IDataMap.of(result[0]);
        assertEquals("z", first.get("key1"));
        assertEquals("x", first.get("key2"));

        IDataMap second = IDataMap.of(result[1]);
        assertEquals("z", second.get("key1"));
        assertEquals("y", second.get("key2"));

        IDataMap third = IDataMap.of(result[2]);
        assertEquals("z", third.get("key1"));
        assertEquals("z", third.get("key2"));

        IDataMap fourth = IDataMap.of(result[3]);
        assertEquals("a", fourth.get("key1"));
        assertEquals("x", fourth.get("key2"));

        IDataMap fifth = IDataMap.of(result[4]);
        assertEquals("a", fifth.get("key1"));
        assertEquals("y", fifth.get("key2"));

        IDataMap sixth = IDataMap.of(result[5]);
        assertEquals("a", sixth.get("key1"));
        assertEquals("z", sixth.get("key2"));
    }

    @Test
    public void testGetAsArrayWithSingleOccurrence() throws Exception {
        Object[] expected = new String[] { "1" };

        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "a", "1");
        IDataUtil.put(cursor, "b", "4");
        cursor.destroy();

        Object[] actual = IDataHelper.getAsArray(document, "a");

        assertArrayEquals(expected, actual);
        assertTrue(actual instanceof String[]);
    }

    @Test
    public void testGetAsArrayWithArrayIndex() throws Exception {
        Object[] expected = new String[] { "2" };
        Object[] array = new String[] { "1", "2", "3" };

        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "a", array);
        IDataUtil.put(cursor, "b", "4");
        cursor.destroy();

        Object[] actual = IDataHelper.getAsArray(document, "a[1]");

        assertArrayEquals(expected, actual);
        assertTrue(actual instanceof String[]);
    }

    @Test
    public void testGetAsArrayWithMultipleOccurrence() throws Exception {
        Object[] expected = new String[] { "1", "2", "3" };

        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "1");
        cursor.insertAfter("a", "2");
        cursor.insertAfter("a", "3");
        cursor.insertAfter("b", "4");
        cursor.destroy();

        Object[] actual = IDataHelper.getAsArray(document, "a");

        assertArrayEquals(expected, actual);
        assertTrue(actual instanceof String[]);
    }

    @Test
    public void testGetAsArrayWithNthKeyAndMultipleOccurrence() throws Exception {
        Object[] expected = new String[] { "2" };

        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("a", "1");
        cursor.insertAfter("a", "2");
        cursor.insertAfter("a", "3");
        cursor.insertAfter("b", "4");
        cursor.destroy();

        Object[] actual = IDataHelper.getAsArray(document, "a(1)");

        assertArrayEquals(expected, actual);
        assertTrue(actual instanceof String[]);
    }

    @Test
    public void testGetAsArrayWithQualifiedKeyAndSingleOccurrence() throws Exception {
        Object[] expected = new String[] { "1" };

        IData child = IDataFactory.create();
        IDataCursor cursor = child.getCursor();
        cursor.insertAfter("b", "1");
        cursor.insertAfter("c", "2");
        cursor.insertAfter("c", "3");
        cursor.insertAfter("c", "4");
        cursor.destroy();

        IDataMap parent = new IDataMap();
        parent.put("a", child);

        Object[] actual = IDataHelper.getAsArray(parent, "a/b");

        assertArrayEquals(expected, actual);
        assertTrue(actual instanceof String[]);
    }

    @Test
    public void testGetAsArrayWithQualifiedKeyAndMultipleOccurrence() throws Exception {
        Object[] expected = new String[] { "2", "3", "4" };

        IData child = IDataFactory.create();
        IDataCursor cursor = child.getCursor();
        cursor.insertAfter("b", "1");
        cursor.insertAfter("c", "2");
        cursor.insertAfter("c", "3");
        cursor.insertAfter("c", "4");
        cursor.destroy();

        IDataMap parent = new IDataMap();
        parent.put("a", child);

        Object[] actual = IDataHelper.getAsArray(parent, "a/c");

        assertArrayEquals(expected, actual);
        assertTrue(actual instanceof String[]);
    }

    @Test
    public void testGetAsArrayWithQualifiedKeyAndMultipleArrays() throws Exception {
        Object[] expected = new String[] { "1", "2", "3", "4", "5", "6", "7" };

        Object[] array1 = new String[] { "1", "2", "3" };
        Object[] array2 = new String[] { "4", "5", "6" };

        IData child = IDataFactory.create();
        IDataCursor cursor = child.getCursor();
        cursor.insertAfter("b", "1");
        cursor.insertAfter("c", array1);
        cursor.insertAfter("c", array2);
        cursor.insertAfter("c", "7");

        cursor.destroy();

        IDataMap parent = new IDataMap();
        parent.put("a", child);

        Object[] actual = IDataHelper.getAsArray(parent, "a/c");

        assertArrayEquals(expected, actual);
        assertTrue(actual instanceof String[]);
    }

    @Test
    public void testArrayify() throws Exception {
        Object[] expected = new String[] { "2", "3", "4" };

        IData child = IDataFactory.create();
        IDataCursor cursor = child.getCursor();
        cursor.insertAfter("b", "1");
        cursor.insertAfter("c", "2");
        cursor.insertAfter("c", "3");
        cursor.insertAfter("c", "4");
        cursor.destroy();

        IDataMap parent = new IDataMap();
        parent.put("a", child);

        IDataHelper.arrayify(parent, "a/c");

        assertArrayEquals(expected, (Object[])IDataHelper.get(parent, "a/c"));
    }

    @Test
    public void testUniqueWithOneKey() throws Exception {
        IData[] array = new IData[6];

        IDataMap item1 = new IDataMap();
        item1.put("key1", "z");
        item1.put("key2", "z");

        IDataMap item2 = new IDataMap();
        item2.put("key1", "z");
        item2.put("key2", "y");

        IDataMap item3 = new IDataMap();
        item3.put("key1", "z");
        item3.put("key2", "x");

        IDataMap item4 = new IDataMap();
        item4.put("key1", "a");
        item4.put("key2", "z");

        IDataMap item5 = new IDataMap();
        item5.put("key1", "a");
        item5.put("key2", "y");

        IDataMap item6 = new IDataMap();
        item6.put("key1", "a");
        item6.put("key2", "x");

        array[0] = item1;
        array[1] = item2;
        array[2] = item3;
        array[3] = item4;
        array[4] = item5;
        array[5] = item6;

        IData[] result = IDataHelper.unique(array, "key1");

        assertEquals(2, result.length);

        IDataMap first = IDataMap.of(result[0]);
        assertEquals("a", first.get("key1"));
        assertEquals("z", first.get("key2"));

        IDataMap second = IDataMap.of(result[1]);
        assertEquals("z", second.get("key1"));
        assertEquals("z", second.get("key2"));
    }

    @Test
    public void testUniqueWithTwoKeys() throws Exception {
        IData[] array = new IData[6];

        IDataMap item1 = new IDataMap();
        item1.put("key1", "z");
        item1.put("key2", "z");
        item1.put("key3", "1");

        IDataMap item2 = new IDataMap();
        item2.put("key1", "z");
        item2.put("key2", "y");
        item2.put("key3", "1");

        IDataMap item3 = new IDataMap();
        item3.put("key1", "z");
        item3.put("key2", "x");
        item3.put("key3", "1");

        IDataMap item4 = new IDataMap();
        item4.put("key1", "z");
        item4.put("key2", "z");
        item4.put("key3", "2");

        IDataMap item5 = new IDataMap();
        item5.put("key1", "z");
        item5.put("key2", "y");
        item5.put("key3", "2");

        IDataMap item6 = new IDataMap();
        item6.put("key1", "a");
        item6.put("key2", "x");
        item6.put("key3", "1");


        array[0] = item1;
        array[1] = item2;
        array[2] = item3;
        array[3] = item4;
        array[4] = item5;
        array[5] = item6;

        IData[] result = IDataHelper.unique(array, "key1", "key2");

        assertEquals(4, result.length);

        IDataMap first = IDataMap.of(result[0]);
        assertEquals("a", first.get("key1"));
        assertEquals("x", first.get("key2"));
        assertEquals("1", first.get("key3"));

        IDataMap second = IDataMap.of(result[1]);
        assertEquals("z", second.get("key1"));
        assertEquals("x", second.get("key2"));
        assertEquals("1", second.get("key3"));

        IDataMap third = IDataMap.of(result[2]);
        assertEquals("z", third.get("key1"));
        assertEquals("y", third.get("key2"));
        assertEquals("1", third.get("key3"));

        IDataMap fourth = IDataMap.of(result[3]);
        assertEquals("z", fourth.get("key1"));
        assertEquals("z", fourth.get("key2"));
        assertEquals("1", fourth.get("key3"));
    }

    @Test
    public void testUniqueWithNoKeys() throws Exception {
        IData[] array = new IData[6];

        IDataMap item1 = new IDataMap();
        item1.put("key1", "z");
        item1.put("key2", "z");
        item1.put("key3", "z");

        IDataMap item2 = new IDataMap();
        item2.put("key1", "z");
        item2.put("key2", "y");
        item2.put("key3", new Object());

        IDataMap item3 = new IDataMap();
        item3.put("key1", "z");
        item3.put("key2", "x");
        item3.put("key3", new Object());

        IDataMap item4 = new IDataMap();
        item4.put("key1", "z");
        item4.put("key2", "z");
        item4.put("key3", "z");

        IDataMap item5 = new IDataMap();
        item5.put("key1", "z");
        item5.put("key2", "y");
        item5.put("key3", new Object());

        IDataMap item6 = new IDataMap();
        item6.put("key1", "a");
        item6.put("key2", "x");
        item6.put("key3", new Object());

        array[0] = item1;
        array[1] = item2;
        array[2] = item3;
        array[3] = item4;
        array[4] = item5;
        array[5] = item6;

        IData[] result = IDataHelper.unique(array);

        assertEquals(5, result.length);

        IDataMap first = IDataMap.of(result[0]);
        assertEquals("a", first.get("key1"));
        assertEquals("x", first.get("key2"));

        IDataMap second = IDataMap.of(result[1]);
        assertEquals("z", second.get("key1"));
        assertEquals("x", second.get("key2"));

        IDataMap third = IDataMap.of(result[2]);
        assertEquals("z", third.get("key1"));
        assertEquals("y", third.get("key2"));

        IDataMap fourth = IDataMap.of(result[3]);
        assertEquals("z", fourth.get("key1"));
        assertEquals("y", fourth.get("key2"));

        IDataMap fifth = IDataMap.of(result[4]);
        assertEquals("z", fifth.get("key1"));
        assertEquals("z", fifth.get("key2"));
        assertEquals("z", fifth.get("key3"));
    }

    @Test
    public void testGetLeavesForIData() throws Exception {
        String[] expected = new String[] { "1", "2", "3", "4", "6" };

        IDataMap parent = new IDataMap();
        parent.put("a", "1");

        IDataMap child = new IDataMap();
        child.put("c", "2");
        child.put("d", new String[] { "3", "4" });
        child.put("e", 5);
        child.put("f", "6");

        parent.put("b", child);

        String[] actual = IDataHelper.getLeaves(parent, String.class);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetLeavesNoRecurseWithArrays() throws Exception {
        String[] expected = new String[] { "1", "2", "3" };

        IDataMap parent = new IDataMap();
        parent.put("a", "1");
        parent.put("b", new String[] { "2", "3" });

        IDataMap child = new IDataMap();
        child.put("d", "4");
        child.put("e", 5);
        child.put("f", "6");

        parent.put("c", child);

        String[] actual = IDataHelper.getLeaves(parent, String.class, false);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGetLeavesForIDataArray() throws Exception {
        String[] expected = new String[] { "1", "2", "3", "4", "6", "7", "8", "9" };

        IData[] array = new IData[2];

        IDataMap parent = new IDataMap();
        parent.put("a", "1");
        IDataMap child = new IDataMap();
        child.put("c", "2");
        child.put("d", new String[] { "3", "4" });
        child.put("e", 5);
        child.put("f", "6");
        parent.put("b", child);

        array[0] = parent;

        parent = new IDataMap();
        child = new IDataMap();
        child.put("h", "7");
        child.put("i", new String[] { "8", "9" });
        child.put("j", 10);
        parent.put("g", child);

        array[1] = parent;

        String[] actual = IDataHelper.getLeaves(array, String.class);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testJoin() throws Exception {
        IDataMap map = new IDataMap();
        map.put("a", "1");
        map.put("b", "2");

        IDataMap child = new IDataMap();
        child.put("d", new String[]{"3", "4"});
        child.put("e", new String[][]{{"5", "6"}, {"7", "8"}});

        map.put("c", child);
        map.put("f", new String[]{"9", "10", "11", "12"});

        IDataMap[] array = new IDataMap[2];
        array[0] = new IDataMap();
        array[0].put("h", "13");
        array[0].put("i", "14");
        array[1] = new IDataMap();
        array[1].put("j", "15");

        map.put("g", array);

        assertEquals("a: 1, b: 2, c: {d: [3, 4], e: [[5, 6], [7, 8]]}, f: [9, 10, 11, 12], g: [{h: 13, i: 14}, {j: 15}]", IDataHelper.join(map));
    }

    @Test
    public void testJoinWithCustomSeparators() throws Exception {
        IDataMap map = new IDataMap();
        map.put("z", null);
        map.put("a", "1");
        map.put("b", "2");

        IDataMap child = new IDataMap();
        child.put("d", new String[]{null, "3", "4"});
        child.put("e", new String[][]{{"5", "6"}, {"7", "8"}, null, {null, "9"}});

        map.put("c", child);
        map.put("f", new String[]{"9", "10", null, "12", null});

        IDataMap[] array = new IDataMap[2];
        array[0] = new IDataMap();
        array[0].put("h", "13");
        array[0].put("i", "14");
        array[1] = new IDataMap();
        array[1].put("j", "15");
        array[1].put("k", null);

        map.put("g", array);
        map.put("l", new String[0]);

        assertEquals("z = null; a = 1; b = 2; c = {d = [null, 3, 4]; e = [[5, 6], [7, 8], null, [null, 9]]}; f = [9, 10, null, 12, null]; g = [{h = 13; i = 14}, {j = 15; k = null}]; l = []", IDataHelper.join(map, "; ", ", ", " = "));
        assertEquals("a = 1; b = 2; c = {d = [3, 4]; e = [[5, 6], [7, 8], [9]]}; f = [9, 10, 12]; g = [{h = 13; i = 14}, {j = 15}]; l = []", IDataHelper.join(map, "; ", ", ", " = ", Sanitization.REMOVE_NULLS));
    }

    @Test
    public void testGetFromCursor() throws Exception {
        IDataCursor cursor = document.getCursor();
        IDataHelper.put(cursor, "a", "1");
        IDataHelper.put(cursor, "b", 2);
        IDataHelper.put(cursor, "c", 3.14f);
        IDataHelper.put(cursor, "d", true);
        IDataHelper.put(cursor, "e", 4L);
        IDataHelper.put(cursor, "f", "true");
        IDataHelper.put(cursor, "g", 5, false, false, false);
        IDataHelper.put(cursor, "g", true, false, false, false);
        cursor.destroy();

        cursor = document.getCursor();
        assertEquals("1", IDataHelper.get(cursor, "a", String.class));
        assertEquals(Integer.valueOf(2), IDataHelper.get(cursor, "b", Integer.class));
        assertEquals(Long.valueOf(2), IDataHelper.get(cursor, "b", Long.class));
        assertEquals(Float.valueOf(3.14f), IDataHelper.get(cursor, "c", Float.class));
        assertEquals(Double.valueOf(3.14f), IDataHelper.get(cursor, "c", Double.class));
        assertEquals(true, IDataHelper.get(cursor, "d", Boolean.class));
        assertEquals(false, IDataHelper.getOrDefault(cursor, "e", Boolean.class, false));
        assertEquals(true, IDataHelper.getOrDefault(cursor, "e", Boolean.class, true));
        assertEquals(true, IDataHelper.get(cursor, "f", Boolean.class));
        assertEquals(false, IDataHelper.getOrDefault(cursor, "missing", Boolean.class, false));
        assertEquals(true, IDataHelper.getOrDefault(cursor, "missing", Boolean.class, true));
        assertEquals(true, IDataHelper.get(cursor, "g", Boolean.class, true));

        cursor.destroy();
    }

    @Test
    public void testGroupNoSort() throws Exception {
        IDataJSONParser parser = new IDataJSONParser();
        IData[] array = new IData[6];
        array[0] = parser.parse("{ \"a\": \"rec002\", \"b\": \"b0021\" }");
        array[1] = parser.parse("{ \"a\": \"rec003\", \"b\": \"b0031\" }");
        array[2] = parser.parse("{ \"a\": \"rec001\", \"b\": \"b0012\" }");
        array[3] = parser.parse("{ \"a\": \"rec003\", \"b\": \"b0032\" }");
        array[4] = parser.parse("{ \"a\": \"rec001\", \"b\": \"b0011\" }");
        array[5] = parser.parse("{ \"a\": \"rec002\", \"b\": \"b0022\" }");

        IDataComparisonCriterion[] criteria = new IDataComparisonCriterion[1];
        criteria[0] = new IDataComparisonCriterion("a");

        Map<IDataHelper.CompoundKey, List<IData>> groups = IDataHelper.group(array, criteria, IDataHelper.IDataArrayGroupSortType.NONE);

        assertEquals(3, groups.size());
    }

    @Test
    public void testExistsWithIData() throws Exception {
        String[] keys = { "a", "b", "c" };
        for (String key : keys) {
            boolean exists = IDataHelper.exists(document, key);
            assertTrue("key `" + key + "` exists in document", exists);
        }
    }

    @Test
    public void testNotExistsWithIData() throws Exception {
        String[] keys = { "d", "e", "f" };
        for (String key : keys) {
            boolean exists = IDataHelper.exists(document, key);
            assertFalse("key `" + key + "` does not exist in document", exists);
        }
    }

    @Test
    public void testExistsWithNthKey() throws Exception {
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("c", "4");
        cursor.insertAfter("b", "5");
        cursor.insertAfter("a", "6");
        cursor.insertAfter("b", "7");
        cursor.insertAfter("a", "8");
        cursor.insertAfter("c", "9");
        cursor.destroy();

        String[] keys = { "a(0)", "a(1)", "a(2)", "b(0)", "b(1)", "b(2)", "c(0)", "c(1)", "c(2)" };
        for (String key : keys) {
            boolean exists = IDataHelper.exists(document, key);
            assertTrue("key `" + key + "` exists in document", exists);
        }
    }

    @Test
    public void testNotExistsWithNthKey() throws Exception {
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("c", "4");
        cursor.insertAfter("b", "5");
        cursor.insertAfter("a", "6");
        cursor.insertAfter("b", "7");
        cursor.insertAfter("a", "8");
        cursor.insertAfter("c", "9");
        cursor.destroy();

        String[] keys = { "a(3)", "a(4)", "b(3)", "b(4)", "c(3)", "c(4)" };
        for (String key : keys) {
            boolean exists = IDataHelper.exists(document, key);
            assertFalse("key `" + key + "` does not exist in document", exists);
        }
    }

    @Test
    public void testExistsWithFullyQualifiedKey() throws Exception {
        IData grandchild = IDataFactory.create();
        IDataCursor grandchildCursor = grandchild.getCursor();
        grandchildCursor.insertAfter("h", "7");
        grandchildCursor.destroy();

        IData child = IDataFactory.create();
        IDataCursor childCursor = child.getCursor();
        childCursor.insertAfter("e", "4");
        childCursor.insertAfter("f", "5");
        childCursor.insertAfter("g", grandchild);
        childCursor.destroy();

        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("d", child);
        cursor.destroy();

        String[] keys = { "d/e", "d/f", "d/g", "d/g/h" };
        for (String key : keys) {
            boolean exists = IDataHelper.exists(document, key);
            assertTrue("key `" + key + "` exists in document", exists);
        }
    }

    @Test
    public void testNotExistsWithFullyQualifiedKey() throws Exception {
        IData grandchild = IDataFactory.create();
        IDataCursor grandchildCursor = grandchild.getCursor();
        grandchildCursor.insertAfter("h", "7");
        grandchildCursor.destroy();

        IData child = IDataFactory.create();
        IDataCursor childCursor = child.getCursor();
        childCursor.insertAfter("e", "4");
        childCursor.insertAfter("f", "5");
        childCursor.insertAfter("g", grandchild);
        childCursor.destroy();

        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("d", child);
        cursor.destroy();

        String[] keys = { "d/h", "d/g/h/i" };
        for (String key : keys) {
            boolean exists = IDataHelper.exists(document, key);
            assertFalse("key `" + key + "` does not exist in document", exists);
        }
    }

    @Test
    public void testExistsWithArray() throws Exception {
        String[] value = { "1", "2", "3"};

        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("array", value);
        cursor.destroy();

        String key = "array[1]";
        boolean exists = IDataHelper.exists(document, key);
        assertTrue("key `" + key + "` exists in document", exists);
    }

    @Test
    public void testNotExistsWithArray() throws Exception {
        String[] value = { "1", "2", "3"};

        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("array", value);
        cursor.destroy();

        String key = "array[3]";
        boolean exists = IDataHelper.exists(document, key);
        assertFalse("key `" + key + "` does not exist in document", exists);
    }

    @Test
    public void testExistsWithSystemReference() throws Exception {
        IData document = SystemHelper.getReference();
        String[] keys = { "uuid", "datetime/local/yyyy", "datetime/utc/mmm" };
        for (String key : keys) {
            boolean exists = IDataHelper.exists(document, key);
            assertTrue("key `" + key + "` exists in document", exists);
        }
    }
}
