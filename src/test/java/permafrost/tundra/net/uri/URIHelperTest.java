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

package permafrost.tundra.net.uri;

import static org.junit.Assert.assertEquals;
import com.wm.data.IData;
import org.junit.Test;

public class URIHelperTest {
    @Test
    public void testDecode() throws Exception {
        assertEquals("a test", URIHelper.decode("a%20test"));
    }

    @Test
    public void testEncode() throws Exception {
        assertEquals("a%20test", URIHelper.encode("a test"));
    }

    @Test
    public void testParseFilename() throws Exception {
        String string = "a.txt";
        String result = URIHelper.emit(URIHelper.parse(string));
        assertEquals(string, result);
    }

    @Test
    public void testParseRelativePath() throws Exception {
        String string = "a/b/c";
        String result = URIHelper.emit(URIHelper.parse(string));
        assertEquals(string, result);
    }

    @Test
    public void testParseAbsolutePath() throws Exception {
        String string = "/a/b/c";
        String result = URIHelper.emit(URIHelper.parse(string));
        assertEquals(string, result);
    }

    @Test
    public void testParseEmptyString() throws Exception {
        String string = "";
        String result = URIHelper.emit(URIHelper.parse(string));
        assertEquals(string, result);
    }

    @Test
    public void testParseQueryString() throws Exception {
        String string = "?a=1&b=2";
        String result = URIHelper.emit(URIHelper.parse(string));
        assertEquals(string, result);
    }

    @Test
    public void testParseOpaqueWithQuery() throws Exception {
        String string = "sap+idoc:sap_r3?user=aladdin&password=opensesame&client=200&language=en&queue=x:yz";
        String result = URIHelper.emit(URIHelper.parse(string));
        assertEquals(string, result);
    }

    @Test
    public void testParseNonOpaqueWithQuery() throws Exception {
        String string = "sap+idoc://aladdin:opensesame@sappr3?client=200&language=en&queue=x:yz";
        String result = URIHelper.emit(URIHelper.parse(string));
        assertEquals(string, result);
    }

    @Test
    public void testParseNonOpaqueWithCurrentDirectory() throws Exception {
        String string = "ftp://aladdin:opensesame@example.com:21/./path/file?append=true&active=true&ascii=true";
        String result = URIHelper.emit(URIHelper.parse(string));
        assertEquals(string, result);
    }

    @Test
    public void testParseMailtoURI() throws Exception {
        String string = "mailto:bob@example.com?cc=jane@example.com&subject=Example&body=Example&attachment=message.xml";
        String result = URIHelper.emit(URIHelper.parse(string));
        assertEquals(string, result);
    }
}