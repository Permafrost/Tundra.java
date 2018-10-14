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

package permafrost.tundra.net.http.route;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import permafrost.tundra.data.IDataMap;
import java.io.File;

public class HTTPRouteTest {

    @Test
    public void testMatchWithTemplateConstantAssignedAValue() throws Exception {
        HTTPRoute route = new HTTPRoute("get", "/{a%20}/{b}/{c=foo.bar%3Abaz}", "service:name", "route description", new File("source file"));

        IDataMap result = IDataMap.of(route.match("get", "/foo/bar/baz"));

        assertEquals(4, result.size());
        assertEquals("foo", result.get("a "));
        assertEquals("bar", result.get("b"));
        assertEquals("foo.bar:baz", result.get("c"));
        assertEquals("baz", result.get("c=foo.bar%3Abaz"));
    }

    @Test
    public void testMatchWithTemplateConstantAssignedNoValue() throws Exception {
        HTTPRoute route = new HTTPRoute("get", "/{a%20}/{b}/{c=foo.bar%3Abaz}", "service:name", "route description", new File("source file"));

        IDataMap result = IDataMap.of(route.match("get", "/foo/bar/"));

        assertEquals(3, result.size());
        assertEquals("foo", result.get("a "));
        assertEquals("bar", result.get("b"));
        assertEquals("foo.bar:baz", result.get("c"));
    }
}
