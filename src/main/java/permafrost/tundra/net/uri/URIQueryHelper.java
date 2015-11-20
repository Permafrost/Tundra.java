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

package permafrost.tundra.net.uri;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.CharsetHelper;
import java.nio.charset.Charset;

/**
 * A collection of convenience methods for working with URI query strings.
 */
public class URIQueryHelper {
    /**
     * Parses a query string.
     *
     * @param input  The query string to be parsed.
     * @param decode Whether to URI decode the values in the query string.
     * @return An IData representation of the parsed query string.
     */
    public static IData parse(String input, boolean decode) {
        return parse(input, URIHelper.DEFAULT_CHARSET, decode);
    }

    /**
     * Parses a query string.
     *
     * @param input       The query string to be parsed.
     * @param charsetName The character set to use when decoding URI encoded values.
     * @param decode      Whether to URI decode the values in the query string.
     * @return An IData representation of the parsed query string.
     */
    public static IData parse(String input, String charsetName, boolean decode) {
        return parse(input, CharsetHelper.normalize(charsetName, URIHelper.DEFAULT_CHARSET), decode);
    }

    /**
     * Parses a query string.
     *
     * @param input   The query string to be parsed.
     * @param charset The character set to use when decoding URI encoded values.
     * @param decode  Whether to URI decode the values in the query string.
     * @return An IData representation of the parsed query string.
     */
    public static IData parse(String input, Charset charset, boolean decode) {
        if (input == null) return null;

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        for (String pair : input.split("&")) {
            String[] tokens = pair.split("=", 2);
            String name = tokens.length > 0 ? tokens[0] : "";
            String value = tokens.length > 1 ? tokens[1] : "";

            if (decode) {
                name = URIHelper.decode(name, charset);
                value = URIHelper.decode(value, charset);
            }

            Object existing = IDataUtil.get(cursor, name);
            if (existing == null) {
                IDataUtil.put(cursor, name, value);
            } else {
                // support lists in query strings: a=1&a=2&a=3 should be parsed to a[] = { 1, 2, 3 }
                String[] array = null;
                if (existing instanceof String) {
                    array = new String[2];
                    array[0] = (String)existing;
                    array[1] = value;
                } else if (existing instanceof String[]) {
                    array = ArrayHelper.append((String[])existing, value, String.class);
                }
                IDataUtil.put(cursor, name, array);
            }
        }

        cursor.destroy();

        return output;
    }

    /**
     * Emits a query string given a name and value.
     *
     * @param name    The query string parameter's name.
     * @param value   The query string parameter's value.
     * @param charset The character set to use when URI encoding the parameter's value.
     * @param encode  True if the parameter's value should be URI encoded.
     * @return A query string containing the specified parameter.
     */
    private static String emit(String name, Object value, Charset charset, boolean encode) {
        if (encode) {
            name = URIHelper.encode(name, charset);
            value = URIHelper.encode(value.toString(), charset);
        }
        return name + "=" + value;
    }

    /**
     * Emits a query string given a name and array of values.
     *
     * @param name    The query string parameter's name.
     * @param values  A list of values for the query string parameter.
     * @param charset The character set to use when URI encoding the parameter's value.
     * @param encode  True if the parameter's value should be URI encoded.
     * @return A query string containing the specified parameter.
     */
    private static String emit(String name, Object[] values, Charset charset, boolean encode) {
        StringBuilder output = new StringBuilder();
        for (Object value : values) {
            if (output.length() > 0) output.append("&");
            output.append(emit(name, value, charset, encode));
        }
        return output.toString();
    }

    /**
     * Emits a query string given an IData containing name value pairs.
     *
     * @param input  An IData containing keys and values to serialized as a query string.
     * @param encode True if the query string parameters should be URI encoded.
     * @return A query string containing the parameters in the given IData.
     */
    public static String emit(IData input, boolean encode) {
        return emit(input, URIHelper.DEFAULT_CHARSET_NAME, encode);
    }

    /**
     * Emits a query string given an IData containing name value pairs.
     *
     * @param input       An IData containing keys and values to serialized as a query string.
     * @param charsetName The character set to use when URI encoding the parameters.
     * @param encode      True if the query string parameters should be URI encoded.
     * @return A query string containing the parameters in the given IData.
     */
    public static String emit(IData input, String charsetName, boolean encode) {
        return emit(input, CharsetHelper.normalize(charsetName, URIHelper.DEFAULT_CHARSET), encode);
    }

    /**
     * Emits a query string given an IData containing name value pairs.
     *
     * @param input   An IData containing keys and values to serialized as a query string.
     * @param charset The character set to use when URI encoding the parameters.
     * @param encode  True if the query string parameters should be URI encoded.
     * @return A query string containing the parameters in the given IData.
     */
    public static String emit(IData input, Charset charset, boolean encode) {
        if (input == null) return null;

        StringBuilder output = new StringBuilder();

        IDataCursor cursor = input.getCursor();
        while (cursor.next()) {
            String key = cursor.getKey();
            Object value = cursor.getValue();

            if (value != null) {
                if (output.length() > 0) output.append("&");
                if (value instanceof Object[]) {
                    output.append(emit(key, (Object[])value, charset, encode));
                } else {
                    output.append(emit(key, value, charset, encode));
                }
            }
        }

        return output.toString();
    }
}
