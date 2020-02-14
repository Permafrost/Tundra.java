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
import permafrost.tundra.html.HTMLEntity;
import permafrost.tundra.html.HTMLHelper;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.io.InputStreamHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Serializes IData objects to HTML.
 */
public class IDataHTMLParser extends IDataParser {
    /**
     * Default constructor.
     */
    public IDataHTMLParser() {
        super("text/html");
    }

    /**
     * This method has not implemented.
     *
     * @param inputStream The input stream to be decoded.
     * @param charset     The character set to use.
     * @return An IData representation of the given input stream data.
     * @throws IOException                   If there is a problem reading from the stream.
     * @throws UnsupportedOperationException As this method has not been implemented.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException {
        throw new UnsupportedOperationException("decode method not implemented");
    }

    /**
     * Encodes the given IData document as HTML to the given output stream.
     *
     * @param outputStream The stream to write the encoded IData to.
     * @param document     The IData document to be encoded.
     * @param charset      The character set to use.
     * @throws IOException If there is a problem writing to the stream.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException {
        InputOutputHelper.copy(InputStreamHelper.normalize(emit(document, Integer.MAX_VALUE), charset), outputStream);
    }

    /**
     * Returns an HTML representation of the given IData object.
     *
     * @param input             The IData to convert to HTML.
     * @param maxDepth          The maximum depth children will be encoded to.
     * @return                  The HTML representation of the IData.
     * @throws IOException      If an IO error occurs.
     */
    public String emit(IData input, int maxDepth) throws IOException {
        if (input == null) return HTMLEntity.NULL.toString();
        if (maxDepth < 1) maxDepth = Integer.MAX_VALUE;
        return emit(new StringBuilder(), input, maxDepth, 0).toString();
    }

    /**
     * Converts an IData[] to an HTML string.
     *
     * @param input             The IData[] to be converted.
     * @param maxDepth          The maximum depth children will be encoded to.
     * @return                  The HTML string that represents the given IData[].
     * @throws IOException      If an IO error occurs.
     */
    public String emit(IData[] input, int maxDepth) throws IOException {
        if (input == null) return HTMLEntity.NULL.toString();
        if (maxDepth < 1) maxDepth = Integer.MAX_VALUE;
        return emit(new StringBuilder(), input, maxDepth, 0).toString();
    }

    /**
     * Returns an HTML representation of the given IData object.
     *
     * @param appendable        The Appendable to append the HTML to.
     * @param input             The IData to convert to HTML.
     * @param maxDepth          The maximum depth children will be encoded to.
     * @param currentDepth      The current depth being encoded.
     * @return                  The given Appendable for method chaining convenience.
     * @throws IOException      If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, IData input, int maxDepth, int currentDepth) throws IOException {
        input = IDataHelper.normalize(input);
        int size = IDataHelper.size(input);

        if (size == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else if (currentDepth >= maxDepth) {
            appendable.append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString());
        } else {
            IDataCursor cursor = input.getCursor();
            IData[] array = IDataUtil.getIDataArray(cursor, "recordWithNoID");
            cursor.destroy();

            if (array != null) {
                emit(appendable, array, maxDepth, currentDepth);
            } else {
                // table
                appendable.append("<table class=\"IData\">");

                // thead
                appendable.append("<thead class=\"IData-Head\">");
                appendable.append("<tr>");
                appendable.append("<th>Key</th>");
                appendable.append("<th>Value</th>");
                appendable.append("</tr>");
                appendable.append("</thead>");

                // tbody
                appendable.append("<tbody class=\"IData-Body\">");

                cursor = input.getCursor();

                while(cursor.next()) {
                    String key = cursor.getKey();
                    Object value = cursor.getValue();

                    appendable.append("<tr>");
                    appendable.append("<th>");
                    appendable.append(HTMLHelper.encode(key));
                    appendable.append("</th>");
                    appendable.append("<td>");

                    if (value == null) {
                        appendable.append(HTMLEntity.NULL.toString());
                    } else {
                        if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                            emit(appendable, IDataHelper.toIDataArray(value), maxDepth, currentDepth + 1);
                        } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                            emit(appendable, IDataHelper.toIData(value), maxDepth, currentDepth + 1);
                        } else if (value instanceof Object[][]) {
                            emit(appendable, (Object[][])value);
                        } else if (value instanceof Object[]) {
                            emit(appendable, (Object[])value);
                        } else {
                            appendable.append(HTMLHelper.encode(value.toString()));
                        }
                    }
                    appendable.append("</td>");
                    appendable.append("</tr>");
                }

                cursor.destroy();

                appendable.append("</tbody>");
                appendable.append("</table>");
            }
        }

        return appendable;
    }

    /**
     * Converts an IData[] to an HTML string.
     *
     * @param appendable        The Appendable to append the HTML to.
     * @param input             The IData[] to be converted.
     * @param maxDepth          The maximum depth children will be encoded to.
     * @param currentDepth      The current depth being encoded.
     * @return                  The given Appendable for method chaining convenience.
     * @throws IOException      If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, IData[] input, int maxDepth, int currentDepth) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else if (currentDepth >= maxDepth) {
            appendable.append(HTMLEntity.VERTICAL_ELLIPSIS.toString());
        } else {
            String[] keys = IDataHelper.getKeys(input);

            // table
            appendable.append("<table class=\"IDataArray\">");

            // thead
            appendable.append("<thead class=\"IDataArray-Head\">");
            appendable.append("<tr>");
            for (String key : keys) {
                appendable.append("<th>");
                appendable.append(HTMLHelper.encode(key));
                appendable.append("</th>");
            }
            appendable.append("</tr>");
            appendable.append("</thead>");

            // tbody
            appendable.append("<tbody class=\"IDataArray-Body\">");
            for (IData document : input) {
                if (document != null) {
                    IDataCursor cursor = document.getCursor();
                    appendable.append("<tr>");
                    for (String key : keys) {
                        appendable.append("<td>");
                        Object value = IDataUtil.get(cursor, key);
                        if (value == null) {
                            appendable.append(HTMLEntity.NULL.toString());
                        } else {
                            if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                                emit(appendable, IDataHelper.toIDataArray(value), maxDepth, currentDepth + 1);
                            } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                                emit(appendable, IDataHelper.toIData(value), maxDepth, currentDepth + 1);
                            } else if (value instanceof Object[][]) {
                                emit(appendable, (Object[][])value);
                            } else if (value instanceof Object[]) {
                                emit(appendable, (Object[])value);
                            } else {
                                appendable.append(HTMLHelper.encode(value.toString()));
                            }
                        }
                        appendable.append("</td>");
                    }
                    appendable.append("</tr>");
                    cursor.destroy();
                }
            }
            appendable.append("</tbody>");
            appendable.append("</table>");
        }

        return appendable;
    }

    /**
     * Converts an Object[][] to an HTML string.
     *
     * @param appendable        The Appendable to append the HTML to.
     * @param input             The Object[][] to be converted.
     * @return                  The given Appendable for method chaining convenience.
     * @throws IOException      If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, Object[][] input) throws IOException {
        appendable.append("<table>");
        appendable.append("<tbody>");
        for (Object[] row : input) {
            appendable.append("<tr>");
            for (Object item : row) {
                appendable.append("<td>");
                if (item == null) {
                    appendable.append(HTMLEntity.NULL.toString());
                } else {
                    appendable.append(HTMLHelper.encode(item.toString()));
                }
                appendable.append("</td>");
            }
            appendable.append("</tr>");
        }
        appendable.append("</tbody>");
        appendable.append("</table>");

        return appendable;
    }

    /**
     * Converts an Object[] to an HTML string.
     *
     * @param appendable        The Appendable to append the HTML to.
     * @param input             The Object[] to be converted.
     * @return                  The given Appendable for method chaining convenience.
     * @throws IOException      If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, Object[] input) throws IOException {
        appendable.append("<table>");
        appendable.append("<tbody>");
        for (Object item : input) {
            appendable.append("<tr>");
            appendable.append("<td>");
            if (item == null) {
                appendable.append(HTMLEntity.NULL.toString());
            } else {
                appendable.append(HTMLHelper.encode(item.toString()));
            }
            appendable.append("</td>");
            appendable.append("</tr>");
        }
        appendable.append("</tbody>");
        appendable.append("</table>");

        return appendable;
    }
}
