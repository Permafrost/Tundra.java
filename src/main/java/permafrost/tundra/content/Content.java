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

package permafrost.tundra.content;

import com.wm.data.IData;
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.mime.MIMETypeHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.activation.MimeType;

/**
 * A generic class for holding arbitrary binary data.
 */
public class Content implements IDataCodable {
    /**
     * The arbitrary binary data held by this object.
     */
    protected byte[] data;
    /**
     * The MIME media type of the data held by this object.
     */
    protected MimeType type;
    /**
     * The size of the content in bytes.
     */
    protected int length;

    /**
     * Create a new Content object to hold arbitrary binary data.
     **/
    public Content() {
        this(null);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     */
    public Content(byte[] data) {
        this(data, MIMETypeHelper.getDefault());
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     */
    public Content(byte[] data, String type) {
        this(data, MIMETypeHelper.of(type));
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     */
    public Content(byte[] data, MimeType type) {
        this.data = data == null ? new byte[0] : data;
        this.type = MIMETypeHelper.normalize(type);
        this.length = this.data.length;
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @throws IOException              If an I/O error occurs reading the data.
     */
    public Content(InputStream data, String type) throws IOException {
        this(data, MIMETypeHelper.of(type));
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @throws IOException              If an I/O error occurs reading the data.
     */
    public Content(InputStream data, MimeType type) throws IOException {
        this(BytesHelper.normalize(data), type);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     */
    public Content(String data, String type) {
        this(data, type, null);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @param  charsetName              The name of the charset used to encode the string data.
     */
    public Content(String data, String type, String charsetName) {
        this(data, MIMETypeHelper.of(type), charsetName);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     */
    public Content(String data, MimeType type) {
        this(data, type, MIMETypeHelper.normalize(type).getParameter("charset"));
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @param  charsetName              The name of the charset used to encode the string data.
     */
    public Content(String data, MimeType type, String charsetName) {
        this(data, type, CharsetHelper.normalize(charsetName));
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @param  charset                  The charset used to encode the string data.
     */
    public Content(String data, MimeType type, Charset charset) {
        this(BytesHelper.normalize(data, charset), type);
        this.type.setParameter("charset", CharsetHelper.normalize(charset).displayName());
    }

    /**
     * Returns the arbitrary binary data held by this object.
     *
     * @return The arbitrary binary data held by this object.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the MIME media type of the data held by this object.
     *
     * @return The MIME media type of the data held by this object.
     */
    public MimeType getType() {
        return type;
    }

    /**
     * Returns the size in bytes of this content.
     *
     * @return The size in bytes of this content.
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns true if the content is empty.
     *
     * @return True if the content is empty.
     */
    public boolean isEmpty() {
        return data.length == 0;
    }

    /**
     * Returns an IData representation of this object.
     *
     * @return An IData representation of this object.
     */
    public IData getIData() {
        IDataMap map = new IDataMap();

        byte[] content = getData();
        map.put("$content", content);
        map.put("$content.type", getType().toString());
        map.put("$content.length", Integer.toString(length));

        return map;
    }

    /**
     * This method has not been implemented.
     *
     * @param  document                         An IData document.
     * @throws UnsupportedOperationException    This method has not been implemented.
     */
    public void setIData(IData document) {
        throw new UnsupportedOperationException("setIData(IData) not implemented");
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @return                          The new Content object.
     */
    public static Content of(byte[] data, String type) {
        return new Content(data, type);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @return                          The new Content object.
     */
    public static Content of(byte[] data, MimeType type) {
        return new Content(data, type);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @return                          The new Content object.
     * @throws IOException              If an IO error occurs.
     */
    public static Content of(InputStream data, String type) throws IOException {
        return new Content(data, type);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @return                          The new Content object.
     * @throws IOException              If an IO error occurs.
     */
    public static Content of(InputStream data, MimeType type) throws IOException {
        return new Content(data, type);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @return                          The new Content object.
     */
    public static Content of(String data, String type) {
        return new Content(data, type);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @return                          The new Content object.
     */
    public static Content of(String data, MimeType type) {
        return new Content(data, type);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @param  charsetName              The name of the charset used to encode the string data.
     * @return                          The new Content object.
     */
    public static Content of(String data, String type, String charsetName) {
        return new Content(data, type, charsetName);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @param  charsetName              The name of the charset used to encode the string data.
     * @return                          The new Content object.
     */
    public static Content of(String data, MimeType type, String charsetName) {
        return new Content(data, type, charsetName);
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param  data                     Arbitrary binary data.
     * @param  type                     The MIME media type of the data.
     * @param  charset                  The charset used to encode the string data.
     * @return                          The new Content object.
     */
    public static Content of(String data, MimeType type, Charset charset) {
        return new Content(data, type, charset);
    }

    /**
     * Returns the given content if not null, otherwise a new empty content is returned.
     * @param  content  The content to be normalized.
     * @return          The given content if not null, otherwise a new emtpy content.
     */
    public static Content normalize(Content content) {
        if (content == null) content = new Content();
        return content;
    }
}
