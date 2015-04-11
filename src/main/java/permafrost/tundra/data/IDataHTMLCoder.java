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

package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataPortable;
import com.wm.data.IDataUtil;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.html.HTMLEntity;
import permafrost.tundra.html.HTMLHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class IDataHTMLCoder extends IDataStringCoder {
    /**
     * Encodes the given IData document as HTML to the given output stream.
     * @param outputStream  The stream to write the encoded IData to.
     * @param document      The IData document to be encoded.
     * @param charset       The character set to use.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public void encode(OutputStream outputStream, IData document, Charset charset) throws IOException {
        StreamHelper.copy(StreamHelper.normalize(encodeToString(document), charset), outputStream);
    }

    /**
     * This method has not implemented.
     * @param inputStream                       The input stream to be decoded.
     * @param charset                           The character set to use.
     * @return                                  An IData representation of the given input stream data.
     * @throws IOException                      If there is a problem reading from the stream.
     * @throws UnsupportedOperationException    As this method has not been implemented.
     */
    public IData decode(InputStream inputStream, Charset charset) throws IOException {
        throw new UnsupportedOperationException("decode method not implemented");
    }

    /**
     * The MIME media type for HTML.
     * @return HTML MIME media type.
     */
    public String getContentType() {
        return "text/html";
    }

    /**
     * Returns an HTML representation of the given IData object.
     * @param input The IData to convert to HTML.
     * @return      The HTML representation of the IData.
     */
    @Override
    public String encodeToString(IData input) {
        if (input == null) return HTMLEntity.NULL.toString();

        input = IDataHelper.normalize(input);
        int size = IDataHelper.size(input);
        StringBuilder buffer = new StringBuilder();

        if (size == 0) {
            buffer.append(HTMLEntity.EMPTY.toString());
        } else {
            // table
            buffer.append("<table>");

            // thead
            buffer.append("<thead>");
            buffer.append("<tr>");
            buffer.append("<th>Key</th>");
            buffer.append("<th>Value</th>");
            buffer.append("</tr>");
            buffer.append("</thead>");

            // tbody
            buffer.append("<tbody>");
            for (Map.Entry<String, Object> entry : new IterableIData(input)) {
                String key = entry.getKey();
                Object value = entry.getValue();

                buffer.append("<tr>");
                buffer.append("<th>");
                buffer.append(HTMLHelper.encode(key));
                buffer.append("</th>");
                buffer.append("<td>");

                if (value == null) {
                    buffer.append(HTMLEntity.NULL.toString());
                } else {
                    if (value instanceof IData) {
                        buffer.append(encodeToString((IData) value));
                    } else if (value instanceof IData[]) {
                        buffer.append(encodeToString((IData[]) value));
                    } else if (value instanceof Object[][]) {
                        buffer.append(encodeToString((Object[][]) value));
                    } else if (value instanceof Object[]) {
                        buffer.append(encodeToString((Object[]) value));
                    } else {
                        buffer.append(HTMLHelper.encode(value.toString()));
                    }
                }
                buffer.append("</td>");
                buffer.append("</tr>");
            }
            buffer.append("</tbody>");
            buffer.append("</table>");
        }

        return buffer.toString();
    }

    /**
     * Converts an IData[] to an HTML string.
     * @param input The IData[] to be converted.
     * @return      The HTML string that represents the given IData[].
     */
    public String encodeToString(IData[] input) {
        if (input == null) return HTMLEntity.NULL.toString();

        String[] keys = IDataHelper.getKeys(input);
        StringBuilder buffer = new StringBuilder();

        if (input.length == 0) {
            buffer.append(HTMLEntity.EMPTY.toString());
        } else {
            // table
            buffer.append("<table>");

            // thead
            buffer.append("<thead>");
            buffer.append("<tr>");
            for (String key : keys) {
                buffer.append("<th>");
                buffer.append(HTMLHelper.encode(key));
                buffer.append("</th>");
            }
            buffer.append("</tr>");
            buffer.append("</thead>");

            // tbody
            buffer.append("<tbody>");
            for (IData document : input) {
                IDataCursor cursor = document.getCursor();
                buffer.append("<tr>");
                for (String key : keys) {
                    buffer.append("<td>");
                    Object value = IDataUtil.get(cursor, key);
                    if (value == null) {
                        buffer.append(HTMLEntity.NULL.toString());
                    } else {
                        if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                            buffer.append(encodeToString(IDataHelper.toIData(value)));
                        } else if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                            buffer.append(encodeToString(IDataHelper.toIDataArray(value)));
                        } else if (value instanceof Object[][]) {
                            buffer.append(encodeToString((Object[][]) value));
                        } else if (value instanceof Object[]) {
                            buffer.append(encodeToString((Object[]) value));
                        } else {
                            buffer.append(HTMLHelper.encode(value.toString()));
                        }
                    }
                    buffer.append("</td>");
                }
                buffer.append("</tr>");
                cursor.destroy();
            }
            buffer.append("</tbody>");
            buffer.append("</table>");
        }
        return buffer.toString();
    }

    /**
     * Converts an Object[][] to an HTML string.
     * @param input The Object[][] to be converted.
     * @return      The HTML string that represents the given Object[][].
     */
    protected String encodeToString(Object[][] input) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("<table>");
        buffer.append("<tbody>");
        for (Object[] row : input) {
            buffer.append("<tr>");
            for (Object item : row) {
                buffer.append("<td>");
                if (item == null) {
                    buffer.append(HTMLEntity.NULL.toString());
                } else {
                    buffer.append(HTMLHelper.encode(item.toString()));
                }
                buffer.append("</td>");
            }
            buffer.append("</tr>");
        }
        buffer.append("</tbody>");
        buffer.append("</table>");

        return buffer.toString();
    }

    /**
     * Converts an Object[] to an HTML string.
     * @param input The Object[] to be converted.
     * @return      The HTML string that represents the given Object[].
     */
    protected String encodeToString(Object[] input) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("<table>");
        buffer.append("<tbody>");
        for (Object item : input) {
            buffer.append("<tr>");
            buffer.append("<td>");
            if (item == null) {
                buffer.append(HTMLEntity.NULL.toString());
            } else {
                buffer.append(HTMLHelper.encode(item.toString()));
            }
            buffer.append("</td>");
            buffer.append("</tr>");
        }
        buffer.append("</tbody>");
        buffer.append("</table>");

        return buffer.toString();
    }
}
