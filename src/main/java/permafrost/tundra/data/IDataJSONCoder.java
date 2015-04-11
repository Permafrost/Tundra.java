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
import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.lang.ArrayHelper;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IDataJSONCoder extends IDataStringCoder {
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
        JsonReaderFactory factory = Json.createReaderFactory(null);
        JsonReader reader = factory.createReader(inputStream, charset);
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
        }

        return output;
    }

    /**
     * The MIME media type for YAML.
     * @return YAML MIME media type.
     */
    public String getContentType() {
        return "application/json";
    }

    /**
     * Converts an JSON value to an appropriate webMethods compatible representation.
     * @param input The JSON value to convert.
     * @return      The converted Object.
     */
    protected static Object fromJsonValue(JsonValue input) {
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
            } else if (type != JsonValue.ValueType.NULL){
                throw new IllegalArgumentException("Unexpected JSON value type: " + type.toString());
            }
        }

        return output;
    }

    /**
     * Converts a JSON string to an appropriate webMethods compatible representation.
     * @param input The JSON string to convert.
     * @return      The converted Object.
     */
    protected static Object fromJsonString(JsonString input) {
        return input.getString();
    }

    /**
     * Converts a JSON number to an appropriate webMethods compatible representation.
     * @param input The JSON number to convert.
     * @return      The converted Object.
     */
    protected static Object fromJsonNumber(JsonNumber input) {
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
     * @param input The JSON object to be converted.
     * @return      The converted IData document.
     */
    protected static IData fromJsonObject(JsonObject input) {
        if (input == null) return null;

        Iterator<String> iterator = input.keySet().iterator();

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        while(iterator.hasNext()) {
            String key = iterator.next();
            JsonValue value = input.get(key);
            IDataUtil.put(cursor, key, fromJsonValue(value));
        }

        cursor.destroy();

        return output;
    }

    /**
     * Converts a JSON array to an Object[].
     * @param input The JSON array to convert.
     * @return      The converted Object[].
     */
    protected static Object[] fromJsonArray(JsonArray input) {
        if (input == null) return null;

        List output = new java.util.ArrayList(input.size());
        Iterator<JsonValue> iterator = input.iterator();

        while(iterator.hasNext()) {
            JsonValue item = iterator.next();
            Object object = fromJsonValue(item);
            output.add(object);
        }

        return ArrayHelper.normalize(output.toArray());
    }

    /**
     * Returns a JSON representation of the given IData object.
     * @param input The IData to convert to JSON.
     * @return      The JSON representation of the IData.
     */
    @Override
    public String encodeToString(IData input) throws IOException {
        java.io.StringWriter stringWriter = new java.io.StringWriter();
        javax.json.JsonWriter writer = Json.createWriter(stringWriter);

        IDataCursor cursor = input.getCursor();
        Object[] array = IDataUtil.getObjectArray(cursor, "recordWithNoID");
        cursor.destroy();

        if (array != null) {
            writer.write(toJsonArray(array));
        } else {
            writer.write(toJsonObject(input));
        }

        writer.close();

        return stringWriter.toString();
    }

    /**
     * Converts an IData document to a JSON object.
     * @param input An IData document.
     * @return      A JSON object.
     */
    protected static JsonObject toJsonObject(IData input) {
        javax.json.JsonObjectBuilder builder = Json.createObjectBuilder();

        if (input != null) {
            IDataCursor cursor = input.getCursor();

            while(cursor.next()) {
                String key = cursor.getKey();
                Object value = cursor.getValue();

                if (value == null) {
                    builder.addNull(key);
                } else if (value instanceof IData) {
                    builder.add(key, toJsonObject((IData)value));
                } else if (value instanceof com.wm.util.Table) {
                    value = ((com.wm.util.Table)value).getValues();
                    builder.add(key, toJsonArray((IData[])value));
                } else if (value instanceof Object[]) {
                    builder.add(key, toJsonArray((Object[])value));
                } else if (value instanceof Boolean) {
                    builder.add(key, ((Boolean)value));
                } else if (value instanceof Integer) {
                    builder.add(key, ((Integer)value).intValue());
                } else if (value instanceof Long) {
                    builder.add(key, ((Long)value).longValue());
                } else if (value instanceof java.math.BigInteger) {
                    builder.add(key, (java.math.BigInteger)value);
                } else if (value instanceof Float) {
                    builder.add(key, ((Float)value));
                } else if (value instanceof Double) {
                    builder.add(key, ((Double)value));
                } else if (value instanceof java.math.BigDecimal) {
                    builder.add(key, (java.math.BigDecimal)value);
                } else {
                    builder.add(key, value.toString());
                }
            }
        }

        return builder.build();
    }

    /**
     * Converts an Object[] to a JSON array.
     * @param input An Object[] to be converted.
     * @return      A JSON array.
     */
    protected static JsonArray toJsonArray(Object[] input) {
        javax.json.JsonArrayBuilder builder = Json.createArrayBuilder();

        if (input != null) {
            for (int i = 0; i < input.length; i++) {
                Object value = input[i];
                if (value == null) {
                    builder.addNull();
                } else if (value instanceof IData) {
                    builder.add(toJsonObject((IData)value));
                } else if (value instanceof com.wm.util.Table) {
                    value = ((com.wm.util.Table)value).getValues();
                    builder.add(toJsonArray((IData[])value));
                } else if (value instanceof Object[]) {
                    builder.add(toJsonArray((Object[])value));
                } else if (value instanceof Boolean) {
                    builder.add(((Boolean)value));
                } else if (value instanceof Integer) {
                    builder.add(((Integer)value).intValue());
                } else if (value instanceof Long) {
                    builder.add(((Long)value).longValue());
                } else if (value instanceof java.math.BigInteger) {
                    builder.add((java.math.BigInteger)value);
                } else if (value instanceof Float) {
                    builder.add(((Float)value));
                } else if (value instanceof Double) {
                    builder.add(((Double)value));
                } else if (value instanceof java.math.BigDecimal) {
                    builder.add((java.math.BigDecimal)value);
                } else {
                    builder.add(value.toString());
                }
            }
        }

        return builder.build();
    }
}
