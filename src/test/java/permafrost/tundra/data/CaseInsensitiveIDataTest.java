/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import static org.junit.Assert.*;
import org.junit.Test;

public class CaseInsensitiveIDataTest {
    @Test
    public void test() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "AAA", "1");
        IDataUtil.put(cursor, "bbB", "2");
        cursor.destroy();

        IData caseInsensitiveDocument = new CaseInsensitiveIData(document);
        cursor = caseInsensitiveDocument.getCursor();
        assertEquals("1", IDataUtil.getString(cursor, "aaa"));
        assertEquals("1", IDataUtil.getString(cursor, "aAa"));
        assertEquals("2", IDataUtil.getString(cursor, "BBB"));
        assertEquals("2", IDataUtil.getString(cursor, "bbb"));
        assertEquals("2", IDataUtil.getString(cursor, "Bbb"));
        cursor.destroy();
    }

}