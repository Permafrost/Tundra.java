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

import com.wm.app.b2b.server.MimeTypes;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.server.NodeHelper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;

public final class MIMETypeHelper {
    /**
     * Disallow instantiation of this class.
     */
    private MIMETypeHelper() {}

    /**
     * The default MIME media type for arbitrary content as a string.
     */
    public static final String DEFAULT_MIME_TYPE_STRING = System.getProperty("watt.server.content.type.default", "application/octet-stream");
    /**
     * A Map of file extensions to MIME type.
     */
    private static final Map<String, Set<String>> FILE_EXTENSIONS_BY_MIME_TYPE = getFileExtensionsByMimeType();

    /**
     * Returns a Map whose keys are the registered MIME types and values are a set of associated file extensions.
     *
     * @return a Map whose keys are the registered MIME types and values are a set of associated file extensions.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Set<String>> getFileExtensionsByMimeType() {
        Map<String, Set<String>> fileExtensionsByMimeType = new TreeMap<String, Set<String>>();

        try {
            Field mimeTypeField = MimeTypes.class.getDeclaredField("mimeType");
            mimeTypeField.setAccessible(true);
            Map<String, String> mimeType = (Map<String, String>)mimeTypeField.get(null);

            for (Map.Entry<String, String> entry : mimeType.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key != null && value != null) {
                    key = key.toLowerCase();
                    value = value.toLowerCase();

                    Set<String> extensions = fileExtensionsByMimeType.get(value);
                    if (extensions == null) extensions = new LinkedHashSet<String>();
                    extensions.add(key);
                    fileExtensionsByMimeType.put(value, extensions);
                }
            }

            for (Map.Entry<String, Set<String>> entry : fileExtensionsByMimeType.entrySet()) {
                fileExtensionsByMimeType.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
            }
        } catch(IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch(NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }

        return fileExtensionsByMimeType;
    }

    /**
     * Returns a new MimeType object given a MIME media type string.
     *
     * @param  string   A MIME media type string.
     * @return          A MimeType object representing the given string.
     */
    public static MimeType of(String string) {
        if (string == null) return null;

        try {
            return new MimeType(string);
        } catch(MimeTypeParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Returns a list of MimeType objects given a list of MIME media type strings.
     *
     * @param strings   List of MIME media type strings.
     * @return          List of MimeType objects.
     */
    public static List<MimeType> of(String ...strings) {
        if (strings == null) return Collections.emptyList();

        List<MimeType> mimeTypes = new ArrayList<MimeType>(strings.length);
        for (String string : strings) {
            if (string != null) {
                mimeTypes.add(of(string));
            }
        }

        return mimeTypes;
    }

    /**
     * Returns true if the given MimeType is a type of text content.
     *
     * @param type  The MimeType in question.
     * @return      Whether the given MimeType represents text content.
     */
    public static boolean isText(MimeType type) {
        boolean isText = false;

        if (type != null) {
            if ("text".equals(type.getPrimaryType()) || type.getParameter("charset") != null) {
                isText = true;
            } else {
                switch(classify(type)) {
                    case XML:
                    case JSON:
                    case PLAIN:
                    case CSV:
                    case TSV:
                    case PSV:
                    case YAML:
                    case HJSON:
                        isText = true;
                        break;
                }
            }
        }

        return isText;
    }

    /**
     * Returns the given MimeType if not null, otherwise the default MimeType.
     *
     * @param type      The MimeType to be normalized.
     * @return          The given MimeType if not null, otherwise the default MimeType.
     */
    public static MimeType normalize(MimeType type) {
        if (type == null) return getDefault();
        return duplicate(type);
    }

    /**
     * Returns the default MimeType.
     *
     * @return     The default MimeType.
     */
    public static MimeType getDefault() {
        try {
            return new MimeType(DEFAULT_MIME_TYPE_STRING);
        } catch(MimeTypeParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Returns a duplicate of the given MimeType.
     *
     * @param type  The MimeType to duplicate.
     * @return      A duplicate of the given MimeType.
     */
    public static MimeType duplicate(MimeType type) {
        if (type == null) return null;
        try {
            return new MimeType(type.toString());
        } catch(MimeTypeParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Classifies the given mime type into one of the following classifications: xml, json, yaml, csv, tsv, psv.
     *
     * @param type  The MIME type to classify.
     * @return      The classification of the MIME type, or null if unclassifiable.
     */
    public static MIMEClassification classify(MimeType type) {
        return classify(type, null);
    }

    /**
     * Classifies the given mime type into one of the following classifications: xml, json, yaml, csv, tsv, psv.
     *
     * @param type          The MIME type to classify.
     * @param contentSchema Optional fully-qualified name of the schema or document reference to use when parsing
     *                      content with this type.
     * @return              The classification of the MIME type, or null if unclassifiable.
     */
    public static MIMEClassification classify(MimeType type, String contentSchema) {
        MIMEClassification classification = MIMEClassification.DEFAULT_MIME_CLASSIFICATION;

        if (contentSchema != null && NodeHelper.exists(contentSchema) && "Flat File Schema".equals(NodeHelper.getNodeType(contentSchema).toString())) {
            classification = MIMEClassification.PLAIN;
        } else if (type != null) {
            String subType = type.getSubType();
            if ("text".equals(type.getPrimaryType()) && "plain".equals(subType)) {
                classification = MIMEClassification.PLAIN;
            } else if (subType.equals("xml") || subType.endsWith("+xml")) {
                classification = MIMEClassification.XML;
            } else if (subType.equals("json") || subType.endsWith("+json")) {
                classification = MIMEClassification.JSON;
            } else if (subType.equals("csv") || subType.endsWith("+csv") || subType.equals("comma-separated-values") || subType.endsWith("+comma-separated-values")) {
                classification = MIMEClassification.CSV;
            } else if (subType.equals("yaml") || subType.endsWith("+yaml") || subType.equals("x-yaml") || subType.endsWith("+x-yaml")) {
                classification = MIMEClassification.YAML;
            } else if (subType.equals("tsv") || subType.endsWith("+tsv") || subType.equals("tab-separated-values") || subType.endsWith("+tab-separated-values")) {
                classification = MIMEClassification.TSV;
            } else if (subType.equals("psv") || subType.endsWith("+psv") || subType.equals("pipe-separated-values") || subType.endsWith("+pipe-separated-values")) {
                classification = MIMEClassification.PSV;
            } else if (subType.equals("vnd.ms-excel")) {
                classification = MIMEClassification.XLS;
            } else if (subType.equals("vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                classification = MIMEClassification.XLSX;
            } else if (subType.equals("hjson") || subType.endsWith("+hjson")) {
                classification = MIMEClassification.HJSON;
            } else {
                MIMEClassification[] values = MIMEClassification.values();
                for (MIMEClassification value : values) {
                    if (type.match(value.getAssociatedType())) {
                        classification = value;
                        break;
                    }
                }
            }
        }

        return classification;
    }

    /**
     * Returns the MIME type associated with the given filename extension.
     *
     * @param extension The filename extension.
     * @return          The MIME type associated with the given extension.
     */
    public static MimeType fromExtension(String extension) {
        return normalize(of(MimeTypes.getTypeFromExtension(extension)));
    }

    /**
     * Returns the MIME type associated with the given filename.
     *
     * @param filename  The filename.
     * @return          The MIME type associated with the given filename.
     */
    public static MimeType fromFilename(String filename) {
        return normalize(of(MimeTypes.getTypeFromName(filename)));
    }

    /**
     * Returns the file extensions associated with the given MIME type.
     *
     * @param mimeType  The MIME type whose associated file extensions are to be returned.
     * @return          The file extensions associated with the given MIME type.
     */
    public static Set<String> getFileExtensions(String mimeType) {
        return getFileExtensions(of(mimeType));
    }

    /**
     * Returns the file extensions associated with the given MIME type.
     *
     * @param mimeType  The MIME type whose associated file extensions are to be returned.
     * @return          The file extensions associated with the given MIME type.
     */
    public static Set<String> getFileExtensions(MimeType mimeType) {
        if (mimeType == null) return null;
        Set<String> extensions = FILE_EXTENSIONS_BY_MIME_TYPE.get(mimeType.getBaseType().toLowerCase());
        if (extensions == null || extensions.size() == 0) {
            extensions = Collections.singleton(classify(mimeType).getDefaultFileExtension());
        }
        return extensions;
    }

    /**
     * Returns the first file extension associated with the given MIME type.
     *
     * @param mimeType  The MIME type whose associated file extension is to be returned.
     * @return          The file extension associated with the given MIME type.
     */
    public static String getFileExtension(String mimeType) {
        return getFileExtension(of(mimeType));
    }

    /**
     * Returns the first file extension associated with the given MIME type.
     *
     * @param mimeType  The MIME type whose associated file extension is to be returned.
     * @return          The file extension associated with the given MIME type.
     */
    public static String getFileExtension(MimeType mimeType) {
        String extension = null;
        Set<String> extensions = getFileExtensions(mimeType);
        if (extensions != null && extensions.size() > 0) {
            extension = extensions.toArray(new String[0])[0];
        }
        return extension;
    }

    /**
     * Parses the given MIME type string to an IData representation.
     *
     * @param string The MIME type string to be parsed.
     * @return An IData representation of the MIME type string.
     * @throws MimeTypeParseException If the given MIME type string is malformed.
     */
    public static IData parse(String string) throws MimeTypeParseException {
        return toIData(of(string));
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
     * @param string1                   The first MIME type string to be compare.
     * @param string2                   The second MIME type string to be compared.
     * @return                          True if the two MIME type strings are considered equal, otherwise false.
     * @throws MimeTypeParseException   If either of the MIME type strings are malformed.
     */
    public static boolean equal(String string1, String string2) throws MimeTypeParseException {
        if (string1 == null && string2 == null) return true;
        if (string1 == null || string2 == null) return false;

        return new ImmutableMimeType(string1).equals(new ImmutableMimeType(string2));
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
