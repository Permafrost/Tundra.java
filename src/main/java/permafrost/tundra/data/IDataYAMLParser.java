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
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;
import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.StringHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Deserializes and serializes IData objects from and to YAML.
 */
public class IDataYAMLParser extends IDataTextParser {
    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final IDataYAMLParser INSTANCE = new IDataYAMLParser();
    }

    /**
     * Disallow instantiation of this class.
     */
    private IDataYAMLParser() {}

    /**
     * Returns the singleton instance of this class.
     * @return The singleton instance of this class.
     */
    public static IDataYAMLParser getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Serializes the given IData document as YAML to the given output stream.
     *
     * @param outputStream  The stream to write the encoded IData to.
     * @param document      The IData document to be encoded.
     * @param charset       The character set to use.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public void encode(OutputStream outputStream, IData document, Charset charset) throws IOException {
        StreamHelper.copy(StreamHelper.normalize(encodeToString(document), charset), outputStream);
    }

    /**
     * Returns an IData representation of the YAML data read from the given input stream.
     *
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
     *
     * @return YAML MIME media type.
     */
    public String getContentType() {
        return "application/yaml";
    }

    /**
     * Returns an IData representation of the YAML data.
     *
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
     *
     * @param input The IData to convert to YAML.
     * @return      The YAML representation of the IData.
     */
    @Override
    public String encodeToString(IData input) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(new Representer(), options);

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

    /**
     * Tundra implementation of YAML Representer which supports InputStreams and
     * Objects with no public members.
     */
    protected class Representer extends org.yaml.snakeyaml.representer.Representer {
        /**
         * Default constructor.
         */
        public Representer() {
            this.multiRepresenters.put(ByteArrayInputStream.class, new RepresentInputStream());
            this.representers.put(null, new RepresentObject());
        }

        /**
         * Expose RepresentString implementation to this class by subclassing it.
         */
        private class RepresentString extends org.yaml.snakeyaml.representer.Representer.RepresentString {}

        /**
         * Represent any Object as a YAML node, even those without public members.
         */
        protected class RepresentObject extends RepresentJavaBean {
            /**
             * The Represent object used when encoding an Object with no public members.
             */
            protected Represent defaultRepresent;

            /**
             * Default constructor.
             */
            public RepresentObject() {
                defaultRepresent = new RepresentString();
            }

            /**
             * Returns a YAML node representation of the given Object: if the Object
             * has public members, these are encoded to YAML, otherwise Object.toString()
             * is encoded to YAML.
             * @param data The Object to be converted to a YAML node.
             */
            public Node representData(Object data) {
                Node node = null;
                try {
                    node = super.representData(data);
                } catch(YAMLException ex) {
                    node = defaultRepresent.representData(data);
                }
                return node;
            }
        }

        /**
         * Represent an InputStream as a YAML node.
         */
        protected class RepresentInputStream extends RepresentByteArray {
            /**
             * Returns a YAML node representation of the given InputStream object.
             * @param data The InputStream to be converted to a YAML node.
             */
            @SuppressWarnings("unchecked")
            public Node representData(Object data) {
                try {
                    return super.representData(BytesHelper.normalize((InputStream) data));
                } catch(IOException ex) {
                    throw new YAMLException(ex);
                }
            }
        }
    }
}
