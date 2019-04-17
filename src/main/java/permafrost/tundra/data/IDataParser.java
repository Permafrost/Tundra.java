/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Lachlan Dowding
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
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.StringHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Provides a base implementation of an IDataParser class, and a bridge to an IDataCoder class.
 */
public abstract class IDataParser extends IDataCoder {
    /**
     * The content type that this parser handles.
     */
    protected String contentType;

    /**
     * Construct a new IDataParser.
     *
     * @param contentType   The content type this parser handles.
     */
    public IDataParser(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Parses the given String as an IData document.
     *
     * @param string        The String to be parsed.
     * @return              The parsed String as an IData document.
     * @throws IOException  If an IO error occurs.
     */
    public final IData parse(String string) throws IOException {
        return parse(InputStreamHelper.normalize(string));
    }

    /**
     * Parses the data in the given input stream, returning an IData representation.
     *
     * @param inputStream   The input stream to be parsed.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    public IData parse(InputStream inputStream) throws IOException {
        return parse(inputStream, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Parses the data in the given input stream, returning an IData representation.
     *
     * @param inputStream   The input stream to be parsed.
     * @param charset       The character set to use when decoding the data in the input stream.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    public abstract IData parse(InputStream inputStream, Charset charset) throws IOException;

    /**
     * Serializes the given IData document.
     *
     * @param outputStream  The output stream the serialized IData is written to.
     * @param document      The IData document to be serialized.
     * @throws IOException  If an I/O error occurs.
     */
    public void emit(OutputStream outputStream, IData document) throws IOException {
        emit(outputStream, document, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Serializes the given IData document.
     *
     * @param outputStream  The output stream the serialized IData is written to.
     * @param document      The IData document to be serialized.
     * @param charset       The character set to use when serializing the IData document.
     * @throws IOException  If an I/O error occurs.
     */
    public abstract void emit(OutputStream outputStream, IData document, Charset charset) throws IOException;

    /**
     * Serializes the given IData document.
     *
     * @param appendable    The appendable the serialized IData is written to.
     * @param document      The IData document to be serialized.
     * @throws IOException  If an I/O error occurs.
     */
    public final void emit(Appendable appendable, IData document) throws IOException {
        appendable.append(StringHelper.normalize(emit(document)));
    }

    /**
     * Serializes the given IData document.
     *
     * @param document      The IData document to be serialized.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    public final InputStream emit(IData document) throws IOException {
        return emit(document, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Serializes the given IData document.
     *
     * @param document      The IData document to be serialized.
     * @param charset       The character set to use when serializing the IData document.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    public final InputStream emit(IData document, Charset charset) throws IOException {
        return emit(document, charset, InputStream.class);
    }

    /**
     * Serializes the given IData document.
     *
     * @param document      The IData document to be serialized.
     * @param returnClass   Whether to return a String, byte[], or InputStream.
     * @param <T>           The desired class of object to be returned.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    public final <T> T emit(IData document, Class<T> returnClass) throws IOException {
        return emit(document, CharsetHelper.DEFAULT_CHARSET, returnClass);
    }

    /**
     * Serializes the given IData document.
     *
     * @param document      The IData document to be serialized.
     * @param charset       The character set to use when serializing the IData document.
     * @param returnClass   Whether to return a String, byte[], or InputStream.
     * @param <T>           The desired class of object to be returned.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    @SuppressWarnings("unchecked")
    public final <T> T emit(IData document, Charset charset, Class<T> returnClass) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        emit(outputStream, document, charset);
        byte[] bytes = outputStream.toByteArray();

        T output;

        if (returnClass.isAssignableFrom(InputStream.class)) {
            output = (T) InputStreamHelper.normalize(bytes);
        } else if (returnClass.equals(byte[].class)) {
            output = (T) bytes;
        } else if (returnClass.isAssignableFrom(String.class)) {
            output = (T)StringHelper.normalize(bytes);
        } else {
            throw new IllegalArgumentException("Unsupported return class specified: " + returnClass.getName());
        }

        return output;
    }

    /**
     * Decodes the given input stream data as an IData.
     *
     * @param inputStream   The stream to read the data to be decoded from.
     * @return              The IData representation of the stream data.
     * @throws IOException  If there is a problem reading from the stream.
     */
    @Override
    public final IData decode(InputStream inputStream) throws IOException {
        return parse(inputStream);
    }

    /**
     * Encodes the given IData document as a string.
     *
     * @param outputStream The stream to write the encoded IData to.
     * @param document     The IData document to be encoded.
     * @throws IOException If there is a problem writing to the stream.
     */
    @Override
    public final void encode(OutputStream outputStream, IData document) throws IOException {
        emit(outputStream, document);
    }

    /**
     * Encodes the given IData document as a string.
     *
     * @param document      The IData document to be encoded.
     * @return              A byte[] representation of the resulting string.
     * @throws IOException  If there is a problem writing to the stream.
     */
    @Override
    public final byte[] encodeToBytes(IData document) throws IOException {
        return BytesHelper.normalize(emit(document));
    }

    /**
     * Returns the MIME type this parser handles.
     *
     * @return The MIME type this parser handles.
     */
    @Override
    public String getContentType() {
        return contentType;
    }
}
