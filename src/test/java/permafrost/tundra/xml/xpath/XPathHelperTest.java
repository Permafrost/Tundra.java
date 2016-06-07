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

package permafrost.tundra.xml.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.xml.XMLHelper;
import permafrost.tundra.xml.dom.Nodes;

public class XPathHelperTest {
    Document document;

    @Before
    public void setUp() throws Exception {
        String content = "<a><z>1</z><z>2</z><z>3</z><b><c>Example</c></b><b><c><d>Example 2</d></c></b><b></b></a>";

        document = XMLHelper.parse(InputStreamHelper.normalize(content));
    }

    @Test
    public void testExistsReturnsTrue() throws Exception {
        assertTrue(XPathHelper.exists(document, XPathHelper.compile("/a/b/c")));
    }

    @Test
    public void testExistsReturnsFalse() throws Exception {
        assertFalse(XPathHelper.exists(document, XPathHelper.compile("/a/b/d")));
    }

    @Test
    public void testGetReturnsNodes() throws Exception {
        String query = "/a/b/c";
        NodeList nodes = XPathHelper.get(document, XPathHelper.compile(query));
        assertEquals(2, nodes.getLength());
    }

    @Test
    public void testGetReturnsNoNodes() throws Exception {
        String query = "/a/b/d";
        Nodes nodes = XPathHelper.get(document, XPathHelper.compile(query));
        assertNull(nodes);
    }

    @Test
    public void evaluateSingleton() throws Exception {
        String query = "/a/b/c/d";
        assertTrue(XPathHelper.evaluate(document, XPathHelper.compile(query), "Example 2"));
    }

    @Test
    public void evaluateList() throws Exception {
        String query = "/a[1]/z";
        assertTrue(XPathHelper.evaluate(document, XPathHelper.compile(query), "1", "2", "3"));
    }
}