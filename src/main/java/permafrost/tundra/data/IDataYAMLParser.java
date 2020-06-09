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

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;
import permafrost.tundra.io.CloseableHelper;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.StringHelper;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Deserializes and serializes IData objects from and to YAML.
 */
public class IDataYAMLParser extends IDataParser {
    /**
     * Default constructor.
     */
    public IDataYAMLParser() {
        super("application/yaml");
    }

    /**
     * Serializes the given IData document as YAML to the given output stream.
     *
     * @param outputStream      The stream to write the encoded IData to.
     * @param document          The IData document to be encoded.
     * @param charset           The character set to use.
     * @throws IOException      If there is a problem writing to the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException, ServiceException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, CharsetHelper.normalize(charset)), InputOutputHelper.DEFAULT_BUFFER_SIZE);
        IDataCursor cursor = document.getCursor();

        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml parser = new Yaml(new Representer(), options);
            Object value = IDataUtil.get(cursor, "recordWithNoID");

            Object object;
            if (value instanceof IData[]) {
                object = IDataHelper.toList((IData[]) value);
            } else if (value instanceof Object[]) {
                object = Arrays.asList((Object[]) value);
            } else if (value != null) {
                object = value;
            } else {
                object = IDataHelper.toMap(document);
            }

            parser.dump(object, writer);
        } finally {
            CloseableHelper.close(writer);
            cursor.destroy();
        }
    }

    /**
     * Returns an IData representation of the YAML data read from the given input stream.
     *
     * @param inputStream       The input stream to be decoded.
     * @param charset           The character set to use.
     * @return                  An IData representation of the given input stream data.
     * @throws IOException      If there is a problem reading from the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException, ServiceException {
        Object value = normalize(new Yaml().load(StringHelper.normalize(inputStream, charset)));

        IData output;

        if (value instanceof IData) {
            output = (IData)value;
        } else {
            output = IDataFactory.create();
            IDataCursor cursor = output.getCursor();
            IDataUtil.put(cursor, "recordWithNoID", value);
            cursor.destroy();
        }

        return output;
    }

    /**
     * Converts a parsed value to an IData compatible representation.
     *
     * @param value The value to be converted.
     * @return      The converted value.
     */
    private Object normalize(Object value) {
        if (value instanceof Map) {
            value = IDataHelper.toIData((Map)value);
        } else if (value instanceof List) {
            List input = (List)value;
            List<Object> output = new ArrayList<Object>(input.size());

            for (Object item : input) {
                output.add(normalize(item));
            }

            value = ArrayHelper.normalize(output);
        }
        return value;
    }

    /**
     * Tundra implementation of YAML Representer which supports InputStreams and Objects with no public members.
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
             * Returns a YAML node representation of the given Object: if the Object has public members, these are
             * encoded to YAML, otherwise Object.toString() is encoded to YAML.
             *
             * @param data The Object to be converted to a YAML node.
             */
            public Node representData(Object data) {
                Node node;
                try {
                    node = super.representData(data);
                } catch (YAMLException ex) {
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
             *
             * @param data The InputStream to be converted to a YAML node.
             */
            @SuppressWarnings("unchecked")
            public Node representData(Object data) {
                try {
                    return super.representData(BytesHelper.normalize((InputStream)data));
                } catch (IOException ex) {
                    throw new YAMLException(ex);
                }
            }
        }
    }
}
