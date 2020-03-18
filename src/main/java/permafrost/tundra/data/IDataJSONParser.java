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
import com.wm.data.IDataPortable;
import com.wm.data.IDataUtil;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import org.glassfish.json.JsonProviderImpl;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.CharsetHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;

/**
 * Deserializes and serializes IData objects from and to JSON.
 */
public class IDataJSONParser extends IDataParser {
    /**
     * Factory for creating JSON readers.
     */
    private JsonReaderFactory jsonReaderFactory;
    /**
     * Factory for created JSON writers.
     */
    private JsonWriterFactory jsonWriterFactory;
    /**
     * Implementation class used for JSON parsing and emitting.
     */
    private JsonProvider provider;

    /**
     * Construct a new pretty printing IData JSON parser/emitter.
     */
    public IDataJSONParser() {
        this(true);
    }

    /**
     * Construct a new IData JSON parser/emitter.
     *
     * @param prettyPrinting If true, will emit pretty printed JSON.
     */
    public IDataJSONParser(boolean prettyPrinting) {
        super("application/json");

        // using the org.glassfish.json implementation directly improves performance by avoiding disk access and thread
        // contention caused by the class loading in the javax.json.spi.JsonProvider.provider() method
        provider = new JsonProviderImpl();

        // create reader factory
        jsonReaderFactory = provider.createReaderFactory(null);

        // create pretty printing writer factory
        Map<String, Object> properties = new HashMap<String, Object>(1);
        if (prettyPrinting) properties.put(JsonGenerator.PRETTY_PRINTING, prettyPrinting);
        jsonWriterFactory = provider.createWriterFactory(properties);
    }

    /**
     * Returns an IData representation of the JSON data in the given input stream.
     *
     * @param inputStream   The input stream to be decoded.
     * @param charset       The character set to use.
     * @return              An IData representation of the given input stream data.
     * @throws IOException  If there is a problem reading from the stream.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException {
        JsonReader reader = jsonReaderFactory.createReader(inputStream, CharsetHelper.normalize(charset));
        JsonStructure structure = reader.read();
        reader.close();

        Object object = fromJsonValue(structure);
        IData output = null;

        if (object instanceof IData) {
            output = (IData)object;
        } else if (object instanceof Object[]) {
            output = IDataFactory.create();
            IDataCursor cursor = output.getCursor();
            IDataUtil.put(cursor, "recordWithNoID", object);
            cursor.destroy();
        }

        return output;
    }

    /**
     * Encodes the given IData document as JSON to the given output stream.
     *
     * @param outputStream  The stream to write the encoded IData to.
     * @param document      The IData document to be encoded.
     * @param charset       The character set to use.
     * @throws IOException  If there is a problem writing to the stream.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException {
        IDataCursor cursor = document.getCursor();

        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = jsonWriterFactory.createWriter(stringWriter);

            Object[] array = IDataUtil.getObjectArray(cursor, "recordWithNoID");
            JsonStructure structure;

            if (array != null) {
                structure = toJsonArray(provider, array);
            } else {
                structure = toJsonObject(provider, document);
            }

            writer.write(structure);
            writer.close();

            InputOutputHelper.copy(InputStreamHelper.normalize(stringWriter.toString().trim(), charset), outputStream);
        } finally {
            cursor.destroy();
        }
    }

    /**
     * Converts an JSON value to an appropriate webMethods compatible representation.
     *
     * @param input The JSON value to convert.
     * @return The converted Object.
     */
    protected Object fromJsonValue(JsonValue input) {
        Object output = null;

        if (input != null) {
            JsonValue.ValueType type = input.getValueType();

            if (type == JsonValue.ValueType.OBJECT) {
                output = fromJsonObject((JsonObject)input);
            } else if (type == JsonValue.ValueType.ARRAY) {
                output = fromJsonArray((JsonArray)input);
            } else if (type == JsonValue.ValueType.TRUE) {
                output = Boolean.TRUE;
            } else if (type == JsonValue.ValueType.FALSE) {
                output = Boolean.FALSE;
            } else if (type == JsonValue.ValueType.NUMBER) {
                output = fromJsonNumber((JsonNumber)input);
            } else if (type == JsonValue.ValueType.STRING) {
                output = fromJsonString((JsonString)input);
            } else if (type != JsonValue.ValueType.NULL) {
                throw new IllegalArgumentException("Unexpected JSON value type: " + type.toString());
            }
        }

        return output;
    }

    /**
     * Converts a JSON string to an appropriate webMethods compatible representation.
     *
     * @param input The JSON string to convert.
     * @return The converted Object.
     */
    protected Object fromJsonString(JsonString input) {
        return input.getString();
    }

    /**
     * Converts a JSON number to an appropriate webMethods compatible representation.
     *
     * @param input The JSON number to convert.
     * @return The converted Object.
     */
    protected Object fromJsonNumber(JsonNumber input) {
        Object output;
        if (input.isIntegral()) {
            output = input.longValue();
        } else {
            output = input.doubleValue();
        }
        return output;
    }

    /**
     * Converts a JSON object to an IData document.
     *
     * @param input The JSON object to be converted.
     * @return The converted IData document.
     */
    protected IData fromJsonObject(JsonObject input) {
        if (input == null) return null;

        Iterator<String> iterator = input.keySet().iterator();

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        while (iterator.hasNext()) {
            String key = iterator.next();
            JsonValue value = input.get(key);
            IDataUtil.put(cursor, key, fromJsonValue(value));
        }

        cursor.destroy();

        return output;
    }

    /**
     * Converts a JSON array to an Object[].
     *
     * @param input The JSON array to convert.
     * @return The converted Object[].
     */
    protected Object[] fromJsonArray(JsonArray input) {
        if (input == null) return null;

        List<Object> output = new ArrayList<Object>(input.size());

        for(JsonValue item : input) {
            Object object = fromJsonValue(item);
            output.add(object);
        }

        return ArrayHelper.normalize(output);
    }

    /**
     * Converts an IData document to a JSON object.
     *
     * @param input An IData document.
     * @return A JSON object.
     */
    @SuppressWarnings("deprecation")
    public static JsonObject toJsonObject(JsonProvider provider, IData input) {
        JsonObjectBuilder builder = provider.createObjectBuilder();

        if (input != null) {
            IDataCursor cursor = input.getCursor();

            while (cursor.next()) {
                String key = cursor.getKey();
                Object value = cursor.getValue();

                if (value == null) {
                    builder.addNull(key);
                } else if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                    builder.add(key, toJsonArray(provider, IDataHelper.toIDataArray(value)));
                } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                    builder.add(key, toJsonObject(provider, IDataHelper.toIData(value)));
                } else if (value instanceof Object[]) {
                    builder.add(key, toJsonArray(provider, (Object[])value));
                } else if (value instanceof Boolean) {
                    builder.add(key, ((Boolean)value));
                } else if (value instanceof Integer) {
                    builder.add(key, (Integer)value);
                } else if (value instanceof Long) {
                    builder.add(key, ((Long)value));
                } else if (value instanceof BigInteger) {
                    builder.add(key, (BigInteger)value);
                } else if (value instanceof Float) {
                    builder.add(key, ((Float)value));
                } else if (value instanceof Double) {
                    builder.add(key, ((Double)value));
                } else if (value instanceof BigDecimal) {
                    builder.add(key, (BigDecimal)value);
                } else {
                    builder.add(key, value.toString());
                }
            }
        }

        return builder.build();
    }

    /**
     * Converts an Object[] to a JSON array.
     *
     * @param input An Object[] to be converted.
     * @return A JSON array.
     */
    @SuppressWarnings("deprecation")
    public static JsonArray toJsonArray(JsonProvider provider, Object[] input) {
        JsonArrayBuilder builder = provider.createArrayBuilder();

        if (input != null) {
            for (Object value : input) {
                if (value == null) {
                    builder.addNull();
                } else if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                    builder.add(toJsonArray(provider, IDataHelper.toIDataArray(value)));
                } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                    builder.add(toJsonObject(provider, IDataHelper.toIData(value)));
                } else if (value instanceof Object[]) {
                    builder.add(toJsonArray(provider, (Object[])value));
                } else if (value instanceof Boolean) {
                    builder.add(((Boolean)value));
                } else if (value instanceof Integer) {
                    builder.add((Integer)value);
                } else if (value instanceof Long) {
                    builder.add((Long)value);
                } else if (value instanceof BigInteger) {
                    builder.add((BigInteger)value);
                } else if (value instanceof Float) {
                    builder.add(((Float)value));
                } else if (value instanceof Double) {
                    builder.add(((Double)value));
                } else if (value instanceof BigDecimal) {
                    builder.add((BigDecimal)value);
                } else {
                    builder.add(value.toString());
                }
            }
        }

        return builder.build();
    }
}
