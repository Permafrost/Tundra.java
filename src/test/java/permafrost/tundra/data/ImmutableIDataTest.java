package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import static org.junit.Assert.*;
import org.junit.Test;

public class ImmutableIDataTest {
    @Test
    public void testPutIgnored() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "a", "1");
        cursor.destroy();

        IData immutableDocument = new ImmutableIData(document);
        cursor = immutableDocument.getCursor();

        IDataUtil.put(cursor, "a", "2");
        assertEquals("1", IDataUtil.getString(cursor, "a"));
        cursor.destroy();
    }
}