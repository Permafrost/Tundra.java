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
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.lang.StringHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class IDataYAMLParser extends IDataTextParser {
    protected static IDataYAMLParser INSTANCE = new IDataYAMLParser();

    /**
     * Disallow instantiation of this class.
     */
    private IDataYAMLParser() {}

    /**
     * Returns the singleton instance of this class.
     * @return The singleton instance of this class.
     */
    public static IDataYAMLParser getInstance() {
        return INSTANCE;
    }

    /**
     * Encodes the given IData document as YAML to the given output stream.
     * @param outputStream  The stream to write the encoded IData to.
     * @param document      The IData document to be encoded.
     * @param charset       The character set to use.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public void encode(OutputStream outputStream, IData document, Charset charset) throws IOException {
        StreamHelper.copy(StreamHelper.normalize(encodeToString(document), charset), outputStream);
    }

    /**
     * Returns an IData representation of the YAML data in the given input stream.
     * @param inputStream                       The input stream to be decoded.
     * @param charset                           The character set to use.
     * @return                                  An IData representation of the given input stream data.
     * @throws IOException                      If there is a problem reading from the stream.
     */
    public IData decode(InputStream inputStream, Charset charset) throws IOException {
        return decodeFromString(StringHelper.normalize(inputStream, charset));
    }

    /**
     * The MIME media type for YAML.
     * @return YAML MIME media type.
     */
    public String getContentType() {
        return "application/yaml";
    }

    /**
     * Returns an IData representation of the YAML data.
     * @param string        The String to be decoded.
     * @return              An IData representation of the given data.
     * @throws IOException  If an I/O problem occurs.
     */
    @Override
    public IData decodeFromString(String string) throws IOException {
        Yaml yaml = new Yaml();
        Object object = yaml.load(string);

        IData output = null;

        if (object instanceof Map) {
            output = IDataHelper.toIData((Map)object);
        } else if (object instanceof List) {
            IData[] array = IDataHelper.toIDataArray((List)object);
            output = IDataFactory.create();
            IDataCursor cursor = output.getCursor();
            IDataUtil.put(cursor, "recordWithNoID", array);
            cursor.destroy();
        }

        return output;
    }

    /**
     * Returns a YAML representation of the given IData object.
     * @param input The IData to convert to YAML.
     * @return      The YAML representation of the IData.
     */
    @Override
    public String encodeToString(IData input) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        IDataCursor cursor = input.getCursor();
        IData[] array = IDataUtil.getIDataArray(cursor, "recordWithNoID");
        cursor.destroy();

        Object object = null;
        if (array != null) {
            object = IDataHelper.toList(array);
        } else {
            object = IDataHelper.toMap(input);
        }

        return yaml.dump(object);
    }
}
