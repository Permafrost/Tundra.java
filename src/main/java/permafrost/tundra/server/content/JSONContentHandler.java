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

import com.wm.app.b2b.server.ContentHandler;
import com.wm.app.b2b.server.InvokeState;
import com.wm.util.Values;
import permafrost.tundra.data.IDataJSONParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An Integration Server content handler for JSON.
 */
public class JSONContentHandler  implements ContentHandler {
    /**
     * The default MIME media type for JSON content.
     */
    public static final String DEFAULT_JSON_CONTENT_TYPE = "application/json";

    /**
     * The MIME media type handled by this class.
     */
    protected String contentType;
    /**
     * The variable name used when adding the parsed JSON content to the input pipeline.
     */
    protected String inputName = "$document";

    /**
     * Creates a new JSONContentHandler object for the default JSON MIME media type.
     */
    public JSONContentHandler() {
        this(DEFAULT_JSON_CONTENT_TYPE);
    }

    /**
     * Creates a new JSONContentHandler object to handle the given MIME media type.
     *
     * @param contentType The MIME media type to be handled.
     */
    public JSONContentHandler(String contentType) {
        this(null, contentType);
    }

    /**
     * Creates a new JSONContentHandler object to handle the given MIME media type.
     *
     * @param inputName   The variable name used when adding the parsed JSON content to the input pipeline.
     * @param contentType The MIME media type to be handled.
     */
    public JSONContentHandler(String inputName, String contentType) {
        if (contentType == null) throw new NullPointerException("contentType must not be null");
        if (inputName != null) this.inputName = inputName;
        this.contentType = contentType;
    }

    /**
     * Returns the MIME media type handled by this object.
     *
     * @return The MIME media type handled by this object.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Reads input for service invocation. Turns properly formatted input into an instance of Values
     * suitable for service invocation. Called before service invocation to provide input.
     *
     * @param inputStream  The input stream from which to read.
     * @param invokeState  The current invocation state (such as the current user).
     * @return             The prepared service invocation input pipeline.
     * @throws IOException If an error occurs reading from the input stream.
     */
    public Values getInputValues(InputStream inputStream, InvokeState invokeState) throws IOException {
        Values pipeline = new Values();
        pipeline.put(inputName, new IDataJSONParser().parse(inputStream));
        return pipeline;
    }

    /**
     * Encodes output of service invocation. Writes output data (including possibly error messages)
     * directly to the output stream.
     *
     * @param outputStream The output stream to which to write.
     * @param output       The output values to encode.
     * @param invokeState  The current invocation state (such as the current user).
     * @throws IOException If an error occurs writing to the output stream.
     */
    public void putOutputValues(OutputStream outputStream, Values output, InvokeState invokeState) throws IOException {
        // do nothing
    }
}
