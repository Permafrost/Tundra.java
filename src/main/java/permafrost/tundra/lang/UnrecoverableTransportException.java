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

import permafrost.tundra.content.Content;
import permafrost.tundra.content.ContentAttached;
import java.util.Map;

/**
 * An exception indicating that an unrecoverable transport error has occurred.
 */
public class UnrecoverableTransportException extends UnrecoverableException implements ContentAttached {
    /**
     * The content associated with this exception.
     */
    protected Content content;

    /**
     * Constructs a new UnrecoverableTransportException with the given message.
     *
     * @param  message                  A message describing why the UnrecoverableTransportException was thrown.
     */
    public UnrecoverableTransportException(String message) {
        this(message, null);
    }


    /**
     * Constructs a new UnrecoverableTransportException with the given message.
     *
     * @param  message                  A message describing why the UnrecoverableTransportException was thrown.
     * @param  content                  The content associated with the exception.
     */
    public UnrecoverableTransportException(String message, Content content) {
        this(message, content, null);
    }

    /**
     * Constructs a new UnrecoverableTransportException with the given message and cause.
     *
     * @param  message                  A message describing why the UnrecoverableTransportException was thrown.
     * @param  content                  The content associated with the exception.
     * @param  cause                    The cause of this Exception.
     */
    public UnrecoverableTransportException(String message, Content content, Throwable cause) {
        super(message, cause);
        this.content = Content.normalize(content);
    }

    /**
     * Returns the content associated with the exception; typically the delivery response content returned by a server
     * that resulted in this exception being thrown.
     *
     * @return The content associated with the exception.
     */
    public Content getContent() {
        return content;
    }

    /**
     * Sets the content associated with this exception.
     *
     * @param  content The content to be attached to this exception.
     */
    public void setContent(Content content) {
        this.content = Content.normalize(content);
    }

    /**
     * Returns the properties of this object.
     *
     * @return the properties of this object.
     */
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put("$exception.content", content.getProperties());
        return properties;
    }
}
