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

package permafrost.tundra.zip;

import com.wm.data.IData;
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.io.StreamHelper;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A wrapper class for arbitrary binary content.
 */
public class NamedContent implements IDataCodable {
    protected String name;
    protected InputStream data;

    /**
     * Constructs a new NamedContent object.
     * @param name The name associated with the content.
     * @param data The data associated with the content.
     */
    public NamedContent(String name, InputStream data) {
        this.name = name;
        this.data = data;
    }

    /**
     * Constructs a new NamedContent object.
     * @param name The name associated with the content.
     * @param data The data associated with the content.
     */
    public NamedContent(String name, byte[] data) {
        this(name, StreamHelper.normalize(data));
    }

    /**
     * Constructs a new NamedContent object.
     * @param name          The name associated with the content.
     * @param data          The data associated with the content.
     * @param charsetName   The character set used to encode the string as binary data.
     */
    public NamedContent(String name, String data, String charsetName) {
        this(name, StreamHelper.normalize(data, charsetName));
    }

    /**
     * Constructs a new NamedContent object.
     * @param name          The name associated with the content.
     * @param data          The data associated with the content.
     * @param charset       The character set used to encode the string as binary data.
     */
    public NamedContent(String name, String data, Charset charset) {
        this(name, StreamHelper.normalize(data, charset));
    }

    /**
     * Constructs a new NamedContent object.
     * @param name          The name associated with the content.
     * @param data          The data associated with the content.
     */
    public NamedContent(String name, String data) {
        this(name, StreamHelper.normalize(data));
    }

    /**
     * Returns the name associated with this content.
     * @return The name associated with this content.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the data associated with this content.
     * @return The data associated with this content.
     */
    public InputStream getData() {
        return data;
    }

    /**
     * Returns an IData representation of this NamedContent object.
     * @return An IData representation of this NamedContent object.
     */
    public IData getIData() {
        IDataMap map = new IDataMap();
        map.put("name", name);
        map.put("content", data);
        return map;
    }

    /**
     * Initializes the values of this NamedContent object from the given IData document.
     * @param document The IData document to initialize this NamedContent object from;
     *                 must include the following keys: name, encoding, content.
     */
    public void setIData(IData document) {
        NamedContent content = valueOf(document);
        this.name = content.getName();
        this.data = content.getData();
    }

    /**
     * Returns a NamedContent object given an IData document.
     * @param document The IData document to construct the Content object from;
     *                 must include the following keys: name, encoding, content.
     * @return         The NamedContent object representing the given IData document.
     */
    public static NamedContent valueOf(IData document) {
        if (document == null) return null;
        IDataMap map = new IDataMap(document);
        return new NamedContent((String)map.get("name"), StreamHelper.normalize(map.get("content"), (String)map.get("encoding")));
    }

    /**
     * Returns a NamedContent[] given an IData[].
     * @param array    The IData[] to construct the NamedContent object from; each IData
     *                 must include the following keys: name, encoding, content.
     * @return         The NamedContent[] representing the given IData[].
     */
    public static NamedContent[] valueOf(IData[] array) {
        if (array == null) return null;

        NamedContent[] contents = new NamedContent[array.length];
        for (int i = 0; i < array.length; i++) {
            contents[i] = valueOf(array[i]);
        }
        return contents;
    }

    /**
     * Returns an IData[] given a NamedContent[].
     * @param contents The NamedContent[] to be converted to an IData[].
     * @return         The IData[] representing the given NamedContent[].
     */
    public static IData[] toIDataArray(NamedContent[] contents) {
        if (contents == null) return null;

        IData[] array = new IData[contents.length];
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) array[i] = contents[i].getIData();
        }

        return array;
    }
}
