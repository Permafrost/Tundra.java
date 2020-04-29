/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
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

package permafrost.tundra.content;

import com.wm.data.IData;
import junit.framework.TestCase;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.mime.MIMETypeHelper;
import javax.activation.MimeType;

public class ContentParserTest extends TestCase {

    public void testParseTSVWithNoHeaders() throws Exception {
        String content = "a\tb\tc\td\te\n" +
                "f\tg\th\ti\tj\n" +
                "k\tl\tm\tn\to\n" +
                "p\tq\tr\ts\tt";

        MimeType contentType = MIMETypeHelper.of("application/vnd.example+tsv; charset=UTF-8");

        IDataMap pipeline = new IDataMap();
        pipeline.put("$content.header?", "false");
        pipeline.put("$content.quote.character", "$null");

        ContentParser parser = new ContentParser(contentType, null, null, null, false, pipeline);

        IDataMap document = IDataMap.of(parser.parse(content));

        IData[] recordWithNoID = (IData[])document.get("recordWithNoID");

        assertEquals(4, recordWithNoID.length);

        IDataMap firstRecord = IDataMap.of(recordWithNoID[0]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("a", firstRecord.get("1"));
        assertEquals("b", firstRecord.get("2"));
        assertEquals("c", firstRecord.get("3"));
        assertEquals("d", firstRecord.get("4"));
        assertEquals("e", firstRecord.get("5"));

        IDataMap secondRecord = IDataMap.of(recordWithNoID[1]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("f", secondRecord.get("1"));
        assertEquals("g", secondRecord.get("2"));
        assertEquals("h", secondRecord.get("3"));
        assertEquals("i", secondRecord.get("4"));
        assertEquals("j", secondRecord.get("5"));

        IDataMap thirdRecord = IDataMap.of(recordWithNoID[2]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("k", thirdRecord.get("1"));
        assertEquals("l", thirdRecord.get("2"));
        assertEquals("m", thirdRecord.get("3"));
        assertEquals("n", thirdRecord.get("4"));
        assertEquals("o", thirdRecord.get("5"));

        IDataMap fourthRecord = IDataMap.of(recordWithNoID[3]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("p", fourthRecord.get("1"));
        assertEquals("q", fourthRecord.get("2"));
        assertEquals("r", fourthRecord.get("3"));
        assertEquals("s", fourthRecord.get("4"));
        assertEquals("t", fourthRecord.get("5"));
    }

    public void testParseTSVWithHeaders() throws Exception {
        String content = "a\tb\tc\td\te\n" +
                "f\tg\th\ti\tj\n" +
                "k\tl\tm\tn\to\n" +
                "p\tq\tr\ts\tt";

        MimeType contentType = MIMETypeHelper.of("application/vnd.example+tsv; charset=UTF-8");

        IDataMap pipeline = new IDataMap();
        pipeline.put("$content.header?", "true");
        pipeline.put("$content.quote.character", "$null");

        ContentParser parser = new ContentParser(contentType, null, null, null, false, pipeline);

        IDataMap document = IDataMap.of(parser.parse(content));

        IData[] recordWithNoID = (IData[])document.get("recordWithNoID");

        assertEquals(3, recordWithNoID.length);

        IDataMap firstRecord = IDataMap.of(recordWithNoID[0]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("f", firstRecord.get("a"));
        assertEquals("g", firstRecord.get("b"));
        assertEquals("h", firstRecord.get("c"));
        assertEquals("i", firstRecord.get("d"));
        assertEquals("j", firstRecord.get("e"));

        IDataMap secondRecord = IDataMap.of(recordWithNoID[1]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("k", secondRecord.get("a"));
        assertEquals("l", secondRecord.get("b"));
        assertEquals("m", secondRecord.get("c"));
        assertEquals("n", secondRecord.get("d"));
        assertEquals("o", secondRecord.get("e"));

        IDataMap thirdRecord = IDataMap.of(recordWithNoID[2]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("p", thirdRecord.get("a"));
        assertEquals("q", thirdRecord.get("b"));
        assertEquals("r", thirdRecord.get("c"));
        assertEquals("s", thirdRecord.get("d"));
        assertEquals("t", thirdRecord.get("e"));
    }

    public void testParseTSVWithDefaults() throws Exception {
        String content = "a\tb\tc\td\te\n" +
                "f\tg\th\ti\tj\n" +
                "k\tl\tm\tn\to\n" +
                "p\tq\tr\ts\tt";

        MimeType contentType = MIMETypeHelper.of("application/vnd.example+tsv; charset=UTF-8");

        ContentParser parser = new ContentParser(contentType, null, null, null, false, null);

        IDataMap document = IDataMap.of(parser.parse(content));

        IData[] recordWithNoID = (IData[])document.get("recordWithNoID");

        assertEquals(3, recordWithNoID.length);

        IDataMap firstRecord = IDataMap.of(recordWithNoID[0]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("f", firstRecord.get("a"));
        assertEquals("g", firstRecord.get("b"));
        assertEquals("h", firstRecord.get("c"));
        assertEquals("i", firstRecord.get("d"));
        assertEquals("j", firstRecord.get("e"));

        IDataMap secondRecord = IDataMap.of(recordWithNoID[1]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("k", secondRecord.get("a"));
        assertEquals("l", secondRecord.get("b"));
        assertEquals("m", secondRecord.get("c"));
        assertEquals("n", secondRecord.get("d"));
        assertEquals("o", secondRecord.get("e"));

        IDataMap thirdRecord = IDataMap.of(recordWithNoID[2]);
        assertEquals(5, IDataHelper.size(firstRecord));
        assertEquals("p", thirdRecord.get("a"));
        assertEquals("q", thirdRecord.get("b"));
        assertEquals("r", thirdRecord.get("c"));
        assertEquals("s", thirdRecord.get("d"));
        assertEquals("t", thirdRecord.get("e"));
    }
}