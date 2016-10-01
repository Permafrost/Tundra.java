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

package permafrost.tundra.server.content;

import com.wm.app.b2b.server.HTTPState;
import com.wm.app.b2b.server.ProtocolInfoIf;
import com.wm.net.HttpHeader;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.lang.Startable;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * A custom content handler which implements support for HTTP compression using the gzip algorithm by
 * wrapping another content handler, then checking if the request is HTTP and includes the
 * `Content-Encoding: gzip` header and if so first wraps the input stream with a gzip decompresssion
 * stream before calling the proxied content handler to process the request as normal.
 */
public class HTTPCompressionContentHandler extends FilterContentHandler {
    /**
     * Regular expression for MIME media types excluded from this filter.
     */
    protected static final Pattern EXCLUDED_CONTENT_TYPES = Pattern.compile("^application\\/(wm-)?soap(\\+xml)?$");

    /**
     * Creates a new HTTPCompressionContentHandler object.
     *
     * @param startable Used to start and stop content filtering.
     */
    public HTTPCompressionContentHandler(Startable startable) {
        super(startable);
    }

    /**
     * Reads input for service invocation. Turns properly formatted input into an instance of Values
     * suitable for service invocation. Called before service invocation to provide input. If the
     * transport is HTTP, and a Content-Encoding header was specified with the value "gzip" or "deflate",
     * the stream is wrapped in a decompression stream.
     *
     * @param contentHandlerInput   The input arguments for processing by the content handler.
     * @throws IOException          If an error occurs writing to the output stream.
     */
    public void getInputValues(ContentHandlerInput contentHandlerInput) throws IOException {
        if (startable.isStarted()) {
            String contentType = contentHandlerInput.getInvokeState().getContentType();

            if (!(contentType != null && EXCLUDED_CONTENT_TYPES.matcher(contentType).matches())) {
                ProtocolInfoIf protocolInfoIf = contentHandlerInput.getInvokeState().getProtocolInfoIf();

                if (protocolInfoIf instanceof HTTPState) {
                    HTTPState httpState = (HTTPState)protocolInfoIf;
                    String contentEncoding = HTTPStateHelper.getHeader(httpState, HttpHeader.CONTENT_ENCODING);

                    if (contentEncoding != null) {
                        if (contentEncoding.equalsIgnoreCase("gzip")) {
                            contentHandlerInput.setInputStream(new GZIPInputStream(contentHandlerInput.getInputStream(), InputOutputHelper.DEFAULT_BUFFER_SIZE));
                            HTTPStateHelper.removeHeader(httpState, HttpHeader.CONTENT_ENCODING);
                        } else if (contentEncoding.equalsIgnoreCase("deflate")) {
                            contentHandlerInput.setInputStream(new DeflaterInputStream(contentHandlerInput.getInputStream()));
                            HTTPStateHelper.removeHeader(httpState, HttpHeader.CONTENT_ENCODING);
                        }
                    }
                }
            }
        }
    }
}
