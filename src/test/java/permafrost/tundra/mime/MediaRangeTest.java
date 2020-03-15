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

package permafrost.tundra.mime;

import org.junit.Test;

import javax.activation.MimeType;
import java.util.List;

import static org.junit.Assert.*;

public class MediaRangeTest {

    @Test
    public void resolve() {
        List<MimeType> supportedTypes = MIMETypeHelper.of("application/atom+xml", "application/xml");

        assertEquals("application/atom+xml", match("application/atom+xml", supportedTypes));
        assertEquals("application/atom+xml", match("application/atom+xml; q=1", supportedTypes));
        assertNull(match("application/atom+xml; q=0", supportedTypes));
        assertEquals("application/xml", match("application/xml; q=1", supportedTypes));
        assertEquals("application/xml", match("application/atom+xml; q=0, application/xml; q=1", supportedTypes));
        assertEquals("application/atom+xml", match("application/*; q=1", supportedTypes));
        assertEquals("application/atom+xml", match("application/atom+xml; q=0, application/*; q=1", supportedTypes));
        assertEquals("application/atom+xml", match("*/*", supportedTypes));

        supportedTypes = MIMETypeHelper.of("application/xml", "text/xml");

        assertEquals("text/xml", match("text/*;q=0.5, */*;q=0.1", supportedTypes));
        assertNull(match("text/html, application/atom+xml; q=0.9", supportedTypes));

        supportedTypes = MIMETypeHelper.of("application/json", "text/html");

        assertEquals("application/json", match("application/json, text/javascript, */*", supportedTypes));
        assertEquals("application/json", match("application/json, text/html;q=0.9", supportedTypes));

        supportedTypes = MIMETypeHelper.of("application/vnd.example+json", "application/vnd.example+xml");

        assertEquals("application/vnd.example+json", match("application/json, application/xml, text/javascript, */*", supportedTypes));
        assertEquals("application/vnd.example+xml", match("application/xml, text/xml;q=0.9", supportedTypes));
        assertNull(match("text/xml;q=0.9", supportedTypes));
    }

    protected String match(String acceptedTypes, List<MimeType> supportedTypes) {
        MimeType resolved = MediaRange.resolve(MediaRange.parse(acceptedTypes), supportedTypes);
        return resolved == null ? null : resolved.toString();
    }
}