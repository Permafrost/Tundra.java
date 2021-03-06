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
import permafrost.tundra.data.IDataMap;

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
    public void testSubstituteOpaque() throws Exception {
        String string = "sap+idoc:sap_r3%25a%25";
        IDataMap map = new IDataMap();
        map.put("a", "1");
        String result = URIHelper.substitute(string, map);
        assertEquals("sap+idoc:sap_r31", result);
    }

    @Test
    public void testSubstituteOpaqueTemplate() throws Exception {
        String string = "sap+idoc:sap_r3{a}";
        IDataMap map = new IDataMap();
        map.put("a", "1");
        String result = URIHelper.substitute(string, map);
        assertEquals("sap+idoc:sap_r31", result);
    }

    @Test
    public void testSubstituteOpaqueTemplateWithFullyQualifiedKey() throws Exception {
        String string = "sap+idoc:sap_r3{a/b}";
        IDataMap map = new IDataMap();
        IDataMap a = new IDataMap();
        a.put("b", "1");
        map.put("a", a);
        String result = URIHelper.substitute(string, map);
        assertEquals("sap+idoc:sap_r31", result);
    }

    @Test
    public void testSubstituteNonOpaqueTemplateWithMissingVariable() throws Exception {
        String string = "sap+idoc://{user}:{password}@{host}?client={client}&language={language}&queue={queue}";

        IDataMap map = new IDataMap();
        map.put("user", "aladdin");
        map.put("password", "opensesame");
        map.put("host", "sappr3");
        map.put("client", "200");
        map.put("queue", "x:yz");

        assertEquals("sap+idoc://aladdin:opensesame@sappr3?client=200&language=%7Blanguage%7D&queue=x:yz", URIHelper.substitute(string, map));
    }

    @Test
    public void testSubstituteNonOpaqueURIWithMissingVariable() throws Exception {
        String string = "sap+idoc://%25user%25:%25password%25@%25host%25?client=%25client%25&language=%25language%25&queue=%25queue%25";

        IDataMap map = new IDataMap();
        map.put("user", "aladdin");
        map.put("password", "opensesame");
        map.put("host", "sappr3");
        map.put("client", "200");
        map.put("queue", "x:yz");

        assertEquals("sap+idoc://aladdin:opensesame@sappr3?client=200&language=%25language%25&queue=x:yz", URIHelper.substitute(string, map));
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