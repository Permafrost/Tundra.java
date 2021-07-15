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

package permafrost.tundra.server.content;

import com.wm.app.b2b.server.InvokeState;
import org.unbescape.java.JavaEscape;
import org.unbescape.java.JavaEscapeLevel;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.Startable;
import permafrost.tundra.mime.MIMETypeHelper;
import permafrost.tundra.server.ServerLogHelper;
import permafrost.tundra.server.ServerLogLevel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.regex.Pattern;
import javax.activation.MimeTypeParseException;

/**
 * A content handler which logs all processed input content.
 */
public class LoggingContentHandler extends FilterContentHandler {
    /**
     * The default log level used by this object.
     */
    private static final ServerLogLevel DEFAULT_LOG_LEVEL = ServerLogLevel.INFO;

    /**
     * The regular expression pattern which matches known text MIME media types.
     */
    protected static final Pattern TEXT_CONTENT_PATTERN = Pattern.compile("^(text\\/.+|[^\\/]+\\/xml|[^\\/]+\\/json|.+\\+xml|.+\\+json|.+\\+\\wsv)$");

    /**
     * Constructs a new LoggingContentHandler, which logs all processed input content.
     *
     * @param startable Used to start and stop content filtering.
     */
    public LoggingContentHandler(Startable startable) {
        super(startable);
    }

    /**
     * Reads input for service invocation. Turns properly formatted input into an instance of Values
     * suitable for service invocation. Called before service invocation to provide input.
     *
     * @param contentHandlerInput   The input arguments for processing by the content handler.
     * @throws IOException          If an error occurs writing to the output stream.
     */
    @Override
    public void getInputValues(ContentHandlerInput contentHandlerInput) throws IOException {
        if (startable.isStarted()) {
            byte[] bytes = InputStreamHelper.read(contentHandlerInput.getInputStream());
            if (bytes != null) {
                contentHandlerInput.setInputStream(new ByteArrayInputStream(bytes));
                InvokeState invokeState = contentHandlerInput.getInvokeState();
                String contentType = null;
                Charset charset = null;

                if (invokeState != null) {
                    try {
                        contentType = MIMETypeHelper.normalize(invokeState.getContentType());
                    } catch (MimeTypeParseException ex) {
                        // do nothing
                    }
                    charset = CharsetHelper.normalize(invokeState.getContentEncoding());
                }

                if (contentType == null) contentType = MIMETypeHelper.DEFAULT_MIME_TYPE_STRING;
                if (charset == null) charset = CharsetHelper.DEFAULT_CHARSET;

                String content;
                if (TEXT_CONTENT_PATTERN.matcher(contentType).matches()) {
                    content = JavaEscape.escapeJava(new String(bytes, charset), JavaEscapeLevel.LEVEL_2_ALL_NON_ASCII_PLUS_BASIC_ESCAPE_SET);
                } else {
                    content = BytesHelper.base64Encode(bytes);
                }

                try {
                    ServerLogHelper.log(this.getClass().getName(), DEFAULT_LOG_LEVEL, MessageFormat.format("{0} -- content-type = {1}, content = {2}", this.getClass().getName(), contentType, content), null, false);
                } catch(Exception ex) {
                    // do nothing
                }
            }
        }
    }
}
