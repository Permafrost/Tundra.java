/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
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
import permafrost.tundra.lang.CharsetHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

/**
 * Deserializes and serializes IData objects from and to Java properties format.
 */
public final class IDataPropertiesParser extends IDataParser {
    /**
     * Construct a new IDataPropertiesParser.
     */
    public IDataPropertiesParser() {
        super("text/x-java-properties");
    }

    /**
     * Returns an IData representation of the Java properties data read from the given input stream.
     *
     * @param inputStream       The input stream to be decoded.
     * @param charset           The character set to use.
     * @return                  An IData representation of the given input stream data.
     * @throws IOException      If there is a problem reading from the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException, ServiceException {
        Properties properties = new Properties();
        properties.load(new InputStreamReader(inputStream, CharsetHelper.normalize(charset)));
        return IDataHelper.toIData(properties);
    }

    /**
     * Serializes the given IData document as Java properties to the given output stream.
     *
     * @param outputStream      The stream to write the encoded IData to.
     * @param document          The IData document to be encoded.
     * @param charset           The character set to use.
     * @throws IOException      If there is a problem writing to the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException, ServiceException {
        Properties properties = new Properties();

        for (Map.Entry<String, Object> entry : IDataMap.of(document)) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                properties.put(key, value.toString());
            }
        }

        properties.store(new OutputStreamWriter(outputStream, CharsetHelper.normalize(charset)), null);
    }
}
