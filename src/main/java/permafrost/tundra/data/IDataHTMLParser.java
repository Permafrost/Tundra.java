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

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataPortable;
import com.wm.data.IDataUtil;
import com.wm.data.MBoolean;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import permafrost.tundra.html.HTMLEntity;
import permafrost.tundra.html.HTMLHelper;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.ObjectHelper;
import permafrost.tundra.lang.StringHelper;
import permafrost.tundra.security.MessageDigestHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Serializes IData objects to HTML.
 */
public class IDataHTMLParser extends IDataParser {
    /**
     * Whether the HTML emitted should be an embeddable fragment, and whether tables should be in portrait orientation.
     */
    boolean fragment, portraitOrientation;
    /**
     * The limit on each dimension in the emitted HTML.
     */
    int maxValueLength, maxLength, maxWidth, maxDepth;

    /**
     * Constructs a new IDataHTMLParser object which emits HTML fragments for embedding in HTML documents.
     */
    public IDataHTMLParser() {
        this(true);
    }

    /**
     * Constructs a new IDataHTMLParser object.
     *
     * @param fragment IF true, emits HTML fragments for embedding in HTML documents, otherwise emits a complete HTML
     *                 document.
     */
    public IDataHTMLParser(boolean fragment) {
        this(fragment, true);
    }

    /**
     * Constructs a new IDataHTMLParser object.
     *
     * @param fragment            IF true, emits HTML fragments for embedding in HTML documents, otherwise emits a
     *                            complete HTML document.
     * @param portraitOrientation If true, IData documents will be serialized vertically with one key value per row.
     */
    public IDataHTMLParser(boolean fragment, boolean portraitOrientation) {
        this(fragment, portraitOrientation, 0, 0, 0, 0);
    }

    /**
     * Constructs a new IDataHTMLParser object with limits on each dimension.
     *
     * @param fragment              If true emits HTML fragments for embedding otherwise emits a complete HTML document.
     * @param portraitOrientation   If true IData documents will be serialized vertically with one key value per row.
     * @param maxValueLength        The maximum length of individual values.
     * @param maxLength             The maximum number of items in lists.
     * @param maxWidth              The maximum number of items if table rows.
     * @param maxDepth              The maximum number of children recursed.
     */
    public IDataHTMLParser(boolean fragment, boolean portraitOrientation, int maxValueLength, int maxLength, int maxWidth, int maxDepth) {
        super("text/html");
        this.fragment = fragment;
        this.portraitOrientation = portraitOrientation;
        this.maxValueLength = maxValueLength < 1 ? Integer.MAX_VALUE : maxValueLength;
        this.maxLength = maxLength < 1 ? Integer.MAX_VALUE : maxLength;
        this.maxWidth = maxWidth < 1 ? Integer.MAX_VALUE : maxWidth;
        this.maxDepth = maxDepth < 1 ? Integer.MAX_VALUE : maxDepth;
    }

    /**
     * This method has not been implemented.
     *
     * @param inputStream                       The input stream to be decoded.
     * @param charset                           The character set to use.
     * @return                                  An IData representation of the given input stream data.
     * @throws IOException                      If there is a problem reading from the stream.
     * @throws ServiceException                 If any other error occurs.
     * @throws UnsupportedOperationException    As this method has not been implemented.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException, ServiceException {
        throw new UnsupportedOperationException("decode method not implemented");
    }

    /**
     * Encodes the given IData document as HTML to the given output stream.
     *
     * @param outputStream      The stream to write the encoded IData to.
     * @param document          The IData document to be encoded.
     * @param charset           The character set to use.
     * @throws IOException      If there is a problem writing to the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException, ServiceException {
        InputOutputHelper.copy(InputStreamHelper.normalize(document == null ? HTMLEntity.NULL.toString() : emit(new StringBuilder(), null, document, 0).toString(), charset), outputStream);
    }

    /**
     * Encodes the given IData[] document list as HTML to the given output stream.
     *
     * @param outputStream      The stream to write the encoded IData[] to.
     * @param documentList      The IData[] document list to be encoded.
     * @param charset           The character set to use.
     * @throws IOException      If there is a problem writing to the stream.
     * @throws ServiceException If any other error occurs.
     */
    public void emit(OutputStream outputStream, IData[] documentList, Charset charset) throws IOException, ServiceException {
        InputOutputHelper.copy(InputStreamHelper.normalize(documentList == null ? HTMLEntity.NULL.toString() : emit(new StringBuilder(), null, documentList, 0).toString(), charset), outputStream);
    }

    /**
     * Returns an HTML representation of the given IData object.
     *
     * @param appendable            The Appendable to append the HTML to.
     * @param className             The class name used to style this element.
     * @param input                 The IData to convert to HTML.
     * @param currentDepth          The current width being encoded.
     * @return                      The given Appendable for method chaining convenience.
     * @throws IOException          If an IO error occurs.
     */
    @SuppressWarnings("deprecation")
    protected Appendable emit(Appendable appendable, String className, IData input, int currentDepth) throws IOException {
        if (currentDepth == 0 && !fragment) {
            appendable.append("<html>");
            appendable.append("<head>");
            appendable.append("<style>");
            appendable.append("*{font:11pt/1.2 sans-serif}body{background-color:white;color:black;margin:1em}table{border-collapse:collapse;margin:0}td,th{background-color:white;border:0.375em solid #F0F0F0;font-weight:normal;padding:0.5em;text-align:left;vertical-align:text-top}th{background-color:#F9F9F9;font-weight:bold}");
            appendable.append("</style>");
            appendable.append("</head>");
            appendable.append("<body>");
        }

        if (currentDepth < maxDepth) {
            int size = IDataHelper.size(input);
            if (size == 0) {
                appendable.append(HTMLEntity.EMPTY.toString());
            } else {
                IDataCursor cursor = input.getCursor();
                try {
                    IData[] array = IDataUtil.getIDataArray(cursor, "recordWithNoID");
                    cursor.destroy();
                    if (array != null) {
                        emit(appendable, className, array, currentDepth);
                    } else {
                        cursor = input.getCursor();
                        if (portraitOrientation) {
                            appendable.append("<table class=\"IData IData-Portrait\">");
                            appendable.append("<tbody class=\"IData-Body\">");
                            int i = 0;
                            while (cursor.next()) {
                                if (i < maxLength) {
                                    String key = cursor.getKey();
                                    String keyClassName = toClassName(key);
                                    appendable.append("<tr>");
                                    appendable.append("<th class=\"");
                                    appendable.append(keyClassName);
                                    appendable.append("\">");
                                    appendable.append(HTMLHelper.encode(key));
                                    appendable.append("</th><td class=\"");
                                    appendable.append(keyClassName);
                                    appendable.append("\">");
                                    emit(appendable, keyClassName, cursor.getValue(), currentDepth);
                                    appendable.append("</td></tr>");
                                } else {
                                    appendable.append("<tr><th>");
                                    appendable.append(HTMLEntity.VERTICAL_ELLIPSIS.toString());
                                    appendable.append("</th><td>");
                                    appendable.append(HTMLEntity.VERTICAL_ELLIPSIS.toString());
                                    appendable.append("</td></tr>");
                                    break;
                                }
                                i++;
                            }
                            appendable.append("</tbody></table>");
                        } else {
                            appendable.append("<table class=\"IData IData-Landscape\">");
                            appendable.append("<thead class=\"IData-Head\">");
                            appendable.append("<tr>");
                            List<String> keys = IDataHelper.getKeyList(input);
                            for (int i = 0; i < keys.size(); i++) {
                                if (i < maxWidth) {
                                    String key = keys.get(i);
                                    appendable.append("<th class=\"");
                                    appendable.append(toClassName(key));
                                    appendable.append("\">");
                                    appendable.append(HTMLHelper.encode(key));
                                    appendable.append("</th>");
                                } else {
                                    appendable.append("<th>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</th>");
                                    break;
                                }
                            }
                            appendable.append("</tr>");
                            appendable.append("</thead>");
                            appendable.append("<tbody class=\"IData-Body\">");
                            appendable.append("<tr>");
                            for (int i = 0; i < keys.size(); i++) {
                                if (i < maxWidth) {
                                    String key = keys.get(i);
                                    String keyClassName = toClassName(key);
                                    appendable.append("<td class=\"");
                                    appendable.append(keyClassName);
                                    appendable.append("\">");
                                    emit(appendable, keyClassName, IDataHelper.get(cursor, key), currentDepth);
                                    appendable.append("</td>");
                                } else {
                                    appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                    break;
                                }
                            }
                            appendable.append("</tr>");
                            appendable.append("</tbody>");
                            appendable.append("</table>");
                        }
                    }
                } finally {
                    cursor.destroy();
                }
            }
        } else {
            appendable.append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString());
        }

        if (currentDepth == 0 && !fragment) {
            appendable.append("</body>");
            appendable.append("</html>");
        }

        return appendable;
    }

    /**
     * Converts an IData[] to an HTML string.
     *
     * @param appendable            The Appendable to append the HTML to.
     * @param input                 The IData[] to be converted.
     * @param currentDepth          The current depth being encoded.
     * @return                      The given Appendable for method chaining convenience.
     * @throws IOException          If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, String className, IData[] input, int currentDepth) throws IOException {
        if (currentDepth < maxDepth) {
            if (input.length == 0) {
                appendable.append(HTMLEntity.EMPTY.toString());
            } else {
                String[] keys = IDataHelper.getKeys(input);
                appendable.append("<table class=\"IDataArray");
                if (className != null) {
                    className = className.trim();
                    if (!className.isEmpty()) {
                        appendable.append(" ");
                        appendable.append(className);
                    }
                }
                appendable.append("\">");
                appendable.append("<thead class=\"IDataArray-Head\">");
                appendable.append("<tr>");
                for (int i = 0; i < keys.length; i++) {
                    if (i < maxWidth) {
                        String key = keys[i];
                        appendable.append("<th class=\"").append(toClassName(key)).append("\">").append(HTMLHelper.encode(key)).append("</th>");
                    } else {
                        appendable.append("<th>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</th>");
                        break;
                    }
                }
                appendable.append("</tr>");
                appendable.append("</thead>");
                appendable.append("<tbody class=\"IDataArray-Body\">");
                for (int i = 0; i < input.length; i++) {
                    if (i < maxLength) {
                        IData document = input[i];
                        if (document != null) {
                            IDataCursor cursor = document.getCursor();
                            appendable.append("<tr>");
                            for (int j = 0; j < keys.length; j++) {
                                if (j < maxWidth) {
                                    String key = keys[j];
                                    String keyClassName = toClassName(key);
                                    appendable.append("<td class=\"").append(keyClassName).append("\">");
                                    emit(appendable, keyClassName, IDataHelper.get(cursor, keys[j]), currentDepth);
                                    appendable.append("</td>");
                                } else {
                                    appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                    break;
                                }
                            }
                            appendable.append("</tr>");
                            cursor.destroy();
                        }
                    } else {
                        appendable.append("<tr>");
                        int keyLength = Math.min(keys.length, maxWidth + 1);
                        for (int j = 0; j < keyLength; j++) {
                            appendable.append("<td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td>");
                        }
                        appendable.append("</tr>");
                        break;
                    }
                }
                appendable.append("</tbody>");
                appendable.append("</table>");
            }
        } else {
            appendable.append(HTMLEntity.VERTICAL_ELLIPSIS.toString());
        }

        return appendable;
    }

    /**
     * Converts an Object[][] to an HTML string.
     *
     * @param appendable            The Appendable to append the HTML to.
     * @param input                 The Object[][] to be converted.
     * @param currentDepth          The current depth being encoded.
     * @return                      The given Appendable for method chaining convenience.
     * @throws IOException          If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, String className, Object[][] input, int currentDepth) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            int width = 0;
            for (Object[] row : input) {
                if (row != null) {
                    if (row.length > width) {
                        width = row.length;
                    }
                }
            }

            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    if (input[i] != null) {
                        appendable.append("<tr>");
                        for (int j = 0; j < input[i].length; j++) {
                            if (j < maxWidth) {
                                appendable.append("<td>");
                                emit(appendable, className, input[i][j], currentDepth);
                                appendable.append("</td>");
                            } else {
                                appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                break;
                            }
                        }
                        appendable.append("</tr>");
                    }
                } else {
                    appendable.append("<tr>");
                    for (int j = 0; j < width; j++) {
                        if (j <= maxWidth) {
                            appendable.append("<td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td>");
                        }
                    }
                    appendable.append("</tr>");
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an double[][] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The double[][] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, double[][] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            int width = 0;
            for (double[] row : input) {
                if (row != null) {
                    if (row.length > width) {
                        width = row.length;
                    }
                }
            }

            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    if (input[i] != null) {
                        appendable.append("<tr>");
                        for (int j = 0; j < input[i].length; j++) {
                            if (j < maxWidth) {
                                appendable.append("<td>").append(Double.toString(input[i][j])).append("</td>");
                            } else {
                                appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                break;
                            }
                        }
                        appendable.append("</tr>");
                    }
                } else {
                    appendable.append("<tr>");
                    for (int j = 0; j < width; j++) {
                        if (j <= maxWidth) {
                            appendable.append("<td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td>");
                        }
                    }
                    appendable.append("</tr>");
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an float[][] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The float[][] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, float[][] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            int width = 0;
            for (float[] row : input) {
                if (row != null) {
                    if (row.length > width) {
                        width = row.length;
                    }
                }
            }

            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    if (input[i] != null) {
                        appendable.append("<tr>");
                        for (int j = 0; j < input[i].length; j++) {
                            if (j < maxWidth) {
                                appendable.append("<td>").append(Float.toString(input[i][j])).append("</td>");
                            } else {
                                appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                break;
                            }
                        }
                        appendable.append("</tr>");
                    }
                } else {
                    appendable.append("<tr>");
                    for (int j = 0; j < width; j++) {
                        if (j <= maxWidth) {
                            appendable.append("<td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td>");
                        }
                    }
                    appendable.append("</tr>");
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an long[][] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The long[][] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, long[][] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            int width = 0;
            for (long[] row : input) {
                if (row != null) {
                    if (row.length > width) {
                        width = row.length;
                    }
                }
            }

            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    if (input[i] != null) {
                        appendable.append("<tr>");
                        for (int j = 0; j < input[i].length; j++) {
                            if (j < maxWidth) {
                                appendable.append("<td>").append(Long.toString(input[i][j])).append("</td>");
                            } else {
                                appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                break;
                            }
                        }
                        appendable.append("</tr>");
                    }
                } else {
                    appendable.append("<tr>");
                    for (int j = 0; j < width; j++) {
                        if (j <= maxWidth) {
                            appendable.append("<td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td>");
                        }
                    }
                    appendable.append("</tr>");
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an int[][] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The int[][] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, int[][] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            int width = 0;
            for (int[] row : input) {
                if (row != null) {
                    if (row.length > width) {
                        width = row.length;
                    }
                }
            }

            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    if (input[i] != null) {
                        appendable.append("<tr>");
                        for (int j = 0; j < input[i].length; j++) {
                            if (j < maxWidth) {
                                appendable.append("<td>").append(Integer.toString(input[i][j])).append("</td>");
                            } else {
                                appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                break;
                            }
                        }
                        appendable.append("</tr>");
                    }
                } else {
                    appendable.append("<tr>");
                    for (int j = 0; j < width; j++) {
                        if (j <= maxWidth) {
                            appendable.append("<td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td>");
                        }
                    }
                    appendable.append("</tr>");
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an short[][] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The short[][] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, short[][] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            int width = 0;
            for (short[] row : input) {
                if (row != null) {
                    if (row.length > width) {
                        width = row.length;
                    }
                }
            }

            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    if (input[i] != null) {
                        appendable.append("<tr>");
                        for (int j = 0; j < input[i].length; j++) {
                            if (j < maxWidth) {
                                appendable.append("<td>").append(Short.toString(input[i][j])).append("</td>");
                            } else {
                                appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                break;
                            }
                        }
                        appendable.append("</tr>");
                    }
                } else {
                    appendable.append("<tr>");
                    for (int j = 0; j < width; j++) {
                        if (j <= maxWidth) {
                            appendable.append("<td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td>");
                        }
                    }
                    appendable.append("</tr>");
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an byte[][] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The byte[][] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, byte[][] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            int i = 0;
            for (byte[] array : input) {
                if (i < maxLength) {
                    if (array != null) {
                        appendable.append("<tr><td>").append(StringHelper.truncate(BytesHelper.hexEncode(array), maxValueLength, true)).append("</td></tr>");
                        i++;
                    }
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an char[][] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The char[][] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, char[][] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            int i = 0;
            for (char[] array : input) {
                if (i < maxLength) {
                    if (array != null) {
                        appendable.append("<tr><td>").append(StringHelper.truncate(new String(array), maxValueLength, true)).append("</td></tr>");
                        i++;
                    }
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an boolean[][] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The boolean[][] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, boolean[][] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            int width = 0;
            for (boolean[] row : input) {
                if (row != null) {
                    if (row.length > width) {
                        width = row.length;
                    }
                }
            }

            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    if (input[i] != null) {
                        appendable.append("<tr>");
                        for (int j = 0; j < input[i].length; j++) {
                            if (j < maxWidth) {
                                appendable.append("<td>").append(Boolean.toString(input[i][j])).append("</td>");
                            } else {
                                appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                break;
                            }
                        }
                        appendable.append("</tr>");
                    }
                } else {
                    appendable.append("<tr>");
                    for (int j = 0; j < width; j++) {
                        if (j <= maxWidth) {
                            appendable.append("<td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td>");
                        }
                    }
                    appendable.append("</tr>");
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an Object[] to an HTML string.
     *
     * @param appendable            The Appendable to append the HTML to.
     * @param input                 The Object[] to be converted.
     * @param currentDepth          The current depth being encoded.
     * @return                      The given Appendable for method chaining convenience.
     * @throws IOException          If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, String className, Object[] input, int currentDepth) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    appendable.append("<tr><td>");
                    emit(appendable, className, input[i], currentDepth);
                    appendable.append("</td></tr>");
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                    break;
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an double[] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The double[] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, double[] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    appendable.append("<tr><td>").append(Double.toString(input[i])).append("</td></tr>");
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                    break;
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an float[] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The float[] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, float[] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    appendable.append("<tr><td>").append(Float.toString(input[i])).append("</td></tr>");
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                    break;
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an long[] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The long[] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, long[] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    appendable.append("<tr><td>").append(Long.toString(input[i])).append("</td></tr>");
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                    break;
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an int[] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The int[] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, int[] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    appendable.append("<tr><td>").append(Integer.toString(input[i])).append("</td></tr>");
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                    break;
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an short[] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The short[] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, short[] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    appendable.append("<tr><td>").append(Short.toString(input[i])).append("</td></tr>");
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                    break;
                }
            }
            appendable.append("</tbody></table>");
        }
        return appendable;
    }

    /**
     * Converts an byte[] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The byte[] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, byte[] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append(StringHelper.truncate(BytesHelper.hexEncode(input), maxValueLength, true));
        }
        return appendable;
    }

    /**
     * Converts an char[] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The char[] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, char[] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append(StringHelper.truncate(new String(input), maxValueLength, true));
        }
        return appendable;
    }

    /**
     * Converts an boolean[] to an HTML string.
     *
     * @param appendable    The Appendable to append the HTML to.
     * @param input         The boolean[] to be converted.
     * @return              The given Appendable for method chaining convenience.
     * @throws IOException  If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, boolean[] input) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            for (int i = 0; i < input.length; i++) {
                if (i < maxLength) {
                    appendable.append("<tr><td>").append(Boolean.toString(input[i])).append("</td></tr>");
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                    break;
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an Iterable[] to an HTML string.
     *
     * @param appendable            The Appendable to append the HTML to.
     * @param input                 The Iterable[] to be converted.
     * @param currentDepth          The current width being encoded.
     * @return                      The given Appendable for method chaining convenience.
     * @throws IOException          If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, String className, Iterable[] input, int currentDepth) throws IOException {
        if (input.length == 0) {
            appendable.append(HTMLEntity.EMPTY.toString());
        } else {
            appendable.append("<table><tbody>");
            int i = 0;
            for (Iterable iterable : input) {
                if (i < maxLength) {
                    if (iterable != null) {
                        int j = 0;
                        appendable.append("<tr>");
                        for (Object value : iterable) {
                            if (j < maxWidth) {
                                appendable.append("<td>");
                                emit(appendable, className, value, currentDepth);
                                appendable.append("</td>");
                            } else {
                                appendable.append("<td>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</td>");
                                break;
                            }
                            j++;
                        }
                        appendable.append("</tr>");
                        i++;
                    }
                } else {
                    appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                    break;
                }
            }
            appendable.append("</tbody></table>");
        }

        return appendable;
    }

    /**
     * Converts an Iterable to an HTML string.
     *
     * @param appendable            The Appendable to append the HTML to.
     * @param input                 The Iterable to be converted.
     * @param currentDepth          The current width being encoded.
     * @return                      The given Appendable for method chaining convenience.
     * @throws IOException          If an IO error occurs.
     */
    protected Appendable emit(Appendable appendable, String className, Iterable input, int currentDepth) throws IOException {
        Set<String> keys = new LinkedHashSet<String>();
        boolean isIDataCollection = false;
        for (Object value : input) {
            isIDataCollection = value instanceof IData;
            if (isIDataCollection) {
                keys.addAll(IDataHelper.getKeyList((IData)value));
            } else {
                break;
            }
        }

        if (isIDataCollection) {
            int i = 0;
            for (Object value : input) {
                if (value instanceof IData) {
                    IDataCursor cursor = ((IData)value).getCursor();
                    try {
                        if (i == 0) {
                            appendable.append("<table class=\"IDataArray\"><thead class=\"IDataArray-Head\"><tr>");
                            int j = 0;
                            for (String key : keys) {
                                if (j < maxWidth) {
                                    appendable.append("<th>").append(HTMLHelper.encode(key)).append("</th>");
                                } else {
                                    appendable.append("<th>").append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString()).append("</th>");
                                }
                                j++;
                            }
                            appendable.append("</tr></thead><tbody class=\"IDataArray-Body\">");
                        }
                        if (i < maxLength) {
                            appendable.append("<tr>");
                            for (String key : keys) {
                                appendable.append("<td>");
                                emit(appendable, className, IDataHelper.get(cursor, key), currentDepth);
                                appendable.append("</td>");
                            }
                            appendable.append("</tr>");
                        } else {
                            appendable.append("<tr>");
                            int j = 0;
                            for (String key : keys) {
                                if (j <= maxWidth) {
                                    appendable.append("<td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td>");
                                }
                                j++;
                            }
                            appendable.append("</tr>");
                            break;
                        }
                        i++;
                    } finally {
                        cursor.destroy();
                    }
                }
            }

            if (i == 0) {
                appendable.append(HTMLEntity.EMPTY.toString());
            } else {
                appendable.append("</tbody></table>");
            }
        } else {
            int i = 0;
            for (Object value : input) {
                if (value != null) {
                    if (i == 0) {
                        appendable.append("<table><tbody>");
                    }
                    if (i < maxLength) {
                        appendable.append("<tr><td>");
                        emit(appendable, className, value, currentDepth);
                        appendable.append("</td></tr>");
                    } else {
                        appendable.append("<tr><td>").append(HTMLEntity.VERTICAL_ELLIPSIS.toString()).append("</td></tr>");
                        break;
                    }
                    i++;
                }
            }
            if (i == 0) {
                appendable.append(HTMLEntity.EMPTY.toString());
            } else {
                appendable.append("</tbody></table>");
            }
        }

        return appendable;
    }

    /**
     * Converts an Object to an HTML string.
     *
     * @param appendable            The Appendable to append the HTML to.
     * @param input                 The Iterable to be converted.
     * @param currentDepth          The current width being encoded.
     * @return                      The given Appendable for method chaining convenience.
     * @throws IOException          If an IO error occurs.
     */
    @SuppressWarnings("deprecation")
    protected Appendable emit(Appendable appendable, String className, Object input, int currentDepth) throws IOException {
        if (currentDepth < maxDepth) {
            if (input == null) {
                appendable.append(HTMLEntity.NULL.toString());
            } else if (input instanceof MBoolean[][]) {
                emit(appendable, className, (Object[][])input, currentDepth + 1);
            } else if (input instanceof MBoolean[]) {
                emit(appendable, className, (Object[])input, currentDepth + 1);
            } else if (input instanceof MBoolean) {
                appendable.append(HTMLHelper.encode(input.toString()));
            } else if (input instanceof IData[] || input instanceof Table || input instanceof IDataCodable[] || input instanceof IDataPortable[] || input instanceof ValuesCodable[]) {
                emit(appendable, className, IDataHelper.toIDataArray(input), currentDepth + 1);
            } else if (input instanceof IData || input instanceof IDataCodable || input instanceof IDataPortable || input instanceof ValuesCodable) {
                emit(appendable, className, IDataHelper.toIData(input), currentDepth + 1);
            } else if (input instanceof Iterable[]) {
                emit(appendable, className, (Iterable[])input, currentDepth + 1);
            } else if (input instanceof Iterable) {
                emit(appendable, className, (Iterable)input,currentDepth + 1);
            } else if (input instanceof Object[][]) {
                emit(appendable, className, (Object[][])input, currentDepth + 1);
            } else if (input instanceof double[][]) {
                emit(appendable, (double[][])input);
            } else if (input instanceof float[][]) {
                emit(appendable, (float[][])input);
            } else if (input instanceof long[][]) {
                emit(appendable, (long[][])input);
            } else if (input instanceof int[][]) {
                emit(appendable, (int[][])input);
            } else if (input instanceof short[][]) {
                emit(appendable, (short[][])input);
            } else if (input instanceof byte[][]) {
                emit(appendable, (byte[][])input);
            } else if (input instanceof char[][]) {
                emit(appendable, (char[][])input);
            } else if (input instanceof boolean[][]) {
                emit(appendable, (boolean[][])input);
            } else if (input instanceof Object[]) {
                emit(appendable, className, (Object[])input, currentDepth + 1);
            } else if (input instanceof double[]) {
                emit(appendable, (double[])input);
            } else if (input instanceof float[]) {
                emit(appendable, (float[])input);
            } else if (input instanceof long[]) {
                emit(appendable, (long[])input);
            } else if (input instanceof int[]) {
                emit(appendable, (int[])input);
            } else if (input instanceof short[]) {
                emit(appendable, (short[])input);
            } else if (input instanceof byte[]) {
                emit(appendable, (byte[])input);
            } else if (input instanceof char[]) {
                emit(appendable, (char[])input);
            } else if (input instanceof boolean[]) {
                emit(appendable, (boolean[])input);
            } else {
                appendable.append(StringHelper.truncate(HTMLHelper.encode(ObjectHelper.stringify(input)), maxValueLength, true));
            }
        } else {
            appendable.append(HTMLEntity.HORIZONTAL_ELLIPSIS.toString());
        }

        return appendable;
    }

    /**
     * Returns a new class name constructed from the given key.
     *
     * @param key           The key to be converted to an HTML class name.
     * @return              The HTML class name to use for the given key.
     */
    private static String toClassName(String key) {
        if (key == null) throw new NullPointerException("key must not be null");

        String className = sanitizeClassName(key);

        try {
            if (className == null || className.trim().isEmpty()) {
                className = BytesHelper.hexEncode(MessageDigestHelper.digest(MessageDigestHelper.normalize("SHA-1"), key, CharsetHelper.DEFAULT_CHARSET));
            }
        } catch(NoSuchAlgorithmException ex) {
            ExceptionHelper.raiseUnchecked(ex);
        }

        return "IDataKey-" + className;
    }

    /**
     * Sanitizes the given class name, removing all illegal characters.
     *
     * @param className The class name to sanitize.
     * @return          The sanitized class name.
     */
    private static String sanitizeClassName(String className) {
        if (className != null) {
            className = className.replaceAll("[^\\w\\-]", "");
        }
        return className;
    }
}
