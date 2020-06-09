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
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataPortable;
import com.wm.data.IDataUtil;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonType;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import permafrost.tundra.io.CloseableHelper;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.io.ReaderHelper;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.CharsetHelper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializes and serializes IData objects from and to Hjson.
 */
public class IDataHjsonParser extends IDataParser {
    /**
     * Default constructor.
     */
    public IDataHjsonParser() {
        super("application/hjson");
    }

    /**
     * Returns an IData representation of the JSON data in the given input stream.
     *
     * @param inputStream       The input stream to be decoded.
     * @param charset           The character set to use.
     * @return                  An IData representation of the given input stream data.
     * @throws IOException      If there is a problem reading from the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException, ServiceException {
        Object object = fromJsonValue(JsonValue.readHjson(ReaderHelper.normalize(inputStream, charset)));

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
     * @param outputStream      The stream to write the encoded IData to.
     * @param document          The IData document to be encoded.
     * @param charset           The character set to use.
     * @throws IOException      If there is a problem writing to the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException, ServiceException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, CharsetHelper.normalize(charset)), InputOutputHelper.DEFAULT_BUFFER_SIZE);

        try {
            if (document != null) {
                IDataCursor cursor = document.getCursor();
                Object[] array = IDataUtil.getObjectArray(cursor, "recordWithNoID");
                cursor.destroy();

                JsonValue value;

                if (array != null) {
                    value = toJsonArray(array);
                } else {
                    value = toJsonObject(document);
                }

                value.writeTo(writer, Stringify.HJSON);
            }
        } finally {
            CloseableHelper.close(writer);
        }
    }

    /**
     * Converts an JSON value to an appropriate webMethods compatible representation.
     *
     * @param input The JSON value to convert.
     * @return      The converted Object.
     */
    protected static Object fromJsonValue(JsonValue input) {
        Object output = null;

        if (input != null) {
            JsonType type = input.getType();

            if (type == JsonType.OBJECT) {
                output = fromJsonObject((JsonObject)input);
            } else if (type == JsonType.ARRAY) {
                output = fromJsonArray((JsonArray)input);
            } else if (type == JsonType.BOOLEAN) {
                output = input.asBoolean();
            } else if (type == JsonType.NUMBER) {
                output = input.asDouble();
            } else if (type == JsonType.STRING) {
                output = input.asString();
            } else if (type != JsonType.NULL) {
                throw new IllegalArgumentException("Unexpected Hjson value type: " + type.toString());
            }
        }

        return output;
    }

    /**
     * Converts a JSON object to an IData document.
     *
     * @param input The JSON object to be converted.
     * @return      The converted IData document.
     */
    protected static IData fromJsonObject(JsonObject input) {
        if (input == null) return null;

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        for (JsonObject.Member member : input) {
            cursor.insertAfter(member.getName(), fromJsonValue(member.getValue()));
        }

        cursor.destroy();

        return output;
    }

    /**
     * Converts a JSON array to an Object[].
     *
     * @param input The JSON array to convert.
     * @return      The converted Object[].
     */
    protected static Object[] fromJsonArray(JsonArray input) {
        if (input == null) return null;

        List<Object> output = new ArrayList<Object>(input.size());

        for(JsonValue item : input) {
            output.add(fromJsonValue(item));
        }

        return ArrayHelper.normalize(output);
    }

    /**
     * Converts an IData document to an Hjson object.
     *
     * @param input An IData document.
     * @return      An Hjson object.
     */
    @SuppressWarnings("deprecation")
    protected static JsonObject toJsonObject(IData input) {
        JsonObject object = new JsonObject();

        if (input != null) {
            IDataCursor cursor = input.getCursor();

            while (cursor.next()) {
                String key = cursor.getKey();
                Object value = cursor.getValue();

                if (value == null) {
                    // omit as nulls are not supported by the Java Hjson implementation
                } else if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                    object.add(key, toJsonArray(IDataHelper.toIDataArray(value)));
                } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                    object.add(key, toJsonObject(IDataHelper.toIData(value)));
                } else if (value instanceof Object[]) {
                    object.add(key, toJsonArray((Object[])value));
                } else if (value instanceof Boolean) {
                    object.add(key, ((Boolean)value));
                } else if (value instanceof Integer) {
                    object.add(key, (Integer)value);
                } else if (value instanceof Long) {
                    object.add(key, ((Long)value));
                } else if (value instanceof BigInteger) {
                    object.add(key, ((BigInteger)value).longValue());
                } else if (value instanceof Float) {
                    object.add(key, ((Float)value));
                } else if (value instanceof Double) {
                    object.add(key, ((Double)value));
                } else if (value instanceof BigDecimal) {
                    object.add(key, ((BigDecimal)value).doubleValue());
                } else {
                    object.add(key, value.toString());
                }
            }
        }

        return object;
    }

    /**
     * Converts an Object[] to an Hjson array.
     *
     * @param input An Object[] to be converted.
     * @return      An Hjson array.
     */
    @SuppressWarnings("deprecation")
    protected static JsonArray toJsonArray(Object[] input) {
        JsonArray array = new JsonArray();

        if (input != null) {
            for (Object value : input) {
                if (value == null) {
                    // omit as nulls are not supported by the Java Hjson implementation
                } else if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                    array.add(toJsonArray(IDataHelper.toIDataArray(value)));
                } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                    array.add(toJsonObject(IDataHelper.toIData(value)));
                } else if (value instanceof Object[]) {
                    array.add(toJsonArray((Object[])value));
                } else if (value instanceof Boolean) {
                    array.add(((Boolean)value));
                } else if (value instanceof Integer) {
                    array.add((Integer)value);
                } else if (value instanceof Long) {
                    array.add((Long)value);
                } else if (value instanceof BigInteger) {
                    array.add(((BigInteger)value).longValue());
                } else if (value instanceof Float) {
                    array.add(((Float)value));
                } else if (value instanceof Double) {
                    array.add(((Double)value));
                } else if (value instanceof BigDecimal) {
                    array.add(((BigDecimal)value).doubleValue());
                } else {
                    array.add(value.toString());
                }
            }
        }

        return array;
    }
}