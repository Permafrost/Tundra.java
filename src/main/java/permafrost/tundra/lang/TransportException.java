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

package permafrost.tundra.lang;

import permafrost.tundra.mime.MIMETypeHelper;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * An exception indicating that a transport error has occurred.
 */
public class TransportException extends RecoverableException {
    /**
     * The content associated with this exception.
     */
    protected byte[] content;
    /**
     * The content type associated with this exception's content.
     */
    protected MimeType contentType;

    /**
     * Constructs a new TransportException with the given message.
     *
     * @param  message                  A message describing why the TransportException was thrown.
     * @param  content                  The content associated with the exception.
     * @param  contentType              The content type associated with the content.
     * @throws MimeTypeParseException   If the content type cannot be parsed as a MIME media type.
     */
    public TransportException(String message, byte[] content, String contentType) throws MimeTypeParseException {
        this(message, content, contentType, null);
    }

    /**
     * Constructs a new TransportException with the given message and cause.
     *
     * @param  message                  A message describing why the TransportException was thrown.
     * @param  content                  The content associated with the exception.
     * @param  contentType              The content type associated with the content.
     * @param  cause                    The cause of this Exception.
     * @throws MimeTypeParseException   If the content type cannot be parsed as a MIME media type.
     */
    public TransportException(String message, byte[] content, String contentType, Throwable cause) throws MimeTypeParseException {
        super(message, cause);
        this.content = content;
        if (contentType != null) {
            this.contentType = new MimeType(contentType);
        } else {
            this.contentType = new MimeType(MIMETypeHelper.DEFAULT_MIME_TYPE);
        }
    }

    /**
     * Returns the content associated with the exception; typically the delivery response content returned by a server
     * that resulted in this exception being thrown.
     *
     * @return The content associated with the exception.
     */
    public byte[] getContent() {
        return this.content;
    }

    /**
     * Returns the content type associated with the content of this exception.
     *
     * @return The content type associated with the content of this exception.
     */
    public MimeType getContentType() {
        return this.contentType;
    }
}
