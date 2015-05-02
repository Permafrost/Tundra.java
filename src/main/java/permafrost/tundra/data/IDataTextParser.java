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
import com.wm.util.coder.IDataCoder;
import permafrost.tundra.lang.CharsetHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Serializes and deserializes IData documents to and from a text representation.
 */
public abstract class IDataTextParser extends IDataCoder implements IDataParser {
    /**
     * Encodes the given IData document as a string.
     *
     * @param outputStream  The stream to write the encoded IData to.
     * @param document      The IData document to be encoded.
     * @param charset       The character set to use.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public abstract void encode(OutputStream outputStream, IData document, Charset charset) throws IOException;

    /**
     * Decodes the given input stream data as an IData.
     *
     * @param inputStream   The stream to read the data to be decoded from.
     * @param charset       The character set to use.
     * @return              The IData representation of the stream data.
     * @throws IOException  If there is a problem reading from the stream.
     */
    public abstract IData decode(InputStream inputStream, Charset charset) throws IOException;

    /**
     * Encodes the given IData document as a string.
     *
     * @param outputStream  The stream to write the encoded IData to.
     * @param document      The IData document to be encoded.
     * @param charsetName   The character set to use.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public void encode(OutputStream outputStream, IData document, String charsetName) throws IOException {
        encode(outputStream, document, CharsetHelper.normalize(charsetName));
    }

    /**
     * Encodes the given IData document as a string.
     *
     * @param outputStream  The stream to write the encoded IData to.
     * @param document      The IData document to be encoded.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public void encode(OutputStream outputStream, IData document) throws IOException {
        encode(outputStream, document, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Encodes the given IData document as a string.
     *
     * @param document      The IData document to be encoded.
     * @param charset       The character set to use.
     * @return              A byte[] representation of the resulting string.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public byte[] encodeToBytes(IData document, Charset charset) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        this.encode(outputStream, document, CharsetHelper.normalize(charset));
        return outputStream.toByteArray();
    }

    /**
     * Encodes the given IData document as a string.
     *
     * @param document      The IData document to be encoded.
     * @param charsetName   The character set to use.
     * @return              A byte[] representation of the resulting string.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public byte[] encodeToBytes(IData document, String charsetName) throws IOException {
        return encodeToBytes(document, CharsetHelper.normalize(charsetName));
    }

    /**
     * Encodes the given IData document as a string.
     *
     * @param document      The IData document to be encoded.
     * @return              A byte[] representation of the resulting string.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public byte[] encodeToBytes(IData document) throws IOException {
        return encodeToBytes(document, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Encodes the given IData document as a string.
     *
     * @param document      The IData document to be encoded.
     * @return              A string representation of the IData.
     * @throws IOException  If there is an I/O problem.
     */
    public String encodeToString(IData document) throws IOException {
        return new String(encodeToBytes(document, CharsetHelper.DEFAULT_CHARSET), CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Decodes the given input stream data as an IData.
     *
     * @param inputStream   The stream to read the data to be decoded from.
     * @param charsetName   The character set to use.
     * @return              The IData representation of the stream data.
     * @throws IOException  If there is a problem reading from the stream.
     */
    public IData decode(InputStream inputStream, String charsetName) throws IOException {
        return decode(inputStream, CharsetHelper.normalize(charsetName));
    }

    /**
     * Decodes the given input stream data as an IData.
     *
     * @param inputStream   The stream to read the data to be decoded from.
     * @return              The IData representation of the stream data.
     * @throws IOException  If there is a problem reading from the stream.
     */
    public IData decode(InputStream inputStream) throws IOException {
        return decode(inputStream, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Decodes the given byte data as an IData.
     *
     * @param bytes         The byte[] to read the data to be decoded from.
     * @param charset       The character set to use.
     * @return              The IData representation of the byte data.
     * @throws IOException  If there is a problem reading from the stream.
     */
    public IData decodeFromBytes(byte[] bytes, Charset charset) throws IOException {
        return decode(new ByteArrayInputStream(bytes), CharsetHelper.normalize(charset));
    }

    /**
     * Decodes the given byte data as an IData.
     *
     * @param bytes         The byte[] to read the data to be decoded from.
     * @param charsetName   The character set to use.
     * @return              The IData representation of the byte data.
     * @throws IOException  If there is a problem reading from the stream.
     */
    public IData decodeFromBytes(byte[] bytes, String charsetName) throws IOException {
        return decodeFromBytes(bytes, CharsetHelper.normalize(charsetName));
    }

    /**
     * Decodes the given byte data as an IData.
     *
     * @param bytes         The byte[] to read the data to be decoded from.
     * @return              The IData representation of the byte data.
     * @throws IOException  If there is a problem reading from the stream.
     */
    public IData decodeFromBytes(byte[] bytes) throws IOException {
        return decodeFromBytes(bytes, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Decodes the given String as an IData.
     *
     * @param string        The String to be decoded.
     * @return              The IData representation of the String.
     * @throws IOException  If there is an I/O problem.
     */
    public IData decodeFromString(String string) throws IOException {
        return decodeFromBytes(string.getBytes(CharsetHelper.DEFAULT_CHARSET), CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Parses the data in the given input stream, returning an IData representation.
     *
     * @param inputStream   The input stream to be parsed.
     * @param charset       The character set to use when decoding the data in the input stream.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    public IData parse(InputStream inputStream, Charset charset) throws IOException {
        return decode(inputStream, charset);
    }

    /**
     * Parses the data in the given input stream, returning an IData representation.
     *
     * @param inputStream   The input stream to be parsed.
     * @param charsetName   The character set to use when decoding the data in the input stream.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    public IData parse(InputStream inputStream, String charsetName) throws IOException {
        return decode(inputStream, CharsetHelper.normalize(charsetName));
    }

    /**
     * Parses the data in the given input stream, returning an IData representation.
     *
     * @param inputStream   The input stream to be parsed.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    public IData parse(InputStream inputStream) throws IOException {
        return decode(inputStream, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Serializes the given IData document.
     *
     * @param document      The IData document to be serialized.
     * @param charset       The character set to use when serializing the IData document.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    public InputStream emit(IData document, Charset charset) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encode(outputStream, document, charset);
        byte[] bytes = outputStream.toByteArray();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Serializes the given IData document.
     *
     * @param document      The IData document to be serialized.
     * @param charsetName   The character set to use when serializing the IData document.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    public InputStream emit(IData document, String charsetName) throws IOException {
        return emit(document, CharsetHelper.normalize(charsetName));
    }

    /**
     * Serializes the given IData document.
     *
     * @param document      The IData document to be serialized.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    public InputStream emit(IData document) throws IOException {
        return emit(document, CharsetHelper.DEFAULT_CHARSET);
    }
}
