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
import permafrost.tundra.lang.Properties;
import permafrost.tundra.mime.MIMETypeHelper;
import javax.activation.MimeType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A generic class for holding arbitrary binary data.
 */
public class Content implements Properties<String, Object> {
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
     * Optional document providing context for this content.
     */
    protected IData context;

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
    public Content(byte[] data, MimeType type) {
        this.data = data == null ? new byte[0] : data;
        this.type = MIMETypeHelper.normalize(type);
        this.length = this.data.length;
    }

    /**
     * Create a new Content object to hold arbitrary binary data.
     *
     * @param data                      Arbitrary binary data.
     * @param type                      The MIME media type of the data.
     * @param context                   The context for this content.
     */
    public Content(byte[] data, MimeType type, IData context) {
        this.data = data == null ? new byte[0] : data;
        this.type = MIMETypeHelper.normalize(type);
        this.length = this.data.length;
        this.context = context;
    }

    /**
     * Returns the context for this content.
     *
     * @return the context for this content.
     */
    public IData getContext() {
        return context;
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
     * Returns the properties of this object.
     *
     * @return the properties of this object
     */
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();

        byte[] content = getData();
        properties.put("$content", content);
        properties.put("$content.type", getType().toString());
        properties.put("$content.length", Integer.toString(length));
        if (context != null) properties.put("$content.context", context);

        return properties;
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
