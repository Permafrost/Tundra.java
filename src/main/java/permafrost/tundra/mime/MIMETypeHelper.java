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

package permafrost.tundra.mime;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.ExceptionHelper;
import java.util.Enumeration;
import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

public class MIMETypeHelper {
    /**
     * Disallow instantiation of this class.
     */
    private MIMETypeHelper() {}

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    /**
     * Parses the given MIME type string to an IData representation.
     *
     * @param string The MIME type string to be parsed.
     * @return An IData representation of the MIME type string.
     * @throws MimeTypeParseException If the given MIME type string is malformed.
     */
    public static IData parse(String string) throws MimeTypeParseException {
        if (string == null) return null;
        return toIData(new MimeType(string));
    }

    /**
     * Returns a MIME type string comprised of the components specified in the given IData document.
     *
     * @param document The IData document to be converted to a MIME type string.
     * @return A MIME type string representing the components specified in the given IData document.
     * @throws MimeTypeParseException If the given MIME type string is malformed.
     */
    public static String emit(IData document) throws MimeTypeParseException {
        if (document == null) return null;

        IDataCursor cursor = document.getCursor();
        String type = IDataUtil.getString(cursor, "type");
        String subtype = IDataUtil.getString(cursor, "subtype");
        IData parameters = IDataUtil.getIData(cursor, "parameters");
        cursor.destroy();

        if (type == null) throw new IllegalArgumentException("type must not be null");
        if (subtype == null) throw new IllegalArgumentException("subtype must not be null");

        MimeType mimeType = new MimeType(type, subtype);

        if (parameters != null) {
            parameters = IDataHelper.sort(parameters, false, true);
            cursor = parameters.getCursor();
            while (cursor.next()) {
                String key = cursor.getKey();
                Object value = cursor.getValue();
                if (value instanceof String) {
                    mimeType.setParameter(key, (String)value);
                }
            }
            cursor.destroy();
        }

        return mimeType.toString();
    }

    /**
     * Normalizes a MIME type string by removing extraneous whitespace characters, and listing parameters in
     * alphabetical order.
     *
     * @param string The MIME type string to be normalized.
     * @return The normalized MIME type string.
     * @throws MimeTypeParseException If the MIME type string is malformed.
     */
    public static String normalize(String string) throws MimeTypeParseException {
        return emit(parse(string));
    }

    /**
     * Returns true if the given MIME type strings are considered equivalent because their types and subtypes match
     * (parameters are not considered in the comparison).
     *
     * @param string1 The first MIME type string to be compare.
     * @param string2 The second MIME type string to be compared.
     * @return True if the two MIME type strings are considered equal, otherwise false.
     * @throws MimeTypeParseException If either of the MIME type strings are malformed.
     */
    public static boolean equal(String string1, String string2) throws MimeTypeParseException {
        if (string1 == null && string2 == null) return true;
        if (string1 == null || string2 == null) return false;

        MimeType mimeType1 = new MimeType(string1);
        MimeType mimeType2 = new MimeType(string2);

        return mimeType1.match(mimeType2);
    }

    /**
     * Returns true if the given string is a valid MIME type, optionally throwing an exception if the string is
     * invalid.
     *
     * @param string The string to validate.
     * @param raise  Whether an exception should be thrown if the string is an invalid MIME type.
     * @return True if the string is a well-formed MIME type.
     * @throws ServiceException If raise is true and the given string is an invalid MIME type.
     */
    public static boolean validate(String string, boolean raise) throws ServiceException {
        boolean valid = false;
        if (string != null) {
            try {
                MimeType type = new MimeType(string);
                valid = true;
            } catch (MimeTypeParseException ex) {
                if (raise) ExceptionHelper.raise(ex);
            }
        }
        return valid;
    }

    /**
     * Returns true if the given string is a valid MIME type.
     *
     * @param string The string to validate.
     * @return True if the string is a well-formed MIME type.
     */
    public static boolean validate(String string) {
        boolean result = false;
        try {
            result = validate(string, false);
        } catch (ServiceException ex) {
            // ignore as this exception will never be thrown
        }
        return result;
    }

    /**
     * Converts a MimeType to an IData representation.
     *
     * @param mimeType The MimeType to convert.
     * @return An IData representation of the given MimeType.
     */
    public static IData toIData(MimeType mimeType) {
        if (mimeType == null) return null;

        IDataMap output = new IDataMap();

        output.put("type", mimeType.getPrimaryType());
        output.put("subtype", mimeType.getSubType());

        MimeTypeParameterList mimeTypeParameterList = mimeType.getParameters();
        if (mimeTypeParameterList.size() > 0) output.put("parameters", toIData(mimeTypeParameterList));

        return output;
    }

    /**
     * Converts a MimeTypeParameterList to an IData representation.
     *
     * @param mimeTypeParameterList The MimeTypeParameterList to convert.
     * @return An IData representation of the given MimeTypeParameterList.
     */
    public static IData toIData(MimeTypeParameterList mimeTypeParameterList) {
        if (mimeTypeParameterList == null) return null;

        IDataMap output = new IDataMap();

        Enumeration names = mimeTypeParameterList.getNames();
        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            String value = mimeTypeParameterList.get(name);
            output.put(name, value);
        }

        return output;
    }
}
