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
import permafrost.tundra.lang.ByteHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.ObjectHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;

/**
 * A wrapper class for arbitrary binary content.
 */
public class ZipEntryWithData extends ZipEntry implements IDataCodable {
    protected byte[] data;

    /**
     * Constructs a new ZipEntryWithData object.
     * @param name          The name associated with the content.
     * @param data          The data associated with the content.
     * @throws IOException  If an I/O problem occurs reading from the stream.
     */
    public ZipEntryWithData(String name, InputStream data) throws IOException {
        this(name, ByteHelper.normalize(data));
    }

    /**
     * Constructs a new ZipEntryWithData object.
     * @param name The name associated with the content.
     * @param data The data associated with the content.
     */
    public ZipEntryWithData(String name, byte[] data) {
        super(name);
        this.data = data;
    }

    /**
     * Constructs a new ZipEntryWithData object.
     * @param name          The name associated with the content.
     * @param data          The data associated with the content.
     * @param charsetName   The character set used to encode the string as binary data.
     */
    public ZipEntryWithData(String name, String data, String charsetName) {
        this(name, ByteHelper.normalize(data, charsetName));
    }

    /**
     * Constructs a new ZipEntryWithData object.
     * @param name          The name associated with the content.
     * @param data          The data associated with the content.
     * @param charset       The character set used to encode the string as binary data.
     */
    public ZipEntryWithData(String name, String data, Charset charset) {
        this(name, ByteHelper.normalize(data, charset));
    }

    /**
     * Constructs a new ZipEntryWithData object.
     * @param name          The name associated with the content.
     * @param data          The data associated with the content.
     */
    public ZipEntryWithData(String name, String data) {
        this(name, ByteHelper.normalize(data));
    }

    /**
     * Returns the data associated with this content.
     * @return The data associated with this content.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns an IData representation of this object.
     * @param mode      Determines the type of object the data is returned as.
     * @param charset   The character set to use when mode is STRING.
     * @return          An IData representation of this object.
     */
    public IData getIData(ObjectHelper.ConvertMode mode, Charset charset) {
        if (mode == null) mode = ObjectHelper.ConvertMode.STREAM;

        IDataMap map = new IDataMap();
        map.put("name", getName());

        Object outputData;
        try {
            outputData = ObjectHelper.convert(getData(), charset, mode);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        map.put("content", outputData);
        return map;
    }

    /**
     * Returns an IData representation of this object.
     * @param mode        Determines the type of object the data is returned as.
     * @param charsetName The character set to use when mode is STRING.
     * @return            An IData representation of this object.
     */
    public IData getIData(ObjectHelper.ConvertMode mode, String charsetName) {
        return getIData(mode, CharsetHelper.normalize(charsetName));
    }

    /**
     * Returns an IData representation of this object.
     * @param mode      Determines the type of object the data is returned as.
     * @return An IData representation of this object.
     */
    public IData getIData(ObjectHelper.ConvertMode mode) {
        return getIData(mode, (Charset)null);
    }

    /**
     * Returns an IData representation of this object.
     * @return An IData representation of this object.
     */
    @Override
    public IData getIData() {
        return getIData(null);
    }

    /**
     * Not implemented: initializes the values of this object from the given IData document.
     * @param document The IData document to initialize this object from;
     *                 must include the following keys: name, encoding, content.
     */
    @Override
    public void setIData(IData document) {
        throw new UnsupportedOperationException("setIData method not implemented");
    }

    /**
     * Returns a new ZipEntryWithData object given an IData document.
     * @param document      The IData document to construct the ZipEntryWithData object from;
     *                      must include the following keys: name, encoding, content.
     * @return              The ZipEntryWithData object representing the given IData document.
     * @throws IOException  If an I/O problem occurs reading from a stream.
     */
    public static ZipEntryWithData valueOf(IData document) throws IOException {
        if (document == null) return null;
        IDataMap map = new IDataMap(document);
        return new ZipEntryWithData((String)map.get("name"), StreamHelper.normalize(map.get("content"), (String)map.get("encoding")));
    }

    /**
     * Returns a ZipEntryWithData[] given an IData[].
     * @param array         The IData[] to construct the ZipEntryWithData object from; each IData
     *                      must include the following keys: name, encoding, content.
     * @return              The ZipEntryWithData[] representing the given IData[].
     * @throws IOException  If an I/O problem occurs reading from a stream.
     */
    public static ZipEntryWithData[] valueOf(IData[] array) throws IOException {
        if (array == null) return null;

        ZipEntryWithData[] contents = new ZipEntryWithData[array.length];
        for (int i = 0; i < array.length; i++) {
            contents[i] = valueOf(array[i]);
        }
        return contents;
    }

    /**
     * Returns an IData[] given a ZipEntryWithData[].
     * @param contents The ZipEntryWithData[] to be converted to an IData[].
     * @param mode     Determines the type of object the data is returned as.
     * @param charset  The character set to use when mode is STRING.
     * @return         The IData[] representing the given ZipEntryWithData[].
     */
    public static IData[] toIDataArray(ZipEntryWithData[] contents, ObjectHelper.ConvertMode mode, Charset charset) {
        if (contents == null) return null;

        IData[] array = new IData[contents.length];
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                array[i] = contents[i].getIData(mode, charset);
            }
        }

        return array;
    }

    /**
     * Returns an IData[] given a ZipEntryWithData[].
     * @param contents      The ZipEntryWithData[] to be converted to an IData[].
     * @param mode          Determines the type of object the data is returned as.
     * @param charsetName   The character set to use when mode is STRING.
     * @return              The IData[] representing the given ZipEntryWithData[].
     */
    public static IData[] toIDataArray(ZipEntryWithData[] contents, ObjectHelper.ConvertMode mode, String charsetName) {
        return toIDataArray(contents, mode, CharsetHelper.normalize(charsetName));
    }

    /**
     * Returns an IData[] given a ZipEntryWithData[].
     * @param contents      The ZipEntryWithData[] to be converted to an IData[].
     * @param mode          Determines the type of object the data is returned as.
     * @return              The IData[] representing the given ZipEntryWithData[].
     */
    public static IData[] toIDataArray(ZipEntryWithData[] contents, ObjectHelper.ConvertMode mode) {
        return toIDataArray(contents, mode, (Charset)null);
    }

    /**
     * Returns an IData[] given a ZipEntryWithData[].
     * @param contents The ZipEntryWithData[] to be converted to an IData[].
     * @return         The IData[] representing the given ZipEntryWithData[].
     */
    public static IData[] toIDataArray(ZipEntryWithData[] contents) {
        return toIDataArray(contents, (null));
    }
}
