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

package permafrost.tundra.uri;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import permafrost.tundra.array.ArrayHelper;
import permafrost.tundra.exception.BaseException;

public class QueryHelper {
    /**
     * Parses a query string.
     * @param input
     * @param decode
     * @return
     * @throws BaseException
     */
    public static IData parse(String input, boolean decode) throws BaseException {
        return parse(input, null, decode);
    }

    /**
     * Parses a query string.
     * @param input
     * @param encoding
     * @param decode
     * @return
     * @throws BaseException
     */
    public static IData parse(String input, String encoding, boolean decode) throws BaseException {
        if (input == null) return null;
        if (encoding == null) encoding = URIHelper.DEFAULT_CHARACTER_ENCODING;

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        for (String pair : input.split("&")) {
            String[] tokens = pair.split("=");
            String name = tokens.length > 0 ? tokens[0] : "";
            String value = tokens.length > 1 ? tokens[1] : "";

            if (decode) {
                name = URIHelper.decode(name, encoding);
                value = URIHelper.decode(value, encoding);
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
                    array = ArrayHelper.append((String[]) existing, value, String.class);
                }
                IDataUtil.put(cursor, name, array);
            }
        }

        cursor.destroy();

        return output;
    }

    /**
     * Emits a query string given a name and value.
     * @param name
     * @param value
     * @param encoding
     * @param encode
     * @return
     * @throws BaseException
     */
    private static String emit(String name, Object value, String encoding, boolean encode) throws BaseException {
        if (encode) {
            name = URIHelper.encode(name, encoding);
            value = URIHelper.encode(value.toString(), encoding);
        }
        return name + "=" + value;
    }

    /**
     * Emits a query string given a name and array of values.
     * @param name
     * @param values
     * @param encoding
     * @param encode
     * @return
     * @throws BaseException
     */
    private static String emit(String name, Object[] values, String encoding, boolean encode) throws BaseException {
        StringBuilder output = new StringBuilder();
        for(Object value : values) {
            if (output.length() > 0) output.append("&");
            output.append(emit(name, value, encoding, encode));
        }
        return output.toString();
    }

    /**
     * Emits a query string given an IData containing name value pairs.
     * @param input
     * @param encode
     * @return
     * @throws BaseException
     */
    public static String emit(IData input, boolean encode) throws BaseException {
        return emit(input, null, encode);
    }

    /**
     * Emits a query string given an IData containing name value pairs.
     * @param input
     * @param encoding
     * @param encode
     * @return
     * @throws BaseException
     */
    public static String emit(IData input, String encoding, boolean encode) throws BaseException {
        if (input == null) return null;
        if (encoding == null) encoding = URIHelper.DEFAULT_CHARACTER_ENCODING;

        StringBuilder output = new StringBuilder();

        IDataCursor cursor = input.getCursor();
        while(cursor.next()) {
            String key = cursor.getKey();
            Object value = cursor.getValue();

            if (value != null) {
                if (output.length() > 0) output.append("&");
                if (value instanceof Object[]) {
                    output.append(emit(key, (Object[])value, encoding, encode));
                } else {
                    output.append(emit(key, value, encoding, encode));
                }
            }
        }

        return output.toString();
    }
}
