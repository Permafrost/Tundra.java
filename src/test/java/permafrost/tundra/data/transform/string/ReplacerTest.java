package permafrost.tundra.data.transform.string;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.data.transform.TransformerMode;
import java.util.ArrayList;
import java.util.List;

public class ReplacerTest extends TestCase {
    IData document = IDataFactory.create();
    int length = 10;

    @Before
    public void setUp() throws Exception {
        List<IData> recordWithNoID = new ArrayList<IData>();
        for (int i = 0; i < length; i++) {
            IDataMap item = new IDataMap();
            item.put("Test_Key_1", "Test_Value_1");
            item.put("Test_Key_2", "Test_Value_2");
            item.put("Test_Key_3", "Test_Value_3");
            recordWithNoID.add(item);
        }
        IDataCursor cursor = document.getCursor();
        cursor.insertAfter("recordWithNoID", recordWithNoID.toArray(new IData[0]));
        cursor.destroy();
    }

    @Test
    public void testTransformKeys() {
        Replacer replacer = new Replacer(TransformerMode.KEYS, "_", true, " ", true, false, true);

        IData output = replacer.transform(document);
        IDataCursor cursor = output.getCursor();
        IData[] recordWithNoID = IDataHelper.get(cursor, "recordWithNoID", IData[].class);
        cursor.destroy();

        assertEquals(length, recordWithNoID.length);
        for (int i = 0; i < recordWithNoID.length; i++) {
            IData item = recordWithNoID[i];
            IDataCursor itemCursor = item.getCursor();
            assertEquals(3, IDataHelper.size(item));
            assertEquals("Test_Value_1", IDataHelper.get(itemCursor, "Test Key 1", String.class));
            assertEquals("Test_Value_2", IDataHelper.get(itemCursor, "Test Key 2", String.class));
            assertEquals("Test_Value_3", IDataHelper.get(itemCursor, "Test Key 3", String.class));
            itemCursor.destroy();
        }
    }

    @Test
    public void testTransformValues() {
        Replacer replacer = new Replacer(TransformerMode.VALUES, "_", true, " ", true, false, true);

        IData output = replacer.transform(document);
        IDataCursor cursor = output.getCursor();
        IData[] recordWithNoID = IDataHelper.get(cursor, "recordWithNoID", IData[].class);
        cursor.destroy();

        assertEquals(length, recordWithNoID.length);
        for (int i = 0; i < recordWithNoID.length; i++) {
            IData item = recordWithNoID[i];
            IDataCursor itemCursor = item.getCursor();
            assertEquals(3, IDataHelper.size(item));
            assertEquals("Test Value 1", IDataHelper.get(itemCursor, "Test_Key_1", String.class));
            assertEquals("Test Value 2", IDataHelper.get(itemCursor, "Test_Key_2", String.class));
            assertEquals("Test Value 3", IDataHelper.get(itemCursor, "Test_Key_3", String.class));
            itemCursor.destroy();
        }
    }

    @Test
    public void testTransformKeysAndValues() {
        Replacer replacer = new Replacer(TransformerMode.KEYS_AND_VALUES, "_", true, " ", true, false, true);

        IData output = replacer.transform(document);
        IDataCursor cursor = output.getCursor();
        IData[] recordWithNoID = IDataHelper.get(cursor, "recordWithNoID", IData[].class);
        cursor.destroy();

        assertEquals(length, recordWithNoID.length);
        for (int i = 0; i < recordWithNoID.length; i++) {
            IData item = recordWithNoID[i];
            IDataCursor itemCursor = item.getCursor();
            assertEquals(3, IDataHelper.size(item));
            assertEquals("Test Value 1", IDataHelper.get(itemCursor, "Test Key 1", String.class));
            assertEquals("Test Value 2", IDataHelper.get(itemCursor, "Test Key 2", String.class));
            assertEquals("Test Value 3", IDataHelper.get(itemCursor, "Test Key 3", String.class));
            itemCursor.destroy();
        }
    }
}