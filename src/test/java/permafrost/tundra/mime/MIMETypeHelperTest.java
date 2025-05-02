/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lachlan Dowding
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

package permafrost.tundra.mime;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.BaseException;

public class MIMETypeHelperTest {

    @Test
    public void testParse() throws Exception {
        IData document = MIMETypeHelper.parse("text/plain;charset=UTF-8;foo=bar");
        IDataCursor cursor = document.getCursor();
        assertEquals("text", IDataUtil.getString(cursor, "type"));
        assertEquals("plain", IDataUtil.getString(cursor, "subtype"));
        IData parameters = IDataUtil.getIData(cursor, "parameters");
        cursor.destroy();

        cursor = parameters.getCursor();
        assertEquals("UTF-8", IDataUtil.getString(cursor, "charset"));
        assertEquals("bar", IDataUtil.getString(cursor, "foo"));
        cursor.destroy();
    }

    @Test
    public void testEmit() throws Exception {
        IDataMap document = new IDataMap();

        document.put("type", "text");
        document.put("subtype", "plain");

        IDataMap parameters = new IDataMap();
        parameters.put("foo", "bar");
        parameters.put("charset", "UTF-8");

        document.put("parameters", parameters);

        assertEquals("text/plain; charset=UTF-8; foo=bar", MIMETypeHelper.emit(document));
    }

    @Test
    public void testNormalize() throws Exception {
        assertEquals("text/plain; charset=UTF-8; foo=bar", MIMETypeHelper.normalize("text/plain;foo=bar;charset=UTF-8;"));
    }

    @Test
    public void testEqual() throws Exception {
        assertEquals(true, MIMETypeHelper.equal("text/plain", "text/plain;charset=UTF-8"));
        assertEquals(false, MIMETypeHelper.equal("text/csv", "text/plain;charset=UTF-8"));
    }

    @Test(expected = BaseException.class)
    public void testValidateWithRaise() throws Exception {
        MIMETypeHelper.validate("foo", true);
    }

    @Test
    public void testValidate() throws Exception {
        assertEquals(false, MIMETypeHelper.validate("foo"));
        assertEquals(false, MIMETypeHelper.validate("foo", false));
        assertEquals(true, MIMETypeHelper.validate("foo/bar"));
        assertEquals(true, MIMETypeHelper.validate("foo/bar", true));
        assertEquals(true, MIMETypeHelper.validate("foo/bar", false));
    }

    @Test
    public void testParsingMalformedMIMETypes() throws Exception {
        assertEquals("FoO", MIMETypeHelper.of("FoO").toString());
        assertEquals("FoO", MIMETypeHelper.of("FoO/").toString());
        assertEquals("FoO/BaR", MIMETypeHelper.of("FoO/BaR").toString());
        assertEquals("FoO; param=value", MIMETypeHelper.of("FoO;param=value").toString());
        assertEquals("FoO/BaR; param=value", MIMETypeHelper.of("FoO/BaR;param=value").toString());
        assertEquals("/BaR", MIMETypeHelper.of("/BaR").toString());
        assertEquals("/BaR; param=value", MIMETypeHelper.of("/BaR;param=value").toString());
        assertEquals("; param=value", MIMETypeHelper.of(";param=value").toString());
    }
}