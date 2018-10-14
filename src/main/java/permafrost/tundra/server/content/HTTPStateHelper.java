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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.wm.app.b2b.server.HTTPState;
import com.wm.app.b2b.server.ProtocolState;
import com.wm.net.HttpHeader;

/**
 * A collection of convenience methods for working with the Integration Server HTTPState object.
 */
public final class HTTPStateHelper {
    /**
     * Disallow instantiation of this class.
     */
    private HTTPStateHelper() {}

    /**
     * Returns the value associated with the given HTTP header from the given HTTPState object,
     * or null if the header does not exist.
     *
     * @param httpState An HTTPState object.
     * @param header    The HTTP header whose value is to be returned.
     * @return          The value associated with the given HTTP header, or null if the header does not exist.
     */
    public static String getHeader(HTTPState httpState, String header) {
        if (httpState == null) return null;

        String value = null;

        HttpHeader headers = httpState.getRequestHeader();
        if (headers != null) {
            value = headers.getFieldValue(header);
            if (value != null) value = value.trim();
        }

        return value;
    }

    /**
     * Removes the given header from the given HTTPState object.
     *
     * @param httpState An HTTPState object.
     * @param header    The HTTP header to be removed.
     */
    public static void removeHeader(HTTPState httpState, String header) {
        if (httpState == null) return;

        HttpHeader headers = httpState.getRequestHeader();
        if (headers != null) {
            headers.clearField(header);

            // regenerate the transport info stored in the protocol state
            try {
                // use reflection to invoke protected method initProtocolProps
                Method initProtocolProps = ProtocolState.class.getDeclaredMethod("initProtocolProps");
                initProtocolProps.setAccessible(true);
                initProtocolProps.invoke(httpState);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
