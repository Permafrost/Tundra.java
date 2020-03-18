/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
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
import com.wm.data.IDataFactory;
import permafrost.tundra.io.CloseableHelper;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.ObjectHelper;
import permafrost.tundra.lang.StringHelper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Serializes IData objects from and to URL encoded HTML form data.
 */
public class IDataHTMLFormParser extends IDataParser {
    /**
     * The character set used when URL encoding and decoding names and values.
     */
    private static final String URL_ENCODING_CHARSET = "UTF-8";

    /**
     * Constructs a new IDataFormEncodedParser object which parses and emits application/x-www-form-urlencoded data.
     */
    public IDataHTMLFormParser() {
        super("application/x-www-form-urlencoded");
    }

    /**
     * Parses the data in the given input stream, returning an IData representation.
     *
     * @param inputStream   The input stream to be parsed.
     * @param charset       The character set to use when decoding the data in the input stream.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException {
        String[] items = StringHelper.split(StringHelper.normalize(inputStream, charset), "&", true);

        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();

        try {
            for (String item : items) {
                String[] tuple = StringHelper.split(item, "=", true);
                String name = null, value = null;
                if (tuple.length > 0 && tuple[0] != null) {
                    name = URLDecoder.decode(tuple[0], URL_ENCODING_CHARSET);
                }
                if (tuple.length > 1 && tuple[1] != null) {
                    value = URLDecoder.decode(tuple[1], URL_ENCODING_CHARSET);
                }
                if (name != null) {
                    cursor.insertAfter(name, value);
                }
            }
        } finally {
            cursor.destroy();
        }

        return IDataHelper.normalize(document);
    }

    /**
     * Serializes the given IData document.
     *
     * @param outputStream  The output stream the serialized IData is written to.
     * @param document      The IData document to be serialized.
     * @param charset       The character set to use when serializing the IData document.
     * @throws IOException  If an I/O error occurs.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, CharsetHelper.normalize(charset)), InputOutputHelper.DEFAULT_BUFFER_SIZE);

        IData denormalizedDcument = IDataHelper.denormalize(document);
        IDataCursor cursor = denormalizedDcument.getCursor();

        try {
            boolean requiresItemSeparator = false;
            while(cursor.next()) {
                String key = cursor.getKey();
                Object value = cursor.getValue();
                if (key != null) {
                    if (requiresItemSeparator) {
                        writer.write("&");
                    }
                    writer.write(URLEncoder.encode(key, URL_ENCODING_CHARSET));
                    writer.write("=");
                    if (value != null) {
                        writer.write(URLEncoder.encode(ObjectHelper.stringify(value), URL_ENCODING_CHARSET));
                    }
                    requiresItemSeparator = true;
                }
            }
        } finally {
            CloseableHelper.close(writer);
            cursor.destroy();
        }
    }
}
