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
import com.wm.data.IDataPortable;
import com.wm.data.IDataUtil;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import org.w3c.dom.Node;
import permafrost.tundra.data.transform.Blankifier;
import permafrost.tundra.data.transform.Nullifier;
import permafrost.tundra.data.transform.Replacer;
import permafrost.tundra.data.transform.Stringifier;
import permafrost.tundra.data.transform.TransformerMode;
import permafrost.tundra.data.transform.Squeezer;
import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.Trimmer;
import permafrost.tundra.flow.ConditionEvaluator;
import permafrost.tundra.flow.variable.SubstitutionHelper;
import permafrost.tundra.io.filter.FilenameFilterType;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.BooleanHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.ObjectConvertMode;
import permafrost.tundra.lang.Sanitization;
import permafrost.tundra.lang.ObjectHelper;
import permafrost.tundra.lang.TableHelper;
import permafrost.tundra.math.BigDecimalHelper;
import permafrost.tundra.math.BigIntegerHelper;
import permafrost.tundra.math.DoubleHelper;
import permafrost.tundra.math.FloatHelper;
import permafrost.tundra.math.IntegerHelper;
import permafrost.tundra.math.LongHelper;
import permafrost.tundra.math.RoundingModeHelper;
import permafrost.tundra.mime.MIMETypeHelper;
import permafrost.tundra.net.http.HTTPMethod;
import permafrost.tundra.server.NodePermission;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import permafrost.tundra.xml.dom.NodeHelper;
import permafrost.tundra.xml.dom.Nodes;
import permafrost.tundra.xml.namespace.IDataNamespaceContext;
import permafrost.tundra.xml.xpath.XPathHelper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.MimeType;
import javax.xml.datatype.Duration;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

/**
 * A collection of convenience methods for working with IData objects.
 */
public final class IDataHelper {
    /**
     * Regular expression pattern for matching an IData node XPath expression.
     */
    public static final Pattern KEY_NODE_XPATH_REGULAR_EXPRESSION_PATTERN = Pattern.compile("(?i)([^\\/]+)(\\/.+)");

    /**
     * Disallow instantiation of this class.
     */
    private IDataHelper() {}

    /**
     * Returns all the keys in the given IData document.
     *
     * @param document  An IData document to retrieve the keys from.
     * @return          The list of keys present in the given IData document.
     */
    public static String[] getKeys(IData document) {
        List<String> keys = getKeyList(document);
        return keys.toArray(new String[keys.size()]);
    }

    /**
     * Returns the keys that match the given regular expression pattern in the given IData document.
     *
     * @param document      An IData document to retrieve the keys from.
     * @param patternString A regular expression pattern which the returned set of keys must match.
     * @return              The list of keys present in the given IData document that match the given regular expression pattern.
     */
    public static String[] getKeys(IData document, String patternString) {
        return getKeys(document, patternString == null ? null : Pattern.compile(patternString));
    }

    /**
     * Returns the keys that match the given regular expression pattern in the given IData document.
     *
     * @param document  An IData document to retrieve the keys from.
     * @param pattern   A regular expression pattern which the returned set of keys must match.
     * @return          The list of keys present in the given IData document that match the given regular expression
     *                  pattern.
     */
    public static String[] getKeys(IData document, Pattern pattern) {
        List<String> keys = getKeyList(document, pattern);
        return keys.toArray(new String[keys.size()]);
    }

    /**
     * Returns the keys that match the given regular expression pattern in the given IData document.
     *
     * @param document  An IData document.
     * @param pattern   A regular expression pattern which the returned set of keys must match.
     * @return          The list of keys present in the given IData document that match the given regular expression
     *                  pattern.
     */
    public static List<String> getKeyList(IData document, Pattern pattern) {
        List<String> keys = new ArrayList<String>(size(document));

        if (document != null) {
            IDataCursor cursor = document.getCursor();

            while(cursor.next()) {
                String key = cursor.getKey();
                if (pattern == null) {
                    keys.add(key);
                } else {
                    Matcher matcher = pattern.matcher(key);
                    if (matcher.matches()) keys.add(key);
                }
            }

            cursor.destroy();
        }

        return keys;
    }

    /**
     * Returns the top-level keys in the given IData document.
     *
     * @param document  An IData document.
     * @return          The list of top-level keys in the given IData document.
     */
    public static List<String> getKeyList(IData document) {
        List<String> keys = new ArrayList<String>(size(document));

        if (document != null) {
            IDataCursor cursor = document.getCursor();

            while(cursor.next()) {
                keys.add(cursor.getKey());
            }

            cursor.destroy();
        }

        return keys;
    }

    /**
     * Returns all the top-level values that are instances of the given class from the given document.
     *
     * @param document   An IData document.
     * @param valueClass The class that the returned values are instances of.
     * @return           The list of top-level values that are instances of the given class from the given IData
     *                   document.
     */
    public static <V> V[] getValues(IData document, Class<V> valueClass) {
        List<V> values = getValueList(document, valueClass);
        return values.toArray(ArrayHelper.instantiate(valueClass, values.size()));
    }

    /**
     * Returns all the top-level values from the given document.
     *
     * @param document  An IData document from which to return all values.
     * @return          The list of top-level values present in the given IData document.
     */
    public static Object[] getValues(IData document) {
        return ArrayHelper.normalize(getValueList(document));
    }

    /**
     * Returns all the top-level values that are instances of the given class from the given document.
     *
     * @param document   An IData document.
     * @param valueClass The class that the returned values are instances of.
     * @return           The list of top-level values that are instances of the given class from the given IData
     *                   document.
     */
    @SuppressWarnings("unchecked")
    public static <V> List<V> getValueList(IData document, Class<V> valueClass) {
        if (valueClass == null) throw new NullPointerException("valueClass must not be null");

        List<V> values = new ArrayList<V>(size(document));

        if (document != null) {
            IDataCursor cursor = document.getCursor();

            try {
                while (cursor.next()) {
                    Object value = cursor.getValue();
                    if (valueClass.isInstance(value)) {
                        values.add((V)value);
                    }
                }
            } finally {
                cursor.destroy();
            }
        }

        return values;
    }

    /**
     * Returns all the top-level values from the given document.
     *
     * @param document   An IData document.
     * @return           The list of top-level values from given IData document.
     */
    @SuppressWarnings("unchecked")
    public static List getValueList(IData document) {
        List values = new ArrayList(size(document));

        if (document != null) {
            IDataCursor cursor = document.getCursor();

            try {
                while (cursor.next()) {
                    values.add(cursor.getValue());
                }
            } finally {
                cursor.destroy();
            }
        }

        return values;
    }

    /**
     * Returns all the top-level values that are IData compatible objects, including elements in IData[] compatible
     * arrays, from the given IData document.
     *
     * @param document   An IData document.
     * @return           The list of top-level values that are IData compatible objects, including elements in IData[]
     *                   compatible arrays, from the given IData document
     */
    @SuppressWarnings("unchecked")
    public static List<IData> getIDataValueList(IData document) {
        List<IData> values = new ArrayList<IData>(size(document));

        if (document != null) {
            IDataCursor cursor = document.getCursor();

            try {
                while (cursor.next()) {
                    Object value = cursor.getValue();
                    if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                        values.addAll(Arrays.asList(toIDataArray(value)));
                    } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                        values.add(toIData(value));
                    }
                }
            } finally {
                cursor.destroy();
            }
        }

        return values;
    }

    /**
     * Returns all the top-level values that are IData documents or can be converted to IData documents from the given
     * IData document.
     *
     * @param document   An IData document.
     * @return           The list of top-level values that are IData documents or can be converted to IData documents
     *                   from the given IData document.
     */
    public static IData[] getIDataValues(IData document) {
        List<IData> values = getIDataValueList(document);
        return values.toArray(new IData[values.size()]);
    }

    /**
     * Returns all leaf values from the given document.
     *
     * @param document  The document to getLeafValues.
     * @return          All leaf values recursively collected from the given document and its children.
     */
    public static Object[] getLeafValues(IData document) {
        return getLeafValues(document, new Class[0]);
    }

    /**
     * Returns all leaf values that are instances of the given classes from the given document.
     *
     * @param document  The document to getLeafValues.
     * @param classes   List of classes the returned values must be instances of.
     * @return          All leaf values recursively collected from the given document and its children.
     */
    public static Object[] getLeafValues(IData document, Class... classes) {
        return ArrayHelper.normalize(getLeafValues(new ArrayList<Object>(), document, classes).toArray());
    }

    /**
     * Returns all leaf values from the given document list.
     *
     * @param array The document list to getLeafValues.
     * @return      All leaf values recursively collected from the given document list and its children.
     */
    public static Object[] getLeafValues(IData[] array) {
        return getLeafValues(array, new Class[0]);
    }

    /**
     * Returns all leaf values that are instances of the given classes from the given document list.
     *
     * @param array     The document list to getLeafValues.
     * @param classes   List of classes the returned values must be instances of.
     * @return          All leaf values recursively collected from the given document list and its children.
     */
    public static Object[] getLeafValues(IData[] array, Class... classes) {
        return ArrayHelper.normalize(getLeafValues(new ArrayList<Object>(), array, classes).toArray());
    }

    /**
     * Determine if IData leaves/children should be recursed.
     *
     * @param classes The list of classes the leaves are required to be instances of.
     * @return        True if IData leaves should be recursed.
     */
    private static boolean recurseIDataLeaves(Class ... classes) {
        boolean recurse = true;

        // if one of the requested classes is an IData compatible class, then we shouldn't recurse IData documents
        if (classes != null && classes.length > 0) {
            for (Class klass : classes) {
                if (klass != null && (klass == IData.class || klass == IDataCodable.class || klass == IDataPortable.class || klass == ValuesCodable.class)) {
                    recurse = false;
                    break;
                }
            }
        }

        return recurse;
    }


    /**
     * Returns all leaf values that are instances of the given classes from the given top-level IData.
     *
     * @param values    The list to add the flattened values to.
     * @param value     The IData to getLeafValues.
     * @param classes   List of classes the returned values must be instances of.
     * @return          The list of flattened values.
     */
    private static List<Object> getLeafValues(List<Object> values, IData value, Class... classes) {
        return getLeafValues(values, value, recurseIDataLeaves(classes), classes);
    }

    /**
     * Returns all leaf values that are instances of the given classes from the given IData[].
     *
     * @param values    The list to add the flattened values to.
     * @param value     The IData[] to getLeafValues.
     * @param classes   List of classes the returned values must be instances of.
     * @return          The list of flattened values.
     */
    private static List<Object> getLeafValues(List<Object> values, IData[] value, Class... classes) {
        return getLeafValues(values, value, recurseIDataLeaves(classes), classes);
    }

    /**
     * Returns all leaf values that are instances of the given classes from the given IData.
     *
     * @param values    The list to add the flattened values to.
     * @param value     The IData to getLeafValues.
     * @param recurse   If true, all IData objects will be recursed to construct the list of leaf values.
     * @param classes   List of classes the returned values must be instances of.
     * @return          The list of flattened values.
     */
    private static List<Object> getLeafValues(List<Object> values, IData value, boolean recurse, Class... classes) {
        for (Map.Entry<String, Object> entry : IDataMap.of(value)) {
            values = getLeafValues(values, entry.getValue(), recurse, classes);
        }

        return values;
    }

    /**
     * Returns all leaf values that are instances of the given classes from the given IData[].
     *
     * @param values    The list to add the flattened values to.
     * @param value     The IData[] to getLeafValues.
     * @param recurse   If true, all IData objects will be recursed to construct the list of leaf values.
     * @param classes   List of classes the returned values must be instances of.
     * @return          The list of flattened values.
     */
    private static List<Object> getLeafValues(List<Object> values, IData[] value, boolean recurse, Class... classes) {
        for (IData item : value) {
            values = getLeafValues(values, item, recurse, classes);
        }

        return values;
    }

    /**
     * Returns all leaf values that are instances of the given classes from the given Object[][].
     *
     * @param values    The list to add the flattened values to.
     * @param value     The Object[][] to getLeafValues.
     * @param recurse   If true, all IData objects will be recursed to construct the list of leaf values.
     * @param classes   List of classes the returned values must be instances of.
     * @return          The list of flattened values.
     */
    private static List<Object> getLeafValues(List<Object> values, Object[][] value, boolean recurse, Class... classes) {
        for (Object[] array : value) {
            values = getLeafValues(values, array, recurse, classes);
        }

        return values;
    }

    /**
     * Returns all leaf values that are instances of the given classes from the given Object[].
     *
     * @param values    The list to add the flattened values to.
     * @param value     The Object[] to getLeafValues.
     * @param recurse   If true, all IData objects will be recursed to construct the list of leaf values.
     * @param classes   List of classes the returned values must be instances of.
     * @return          The list of flattened values.
     */
    private static List<Object> getLeafValues(List<Object> values, Object[] value, boolean recurse, Class... classes) {
        for (Object item : value) {
            values = getLeafValues(values, item, recurse, classes);
        }

        return values;
    }

    /**
     * Returns all leaf values that are instances of the given classes from the given Object.
     *
     * @param values    The list to add the flattened values to.
     * @param value     The Object to getLeafValues.
     * @param recurse   If true, all IData objects will be recursed to construct the list of leaf values.
     * @param classes   List of classes the returned values must be instances of.
     * @return          The list of flattened values.
     */
    private static List<Object> getLeafValues(List<Object> values, Object value, boolean recurse, Class... classes) {
        if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
            values = getLeafValues(values, toIDataArray(value), recurse, classes);
        } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
            value = toIData(value);
            if (recurse) {
                values = getLeafValues(values, toIData(value), recurse, classes);
            } else {
                values.add(value);
            }
        } else if (value instanceof Object[][]) {
            values = getLeafValues(values, (Object[][])value, recurse, classes);
        } else if (value instanceof Object[]) {
            values = getLeafValues(values, (Object[])value, recurse, classes);
        } else {
            if (classes == null || classes.length == 0) {
                values.add(value);
            } else {
                for (Class klass : classes) {
                    if (klass != null && klass.isInstance(value)) {
                        values.add(value);
                        break;
                    }
                }
            }
        }

        return values;
    }

    /**
     * Merges multiple IData documents into a single new IData document.
     *
     * @param documents One or more IData documents to be merged.
     * @return          A new IData document containing the keys and values from all merged input documents.
     */
    public static IData merge(IData... documents) {
        return merge(documents == null ? null : Arrays.asList(documents));
    }

    /**
     * Merges multiple IData documents into a single new IData document.
     *
     * @param recurse   If true, a recursive merge is performed.
     * @param documents One or more IData documents to be merged.
     * @return          A new IData document containing the keys and values from all merged input documents.
     */
    public static IData merge(boolean recurse, IData... documents) {
        return merge(documents, recurse);
    }

    /**
     * Merges multiple IData documents into a single new IData document.
     *
     * @param documents One or more IData documents to be merged.
     * @param recurse   If true, a recursive merge is performed.
     * @return          A new IData document containing the keys and values from all merged input documents.
     */
    public static IData merge(IData[] documents, boolean recurse) {
        return merge(documents == null ? null : Arrays.asList(documents), recurse);
    }

    /**
     * Merges the top-level IData values from the given IData document.
     *
     * @param documents An IData document containing IData values to be merged.
     * @param recurse   If true, a recursive merge is performed.
     * @return          A new IData document containing the keys and values from all merged IData documents.
     */
    public static IData merge(IData documents, boolean recurse) {
        return merge(getIDataValueList(documents), recurse);
    }

    /**
     * Merges multiple IData documents into a single new IData document.
     *
     * @param documents One or more IData documents to be merged.
     * @return          A new IData document containing the keys and values from all merged input documents.
     */
    public static IData merge(Iterable<IData> documents) {
        IData output = IDataFactory.create();
        if (documents != null) {
            for (IData document : documents) {
                if (document != null) {
                    IDataUtil.merge(document, output);
                }
            }
        }
        return output;
    }

    /**
     * Merges multiple IData documents into a single new IData document.
     *
     * @param documents One or more IData documents to be merged.
     * @param recurse   If true, a recursive merge is performed.
     * @return          A new IData document containing the keys and values from all merged input documents.
     */
    public static IData merge(Iterable<IData> documents, boolean recurse) {
        IData output = IDataFactory.create();

        if (documents != null) {
            for (IData document : documents) {
                if (document != null) {
                    IDataCursor documentCursor = document.getCursor();
                    IDataCursor outputCursor = output.getCursor();

                    try {
                        while(documentCursor.next()) {
                            String key = documentCursor.getKey();
                            Object value = documentCursor.getValue();
                            Object existingValue = IDataUtil.get(outputCursor, key);

                            if (value != null) {
                                if (recurse &&
                                        (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) &&
                                        (existingValue instanceof IData || existingValue instanceof IDataCodable || existingValue instanceof IDataPortable || existingValue instanceof ValuesCodable)) {
                                    IDataUtil.put(outputCursor, key, merge(Arrays.asList(toIData(existingValue), toIData(value)), recurse));
                                } else {
                                    IDataUtil.put(outputCursor, key, value);
                                }
                            }
                        }
                    } finally {
                        documentCursor.destroy();
                        outputCursor.destroy();
                    }
                }
            }
        }

        return output;
    }

    /**
     * Returns the number of top-level key value pairs in the given IData document.
     *
     * @param document  An IData document.
     * @return          The number of key value pairs in the given IData document.
     */
    public static int size(IData document) {
        int size = 0;
        if (document != null) {
            IDataCursor cursor = document.getCursor();
            size = IDataUtil.size(cursor);
            cursor.destroy();
        }
        return size;
    }

    /**
     * Returns the number of occurrences of the given key in the given IData document.
     *
     * @param document  An IData document.
     * @param key       The key whose occurrences are to be counted.
     * @return          The number of occurrences of the given key in the given IData document.
     */
    public static int size(IData document, String key) {
        return size(document, key, false);
    }

    /**
     * Returns the number of occurrences of the given key in the given IData document.
     *
     * @param document  An IData document.
     * @param key       The key whose occurrences are to be counted.
     * @param literal   If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                  key.
     * @return          The number of occurrences of the given key in the given IData document.
     */
    public static int size(IData document, String key, boolean literal) {
        int size = 0;
        if (document != null && key != null) {
            IDataCursor cursor = document.getCursor();

            if (cursor.first(key)) {
                size++;
                while (cursor.next(key)) size++;
            } else if (IDataKey.isFullyQualified(key, literal)) {
                size = size(document, IDataKey.of(key, literal));
            }

            cursor.destroy();
        }
        return size;
    }

    /**
     * Returns the number of occurrences of the given fully-qualified key in the given IData document.
     *
     * @param document  An IData document.
     * @param key       The parsed fully-qualified key whose occurrences are to be counted.
     * @return          The number of occurrences of the given parsed fully-qualified key in the given IData document.
     */
    private static int size(IData document, IDataKey key) {
        int size = 0;
        if (document != null && key != null && key.size() > 0) {
            IDataCursor cursor = document.getCursor();
            IDataKey.Part keyPart = key.remove();

            if (key.size() > 0) {
                if (keyPart.hasArrayIndex()) {
                    size = size(ArrayHelper.get(toIDataArray(IDataUtil.get(cursor, keyPart.getKey())), keyPart.getIndex()), key);
                } else if (keyPart.hasKeyIndex()) {
                    size = size(toIData(get(document, keyPart.getKey(), keyPart.getIndex())), key);
                } else {
                    size = size(toIData(IDataUtil.get(cursor, keyPart.getKey())), key);
                }
            } else {
                if (keyPart.hasArrayIndex()) {
                    Object[] array = IDataUtil.getObjectArray(cursor, keyPart.getKey());
                    if (array != null && array.length > keyPart.getIndex()) {
                        size = 1;
                    }
                } else if (keyPart.hasKeyIndex()) {
                    size = size(document, keyPart.getKey(), keyPart.getIndex());
                } else {
                    while (cursor.next(keyPart.getKey())) size++;
                }
            }
            cursor.destroy();
        }
        return size;
    }

    /**
     * Returns the number of occurrences of the given nth key in the given IData document.
     *
     * @param document  An IData document.
     * @param key       The key whose occurrence is to be counted.
     * @param n         The nth occurrence to be counted.
     * @return          The number of occurrences of the given nth key in the given IData document.
     */
    private static int size(IData document, String key, int n) {
        int size = 0;

        if (document != null && key != null && n >= 0) {
            int i = 0;
            IDataCursor cursor = document.getCursor();
            while (cursor.next(key) && i++ < n) ;
            if (i > n) size = 1;
            cursor.destroy();
        }

        return size;
    }

    /**
     * Returns true if the given key exists in the given IData document.
     *
     * @param document  An IData document.
     * @param key       The key to check the existence of.
     * @return          True if the given key exists in the given IData document.
     */
    public static boolean exists(IData document, String key) {
        return exists(document, key, false);
    }

    /**
     * Returns true if the given key exists in the given IData document.
     *
     * @param document  An IData document.
     * @param key       The key to check the existence of.
     * @param literal   If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                  key.
     * @return          True if the given key exists in the given IData document.
     */
    public static boolean exists(IData document, String key, boolean literal) {
        return size(document, key, literal) > 0;
    }

    /**
     * Removes the given key from the given IData document, returning the associated value if one exists.
     *
     * @param document  The document to remove the key from.
     * @param key       The key to remove.
     * @return          The value that was associated with the given key.
     */
    public static Object remove(IData document, String key) {
        return remove(document, key, false);
    }

    /**
     * Removes the given key from the given IData document, returning the associated value if one exists.
     *
     * @param document  The document to remove the key from.
     * @param key       The key to remove.
     * @param literal   If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                  key.
     * @return          The value that was associated with the given key.
     */
    public static Object remove(IData document, String key, boolean literal) {
        Object value = get(document, key, literal);
        drop(document, key, literal);
        return value;
    }

    /**
     * Removes all occurrences of the given key from the given IData document, returning the associated values if there
     * were any.
     *
     * @param document  The document to remove the key from.
     * @param key       The key to remove.
     * @return          The values that were associated with the given key.
     */
    public static Object[] removeAll(IData document, String key) {
        return removeAll(document, key, false);
    }

    /**
     * Removes all occurrences of the given key from the given IData document, returning the associated values if there
     * were any.
     *
     * @param document  The document to remove the key from.
     * @param key       The key to remove.
     * @param literal   If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                  key.
     * @return          The values that were associated with the given key.
     */
    public static Object[] removeAll(IData document, String key, boolean literal) {
        Object[] value = getAsArray(document, key, literal);
        dropAll(document, key, literal);
        return value;
    }

    /**
     * Returns a recursive clone of the given IData document.
     *
     * @param document  An IData document to be duplicated.
     * @return          A new IData document which is a copy of the given IData document.
     */
    public static IData duplicate(IData document) {
        return duplicate(document, true);
    }

    /**
     * Returns a new IData document which is a copy of the given IData document.
     *
     * @param document  An IData document to be duplicated.
     * @param recurse   When true, nested IData documents and IData[] document lists will also be duplicated.
     * @return          A new IData document which is a copy of the given IData document.
     */
    public static IData duplicate(IData document, boolean recurse) {
        if (document == null) return null;

        IData output = IDataFactory.create();
        IDataCursor inputCursor = document.getCursor();
        IDataCursor outputCursor = output.getCursor();

        while(inputCursor.next()) {
            String key = inputCursor.getKey();
            Object value = inputCursor.getValue();

            if (recurse) {
                if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                    value = duplicate(toIDataArray(value), recurse);
                } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                    value = duplicate(toIData(value), recurse);
                }
            }

            outputCursor.insertAfter(key, value);
        }

        inputCursor.destroy();
        outputCursor.destroy();

        return output;
    }

    /**
     * Returns a new IData[] document list which is a copy of the given IData[] document list.
     *
     * @param array     An IData[] document list to be duplicated.
     * @param recurse   When true, nested IData documents and IData[] document lists will also be duplicated.
     * @return          A new IData[] document list which is a copy of the given IData[] document list.
     */
    public static IData[] duplicate(IData[] array, boolean recurse) {
        if (array == null) return null;

        IData[] output = new IData[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = duplicate(array[i], recurse);
        }

        return output;
    }

    /**
     * Removes the value with the given key from the given IData document.
     *
     * @param document  An IData document.
     * @param key       A simple or fully-qualified key identifying the value to be removed from the given IData
     *                  document.
     * @return          The given IData document.
     */
    public static IData drop(IData document, String key) {
        return drop(document, key, false);
    }

    /**
     * Removes the value with the given key from the given IData document.
     *
     * @param document An IData document.
     * @param key      A simple or fully-qualified key identifying the value to be removed from the given IData
     *                 document.
     * @param literal  If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                 key.
     * @return         The given IData document.
     */
    public static IData drop(IData document, String key, boolean literal) {
        if (document != null && key != null) {
            IDataCursor cursor = document.getCursor();

            if (cursor.first(key)) {
                cursor.delete();
            } else if (IDataKey.isFullyQualified(key, literal)) {
                drop(document, IDataKey.of(key, literal));
            }

            cursor.destroy();
        }
        return document;
    }

    /**
     * Removes the value with the given key from the given IData document.
     *
     * @param document An IData document.
     * @param key      A fully-qualified key identifying the value to be removed from the given IData document.
     * @return         The given IData document.
     */
    private static IData drop(IData document, IDataKey key) {
        if (document != null && key != null && key.size() > 0) {
            IDataCursor cursor = document.getCursor();
            IDataKey.Part keyPart = key.remove();

            if (key.size() > 0) {
                if (keyPart.hasArrayIndex()) {
                    drop(ArrayHelper.get(toIDataArray(IDataUtil.get(cursor, keyPart.getKey())), keyPart.getIndex()), key);
                } else if (keyPart.hasKeyIndex()) {
                    drop(toIData(get(document, keyPart.getKey(), keyPart.getIndex())), key);
                } else {
                    Object value = IDataUtil.get(cursor, keyPart.getKey());
                    IData[] array = toIDataArray(value);
                    if (array != null) {
                        // if we are referencing an IData[], drop the key from all items in the array
                        for (IData item : array) {
                            drop(item, key.clone());
                        }
                    } else {
                        drop(toIData(value), key);
                    }
                }
            } else {
                if (keyPart.hasArrayIndex()) {
                    IDataUtil.put(cursor, keyPart.getKey(), ArrayHelper.drop(IDataUtil.getObjectArray(cursor, keyPart.getKey()), keyPart.getIndex()));
                } else if (keyPart.hasKeyIndex()) {
                    drop(document, keyPart.getKey(), keyPart.getIndex());
                } else {
                    IDataUtil.remove(cursor, keyPart.getKey());
                }
            }
            cursor.destroy();
        }
        return document;
    }

    /**
     * Removes the element with the given nth key from the given IData document.
     *
     * @param document The IData document to remove the key value pair from.
     * @param key      The key to be removed.
     * @param n        Determines which occurrence of the key to remove.
     */
    private static void drop(IData document, String key, int n) {
        if (document == null || key == null || n < 0) return;

        int i = 0;

        IDataCursor cursor = document.getCursor();
        while (cursor.next(key) && i++ < n) ;
        if (i > n) cursor.delete();
        cursor.destroy();
    }

    /**
     * Removes all occurrences of the given key from the given IData document.
     *
     * @param document  The IData document to remove the key from.
     * @param key       The key to be removed.
     * @return          The given IData document, to allow for method chaining.
     */
    public static IData dropAll(IData document, String key) {
        return dropAll(document, key, false);
    }

    /**
     * Removes all occurrences of the given key from the given IData document.
     *
     * @param document  The IData document to remove the key from.
     * @param key       The key to be removed.
     * @param literal   If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                  key.
     * @return          The given IData document, to allow for method chaining.
     */
    public static IData dropAll(IData document, String key, boolean literal) {
        if (document != null && key != null) {
            IDataCursor cursor = document.getCursor();

            if (cursor.next(key)) {
                do {
                    cursor.delete();
                } while (cursor.next(key));
            } else if (IDataKey.isFullyQualified(key, literal)) {
                dropAll(document, IDataKey.of(key, literal));
            }

            cursor.destroy();
        }
        return document;
    }

    /**
     * Removes all occurrences of the given key from the given IData document.
     *
     * @param document  An IData document.
     * @param key       A fully-qualified key identifying the values to be removed from the given IData document.
     * @return          The given IData document.
     */
    private static IData dropAll(IData document, IDataKey key) {
        if (document != null && key != null && key.size() > 0) {
            IDataCursor cursor = document.getCursor();
            IDataKey.Part keyPart = key.remove();

            if (key.size() > 0) {
                if (keyPart.hasArrayIndex()) {
                    dropAll(ArrayHelper.get(toIDataArray(IDataUtil.get(cursor, keyPart.getKey())), keyPart.getIndex()), key);
                } else if (keyPart.hasKeyIndex()) {
                    dropAll(toIData(get(document, keyPart.getKey(), keyPart.getIndex())), key);
                } else {
                    dropAll(toIData(IDataUtil.get(cursor, keyPart.getKey())), key);
                }
            } else {
                if (keyPart.hasArrayIndex()) {
                    IDataUtil.put(cursor, keyPart.getKey(), ArrayHelper.drop(IDataUtil.getObjectArray(cursor, keyPart.getKey()), keyPart.getIndex()));
                } else if (keyPart.hasKeyIndex()) {
                    drop(document, keyPart.getKey(), keyPart.getIndex());
                } else {
                    while (cursor.next(keyPart.getKey())) {
                        cursor.delete();
                    }
                }
            }
            cursor.destroy();
        }
        return document;
    }

    /**
     * Renames a key from source to target within the given IData document.
     *
     * @param document  An IData document.
     * @param source    A simple or fully-qualified key identifying the value in the given IData document to be renamed.
     * @param target    The new simple or fully-qualified key for the renamed value.
     * @return          The given IData document.
     */
    public static IData rename(IData document, String source, String target) {
        return rename(document, source, target, false);
    }

    /**
     * Renames a key from source to target within the given IData document.
     *
     * @param document  An IData document.
     * @param source    A simple or fully-qualified key identifying the value in the given IData document to be renamed.
     * @param target    The new simple or fully-qualified key for the renamed value.
     * @param literal   If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                  key.
     * @return          The given IData document.
     */
    public static IData rename(IData document, String source, String target, boolean literal) {
        if (document != null && source != null && target != null && !source.equals(target)) {
            document = copy(document, source, target, literal);
            document = drop(document, source, literal);
        }
        return document;
    }

    /**
     * Copies a value from source key to target key within the given IData document.
     *
     * @param document  An IData document.
     * @param source    A simple or fully-qualified key identifying the value in the given IData document to be copied.
     * @param target    A simple or fully-qualified key the source value will be copied to.
     * @return          The given IData document.
     */
    public static IData copy(IData document, String source, String target) {
        return copy(document, source, target, false);
    }

    /**
     * Copies a value from source key to target key within the given IData document.
     *
     * @param document  An IData document.
     * @param source    A simple or fully-qualified key identifying the value in the given IData document to be copied.
     * @param target    A simple or fully-qualified key the source value will be copied to.
     * @param literal   If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                  key.
     * @return          The given IData document.
     */
    public static IData copy(IData document, String source, String target, boolean literal) {
        if (document != null && source != null && target != null && !source.equals(target)) {
            document = put(document, target, get(document, source, literal), literal);
        }
        return document;
    }

    /**
     * Amends the given IData document with the key value pairs specified in the amendments IData document.
     *
     * @param document          The IData document to be amended.
     * @param amendments        The list of key value pairs to amend the document with.
     * @param scope             The scope against which to resolve variable substitution statements.
     * @return                  The amended IData document.
     * @throws ServiceException If an error occurs.
     */
    public static IData amend(IData document, IData[] amendments, IData scope) throws ServiceException {
        if (amendments == null) return document;

        IData output = duplicate(document);

        for (int i = 0; i < amendments.length; i++) {
            if (amendments[i] != null) {
                IDataCursor cursor = amendments[i].getCursor();
                String key = IDataUtil.getString(cursor, "key");
                String value = IDataUtil.getString(cursor, "value");
                String condition = IDataUtil.getString(cursor, "condition");
                cursor.destroy();

                key = SubstitutionHelper.substitute(key, scope);
                value = SubstitutionHelper.substitute(value, scope);

                if ((condition == null) || ConditionEvaluator.evaluate(condition, scope)) {
                    output = IDataHelper.put(output, key, value);
                }
            }
        }

        return output;
    }

    /**
     * Trims all string values in the given IData of leading and trailing whitespace.
     *
     * @param document  The IData to be trimmed.
     * @return          A new IData containing trimmed versions of the elements in the given input.
     */
    public static IData trim(IData document) {
        return trim(document, true);
    }

    /**
     * Trims all string values in the given IData of leading and trailing whitespace.
     *
     * @param document  The IData to be trimmed.
     * @param recurse   Whether to recursively trim.
     * @return          A new IData containing trimmed versions of the elements in the given input.
     */
    public static IData trim(IData document, boolean recurse) {
        return trim(document, TransformerMode.VALUES, recurse);
    }

    /**
     * Trims all string keys and/or values in the given IData of leading and trailing whitespace.
     *
     * @param document  The IData to be trimmed.
     * @param mode      The transformer mode to use.
     * @param recurse   Whether to recursively trim.
     * @return          A new IData containing trimmed versions of the elements in the given input.
     */
    public static IData trim(IData document, TransformerMode mode, boolean recurse) {
        return transform(document, new Trimmer(mode, recurse, true, true, true));
    }

    /**
     * Trims all string values in the given IData[] of leading and trailing whitespace.
     *
     * @param input     The IData[] to be trimmed.
     * @return          A new IData[] containing trimmed versions of the elements in the given input.
     */
    public static IData[] trim(IData[] input) {
        return trim(input, true);
    }

    /**
     * Trims all string values in the given IData[] of leading and trailing whitespace.
     *
     * @param input     The IData[] to be trimmed.
     * @param recurse   Whether to recursively trim.
     * @return          A new IData[] containing trimmed versions of the elements in the given input.
     */
    public static IData[] trim(IData[] input, boolean recurse) {
        return trim(input, TransformerMode.VALUES, recurse);
    }

    /**
     * Trims all string keys and/or values in the given IData[] of leading and trailing whitespace.
     *
     * @param input     The IData[] to be trimmed.
     * @param mode      The transformer mode to use.
     * @param recurse   Whether to recursively trim.
     * @return          A new IData[] containing trimmed versions of the elements in the given input.
     */
    public static IData[] trim(IData[] input, TransformerMode mode, boolean recurse) {
        return transform(input, new Trimmer(mode, recurse, true, true, true));
    }

    /**
     * Replaces either the first or all occurrences of the given regular expression in the given IData document with the
     * given replacement.
     *
     * @param document    The IData document whose string values are to be replaced.
     * @param pattern     The regular expression pattern.
     * @param replacement The replacement string.
     * @param literal     Whether the replacement string is literal and therefore requires quoting.
     * @param firstOnly   If true, only the first occurrence is replaced, otherwise all occurrences are replaced.
     * @param recurse     Whether to recursively process the document.
     * @return            The document with replaced string values.
     */
    public static IData replace(IData document, String pattern, String replacement, boolean literal, boolean firstOnly, boolean recurse) {
        return replace(document, pattern == null ? null : Pattern.compile(pattern), replacement != null && literal ? Matcher.quoteReplacement(replacement) : replacement, firstOnly, recurse);
    }

    /**
     * Replaces either the first or all occurrences of the given regular expression in the given IData document with the
     * given replacement.
     *
     * @param document    The IData document whose string values are to be replaced.
     * @param pattern     The regular expression pattern.
     * @param replacement The replacement string.
     * @param firstOnly   If true, only the first occurrence is replaced, otherwise all occurrences are replaced.
     * @param recurse     Whether to recursively process the document.
     * @return            The document with replaced string values.
     */
    public static IData replace(IData document, Pattern pattern, String replacement, boolean firstOnly, boolean recurse) {
        return replace(document, pattern, replacement, firstOnly, TransformerMode.VALUES, recurse);
    }

    /**
     * Replaces either the first or all occurrences of the given regular expression in the given IData document with the
     * given replacement.
     *
     * @param document    The IData document whose string values are to be replaced.
     * @param pattern     The regular expression pattern.
     * @param replacement The replacement string.
     * @param firstOnly   If true, only the first occurrence is replaced, otherwise all occurrences are replaced.
     * @param mode        The transformer mode to use.
     * @param recurse     Whether to recursively process the document.
     * @return            The document with replaced string values.
     */
    public static IData replace(IData document, Pattern pattern, String replacement, boolean firstOnly, TransformerMode mode, boolean recurse) {
        return transform(document, new Replacer(mode, pattern, replacement, firstOnly, recurse));
    }

    /**
     * Replaces either the first or all occurrences of the given regular expression in the given IData[] document list
     * with the given replacement.
     *
     * @param array       The IData[] document list whose string values are to be replaced.
     * @param pattern     The regular expression pattern.
     * @param replacement The replacement string.
     * @param firstOnly   If true, only the first occurrence is replaced, otherwise all occurrences are replaced.
     * @param recurse     Whether to recursively process the document list.
     * @return            The document list with replaced string values.
     */
    public static IData[] replace(IData[] array, String pattern, String replacement, boolean literal, boolean firstOnly, boolean recurse) {
        return replace(array, pattern == null ? null : Pattern.compile(pattern), replacement != null && literal ? Matcher.quoteReplacement(replacement) : replacement, firstOnly, recurse);
    }

    /**
     * Replaces either the first or all occurrences of the given regular expression in the given IData[] document list
     * with the given replacement.
     *
     * @param array       The IData[] document list whose string values are to be replaced.
     * @param pattern     The regular expression pattern.
     * @param replacement The replacement string.
     * @param firstOnly   If true, only the first occurrence is replaced, otherwise all occurrences are replaced.
     * @param recurse     Whether to recursively process the document list.
     * @return            The document list with replaced string values.
     */
    public static IData[] replace(IData[] array, Pattern pattern, String replacement, boolean firstOnly, boolean recurse) {
        return replace(array, pattern, replacement, firstOnly, TransformerMode.VALUES, recurse);
    }

    /**
     * Replaces either the first or all occurrences of the given regular expression in the given IData[] document list
     * with the given replacement.
     *
     * @param array       The IData[] document list whose string values are to be replaced.
     * @param pattern     The regular expression pattern.
     * @param replacement The replacement string.
     * @param firstOnly   If true, only the first occurrence is replaced, otherwise all occurrences are replaced.
     * @param mode        The transformer mode to use.
     * @param recurse     Whether to recursively process the document list.
     * @return            The document list with replaced string values.
     */
    public static IData[] replace(IData[] array, Pattern pattern, String replacement, boolean firstOnly, TransformerMode mode, boolean recurse) {
        return transform(array, new Replacer(mode, pattern, replacement, firstOnly, recurse));
    }

    /**
     * Trims all string values, then converts empty strings to nulls, then compacts by removing all null values.
     *
     * @param document  An IData document to be squeezed.
     * @return          A new IData document that is the given IData squeezed.
     */
    public static IData squeeze(IData document) {
        return squeeze(document, true);
    }

    /**
     * Trims all string values, then converts empty strings to nulls, then compacts by removing all null values.
     *
     * @param document  An IData document to be squeezed.
     * @param recurse   Whether to also squeeze embedded IData and IData[] objects.
     * @return          A new IData document that is the given IData squeezed.
     */
    public static IData squeeze(IData document, boolean recurse) {
        return transform(document, new Squeezer(recurse));
    }

    /**
     * Returns a new IData[] with all empty and null values removed.
     *
     * @param array     An IData[] to be squeezed.
     * @return          A new IData[] that is the given IData[] squeezed.
     */
    public static IData[] squeeze(IData[] array) {
        return squeeze(array, true);
    }

    /**
     * Returns a new IData[] with all empty and null values removed.
     *
     * @param array     An IData[] to be squeezed.
     * @param recurse   Whether to also squeeze embedded IData and IData[] objects.
     * @return          A new IData[] that is the given IData[] squeezed.
     */
    public static IData[] squeeze(IData[] array, boolean recurse) {
        return transform(array, new Squeezer(recurse));
    }

    /**
     * Converts all strings that only contain whitespace characters to null.
     *
     * @param document An IData document to be nullified.
     * @return         A new IData document that is the given IData nullified.
     */
    public static IData nullify(IData document) {
        return nullify(document, true);
    }

    /**
     * Converts all strings that only contain whitespace characters to null.
     *
     * @param document An IData document to be nullified.
     * @param recurse  Whether to also nullify embedded IData and IData[] objects.
     * @return         A new IData document that is the given IData nullified.
     */
    public static IData nullify(IData document, boolean recurse) {
        return transform(document, new Nullifier(recurse));
    }

    /**
     * Converts all strings that only contain whitespace characters to null.
     *
     * @param input   An IData[] to be nullified.
     * @return        A new IData[] that is the given IData[] nullify.
     */
    public static IData[] nullify(IData[] input) {
        return nullify(input, true);
    }

    /**
     * Converts all strings that only contain whitespace characters to null.
     *
     * @param input   An IData[] to be nullified.
     * @param recurse Whether to also nullify embedded IData and IData[] objects.
     * @return        A new IData[] that is the given IData[] nullify.
     */
    public static IData[] nullify(IData[] input, boolean recurse) {
        return transform(input, new Nullifier(recurse));
    }

    /**
     * Converts all null values to empty strings.
     *
     * @param document  The IData document to blankify.
     * @param recurse   Whether embedded IData and IData[] objects should be recursively blankified.
     * @return          The blankified IData.
     */
    public static IData blankify(IData document, boolean recurse) {
        return transform(document, new Blankifier(recurse));
    }

    /**
     * Converts all null values to empty strings.
     *
     * @param array     The IData[] to blankify.
     * @param recurse   Whether embedded IData and IData[] objects should be recursively blankified.
     * @return          The blankified IData[].
     */
    public static IData[] blankify(IData[] array, boolean recurse) {
        return transform(array, new Blankifier(recurse));
    }

    /**
     * Converts all non-string values to strings, except for IData and IData[] compatible objects.
     *
     * @param document  The IData document to stringify.
     * @return          The stringified IData document.
     */
    public static IData stringify(IData document) {
        return stringify(document, true);
    }

    /**
     * Converts all non-string values to strings, except for IData and IData[] compatible objects.
     *
     * @param document  The IData document to stringify.
     * @param recurse   Whether embedded IData and IData[] objects should also be stringified recursively.
     * @return          The stringified IData document.
     */
    public static IData stringify(IData document, boolean recurse) {
        return transform(document, new Stringifier(recurse));
    }

    /**
     * Converts all non-string values to strings, except for IData and IData[] compatible objects.
     *
     * @param array     The IData[] to stringify.
     * @return          The stringified IData[].
     */
    public static IData[] stringify(IData[] array) {
        return stringify(array, true);
    }

    /**
     * Converts all non-string values to strings, except for IData and IData[] compatible objects.
     *
     * @param array     The IData[] to stringify.
     * @param recurse   Whether to stringify embedded IData and IData[] objects recursively.
     * @return          The stringified IData[].
     */
    public static IData[] stringify(IData[] array, boolean recurse) {
        return transform(array, new Stringifier(recurse));
    }

    /**
     * Transforms the given IData document using the given transformer.
     *
     * @param document      The IData document to be transformed.
     * @param transformer   The Transformer object to use to transform the given IData document.
     * @return              The resulting transformed IData document.
     */
    public static IData transform(IData document, Transformer transformer) {
        return transformer.transform(document);
    }

    /**
     * Transforms the given IData[] document list using the given transformer.
     *
     * @param array         The IData[] document list to be transformed.
     * @param transformer   The Transformer object to use to transform the given IData[] document list.
     * @return              The resulting transformed IData[] document list.
     */
    public static IData[] transform(IData[] array, Transformer transformer) {
        return transformer.transform(array);
    }

    /**
     * Returns a string created by concatenating each element of the given IData document.
     *
     * @param document  The IData document to be converted to a string.
     * @return          A string representation of the given IData document.
     */
    public static String join(IData document) {
        return join(document, null, null, null);
    }

    /**
     * Returns a string created by concatenating each element of the given IData document, separated by the given
     * separator strings.
     *
     * @param document          The IData document to be converted to a string.
     * @param itemSeparator     The string to use to delimit entries in IData documents.
     * @param listSeparator     The string to use to delimit list items.
     * @param valueSeparator    The string to use to delimit key value pairs.
     * @return                  A string representation of the given IData document.
     */
    public static String join(IData document, String itemSeparator, String listSeparator, String valueSeparator) {
        return join(document, itemSeparator, listSeparator, valueSeparator, (Sanitization)null);
    }

    /**
     * Returns a string created by concatenating each element of the given IData document, separated by the given
     * separator strings.
     *
     * @param document          The IData document to be converted to a string.
     * @param itemSeparator     The string to use to delimit entries in IData documents.
     * @param listSeparator     The string to use to delimit list items.
     * @param valueSeparator    The string to use to delimit key value pairs.
     * @param sanitization      The type of sanitization required, if any.
     * @return                  A string representation of the given IData document.
     */
    public static String join(IData document, String itemSeparator, String listSeparator, String valueSeparator, Sanitization sanitization) {
        document = sanitize(document, sanitization);

        if (document == null || size(document) == 0) return null;

        StringBuilder builder = new StringBuilder();
        join(document, itemSeparator, listSeparator, valueSeparator, builder);
        return builder.toString();
    }

    /**
     * Returns a string created by concatenating each element of the given IData document, separated by the given
     * separator strings.
     *
     * @param document          The IData document to be converted to a string.
     * @param itemSeparator     The string to use to delimit entries in IData documents.
     * @param listSeparator     The string to use to delimit list items.
     * @param valueSeparator    The string to use to delimit key value pairs.
     * @param builder           The string builder to use when building the output.
     */
    private static void join(IData document, String itemSeparator, String listSeparator, String valueSeparator, StringBuilder builder) {
        if (document == null || builder == null) return;

        if (itemSeparator == null) itemSeparator = ", ";
        if (listSeparator == null) listSeparator = ", ";
        if (valueSeparator == null) valueSeparator = ": ";

        boolean itemSeparatorRequired = false;

        IDataCursor cursor = document.getCursor();

        while (cursor.next()) {
            String key = cursor.getKey();
            Object value = cursor.getValue();

            if (itemSeparatorRequired) builder.append(itemSeparator);

            builder.append(key);
            builder.append(valueSeparator);

            if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                builder.append("[");
                join(toIDataArray(value), itemSeparator, listSeparator, valueSeparator, builder);
                builder.append("]");
            } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                builder.append("{");
                join(toIData(value), itemSeparator, listSeparator, valueSeparator, builder);
                builder.append("}");
            } else if (value instanceof Object[][]) {
                TableHelper.stringify(TableHelper.toStringTable((Object[][])value), listSeparator, listSeparator, builder);
            } else if (value instanceof Object[]) {
                ArrayHelper.stringify(ArrayHelper.toStringArray((Object[])value), listSeparator, builder);
            } else {
                builder.append(ObjectHelper.stringify(value));
            }

            itemSeparatorRequired = true;
        }

        cursor.destroy();
    }

    /**
     * Returns a string created by concatenating each element of the given IData[] document list.
     *
     * @param array The IData[] document list to be converted to a string.
     * @return      A string representation of the given IData document.
     */
    public static String join(IData[] array) {
        return join(array, null, null, null);
    }

    /**
     * Returns a string created by concatenating each element of the given IData[] document list, separated by the given
     * separator strings.
     *
     * @param array             The IData[] document list to be converted to a string.
     * @param itemSeparator     The string to use to delimit entries in IData documents.
     * @param listSeparator     The string to use to delimit list items.
     * @param valueSeparator    The string to use to delimit key value pairs.
     * @return                  A string representation of the given IData document.
     */
    public static String join(IData[] array, String itemSeparator, String listSeparator, String valueSeparator) {
        return join(array, itemSeparator, listSeparator, valueSeparator, (Sanitization)null);
    }

    /**
     * Returns a string created by concatenating each element of the given IData[] document list, separated by the given
     * separator strings.
     *
     * @param array             The IData[] document list to be converted to a string.
     * @param itemSeparator     The string to use to delimit entries in IData documents.
     * @param listSeparator     The string to use to delimit list items.
     * @param valueSeparator    The string to use to delimit key value pairs.
     * @param sanitization      The type of sanitization required, if any.
     * @return                  A string representation of the given IData document.
     */
    public static String join(IData[] array, String itemSeparator, String listSeparator, String valueSeparator, Sanitization sanitization) {
        array = sanitize(array, sanitization);

        if (array == null || array.length == 0) return null;

        StringBuilder builder = new StringBuilder();
        join(array, itemSeparator, listSeparator, valueSeparator, builder);
        return builder.toString();
    }

    /**
     * Returns a string created by concatenating each element of the given IData[] document list, separated by the given
     * separator strings.
     *
     * @param array             The IData[] document list to be converted to a string.
     * @param itemSeparator     The string to use to delimit entries in IData documents.
     * @param listSeparator     The string to use to delimit list items.
     * @param valueSeparator    The string to use to delimit key value pairs.
     * @param builder           The string builder to use when building the output.
     */
    public static void join(IData[] array, String itemSeparator, String listSeparator, String valueSeparator, StringBuilder builder) {
        if (array == null || builder == null) return;

        if (itemSeparator == null) itemSeparator = ", ";
        if (listSeparator == null) listSeparator = ", ";
        if (valueSeparator == null) valueSeparator = ": ";

        for(int i = 0; i < array.length; i++) {
            if (i > 0) builder.append(listSeparator);

            builder.append("{");
            join(array[i], itemSeparator, listSeparator, valueSeparator, builder);
            builder.append("}");
        }
    }

    /**
     * Converts the value associated with the given key to an array in the given IData document.
     *
     * @param document  An IData document.
     * @param key       The key whose associated value is to be converted to an array.
     * @return          The given IData with the given key's value converted to an array.
     */
    public static IData arrayify(IData document, String key) {
        if (exists(document, key)) {
            Object[] value = getAsArray(document, key);
            dropAll(document, key);
            put(document, key, value);
        }
        return document;
    }

    /**
     * Sanitizes the given IData document by removing nulls, or removing nulls and blanks.
     *
     * @param document          The IData document to be sanitized.
     * @param sanitization      The type of sanitization required, if any.
     * @return                  The sanitized IData document.
     */
    public static IData sanitize(IData document, Sanitization sanitization) {
        if (document != null && sanitization != null) {
            if (sanitization == Sanitization.REMOVE_NULLS) {
                document = compact(document);
            } else if (sanitization == Sanitization.REMOVE_NULLS_AND_BLANKS) {
                document = squeeze(document);
            }
        }

        return document;
    }

    /**
     * Removes all null values from the given IData document.
     *
     * @param document  The IData document to be compacted.
     * @return          The compacted IData.
     */
    public static IData compact(IData document) {
        return compact(document, true);
    }

    /**
     * Removes all null values from the given IData document.
     *
     * @param document  The IData document to be compacted.
     * @param recurse   Whether embedded IData and IData[] objects should be recursively compacted.
     * @return          The compacted IData.
     */
    public static IData compact(IData document, boolean recurse) {
        if (document == null) return null;

        IData output = IDataFactory.create();
        IDataCursor inputCursor = document.getCursor();
        IDataCursor outputCursor = output.getCursor();

        while (inputCursor.next()) {
            String key = inputCursor.getKey();
            Object value = inputCursor.getValue();

            if (value != null) {
                if (recurse) {
                    if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                        value = compact(toIDataArray(value), recurse);
                    } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                        value = compact(toIData(value), recurse);
                    } else if (value instanceof Object[][]) {
                        value = TableHelper.compact((Object[][])value);
                    } else if (value instanceof Object[]) {
                        value = ArrayHelper.compact((Object[])value);
                    }
                }
            }

            if (value != null) IDataUtil.put(outputCursor, key, value);
        }
        inputCursor.destroy();
        outputCursor.destroy();

        return output;
    }

    /**
     * Sanitizes the given IData[] by removing nulls, or removing nulls and blanks.
     *
     * @param array             The IData[] to be sanitized.
     * @param sanitization      The type of sanitization required.
     * @return                  The sanitized IData[].
     */
    public static IData[] sanitize(IData[] array, Sanitization sanitization) {
        if (array != null && sanitization != null) {
            if (sanitization == Sanitization.REMOVE_NULLS) {
                array = compact(array);
            } else if (sanitization == Sanitization.REMOVE_NULLS_AND_BLANKS) {
                array = squeeze(array);
            }
        }

        return array;
    }

    /**
     * Removes all null values from the given IData[].
     *
     * @param array     The IData[] to be compacted.
     * @return          The compacted IData[].
     */
    public static IData[] compact(IData[] array) {
        return compact(array, true);
    }

    /**
     * Removes all null values from the given IData[].
     *
     * @param array     The IData[] to be compacted.
     * @param recurse   Whether embedded IData and IData[] objects should be recursively compacted.
     * @return          The compacted IData[].
     */
    public static IData[] compact(IData[] array, boolean recurse) {
        if (array == null) return null;

        IData[] output = new IData[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = compact(array[i], recurse);
        }

        return ArrayHelper.compact(output);
    }

    /**
     * Normalizes the given Object.
     *
     * @param value An Object to be normalized.
     * @return      A new normalized version of the given Object.
     */
    private static Object normalize(Object value) {
        if (value instanceof Table) {
            value = normalize((Table)value);
        } else if (value instanceof IDataCodable[]) {
            value = normalize((IDataCodable[])value);
        } else if (value instanceof IDataPortable[]) {
            value = normalize((IDataPortable[])value);
        } else if (value instanceof ValuesCodable[]) {
            value = normalize((ValuesCodable[])value);
        } else if (value instanceof Collection) {
            value = normalize((Collection)value);
        } else if (value instanceof Map[]) {
            value = normalize((Map[]) value);
        } else if (value instanceof IData[]) {
            value = normalize((IData[])value);
        } else if (value instanceof IDataCodable) {
            value = normalize((IDataCodable)value);
        } else if (value instanceof IDataPortable) {
            value = normalize((IDataPortable)value);
        } else if (value instanceof ValuesCodable) {
            value = normalize((ValuesCodable)value);
        } else if (value instanceof Map) {
            value = normalize((Map)value);
        } else if (value instanceof IData) {
            value = normalize((IData)value);
        }

        return value;
    }

    /**
     * Normalizes the given Object[].
     *
     * @param array The Object[] to be normalized.
     * @return      Normalized version of the Object[].
     */
    private static Object[] normalize(Object[] array) {
        return (Object[])normalize((Object)ArrayHelper.normalize(array));
    }

    /**
     * Returns a new IData document, where all nested IData and IData[] objects are implemented with the same class, and
     * all fully-qualified keys are replaced with their representative nested structure.
     *
     * @param document  An IData document to be normalized.
     * @return          A new normalized version of the given IData document.
     */
    public static IData normalize(IData document) {
        if (document == null) return null;

        // support normalizing TN FixedData objects such as document types, which are both IData and IDataCodable
        if (document instanceof IDataCodable) {
            document = normalize((IDataCodable)document);
        } else if (document instanceof ValuesCodable) {
            document = normalize((ValuesCodable)document);
        } else if (document instanceof IDataPortable) {
            document = normalize((IDataPortable)document);
        }

        IData output = IDataFactory.create();
        IDataCursor inputCursor = document.getCursor();
        IDataCursor outputCursor = output.getCursor();
        boolean outputCursorDirty = false;

        while(inputCursor.next()) {
            String key = inputCursor.getKey();
            Object value = normalize(inputCursor.getValue());

            if (IDataKey.isFullyQualified(key)) {
                // normalize fully-qualified keys by using IDataHelper.put() rather than IDataUtil.put()
                put(output, key, value);
                outputCursorDirty = true;
            } else {
                // reuse cursor unless it has been marked dirty by a fully-qualified put
                if (outputCursorDirty) {
                    outputCursor.destroy();
                    outputCursor = output.getCursor();
                    outputCursor.last();
                    outputCursorDirty = false;
                }

                // support multiple occurrences of same key by using IDataCursor.insertAfter()
                outputCursor.insertAfter(key, value);
            }
        }

        inputCursor.destroy();
        outputCursor.destroy();

        return output;
    }

    /**
     * Converts a java.util.Map to an IData object.
     *
     * @param map   A java.util.Map to be converted to an IData object.
     * @return      An IData representation of the given java.util.Map object.
     */
    private static IData normalize(Map map) {
        return normalize(toIData(map));
    }

    /**
     * Normalizes a java.util.Collection to an Object[].
     *
     * @param collection    A java.util.Collection to be converted to an Object[].
     * @return              An Object[] representation of the given java.util.Collection object.
     */
    private static Object[] normalize(Collection collection) {
        return normalize(ArrayHelper.toArray(collection));
    }

    /**
     * Normalizes an IDataCodable object to an IData representation.
     *
     * @param document  An IDataCodable object to be normalized.
     * @return          An IData representation for the given IDataCodable object.
     */
    public static IData normalize(IDataCodable document) {
        return normalize(toIData(document));
    }

    /**
     * Normalizes an IDataCodable[] where all items are converted to IData documents implemented with the same class,
     * and all fully-qualified keys are replaced with their representative nested structure.
     *
     * @param array An IDataCodable[] list to be normalized.
     * @return      A new normalized IData[] version of the given IDataCodable[] list.
     */
    public static IData[] normalize(IDataCodable[] array) {
        return normalize(toIDataArray(array));
    }

    /**
     * Normalizes an IDataPortable object to an IData representation.
     *
     * @param document  An IDataPortable object to be normalized.
     * @return          An IData representation for the given IDataPortable object.
     */
    public static IData normalize(IDataPortable document) {
        return normalize(toIData(document));
    }

    /**
     * Normalizes an IDataPortable[] where all items are converted to IData documents implemented with the same class,
     * and all fully-qualified keys are replaced with their representative nested structure.
     *
     * @param array An IDataPortable[] list to be normalized.
     * @return      A new normalized IData[] version of the given IDataPortable[] list.
     */
    public static IData[] normalize(IDataPortable[] array) {
        return normalize(toIDataArray(array));
    }

    /**
     * Normalizes an ValuesCodable object to an IData representation.
     *
     * @param document  An ValuesCodable object to be normalized.
     * @return          An IData representation for the given ValuesCodable object.
     */
    public static IData normalize(ValuesCodable document) {
        return normalize(toIData(document));
    }

    /**
     * Normalizes an ValuesCodable[] where all items are converted to IData documents implemented with the same class,
     * and all fully-qualified keys are replaced with their representative nested structure.
     *
     * @param array An ValuesCodable[] list to be normalized.
     * @return      A new normalized IData[] version of the given ValuesCodable[] list.
     */
    public static IData[] normalize(ValuesCodable[] array) {
        return normalize(toIDataArray(array));
    }

    /**
     * Normalizes an IData[] where all IData objects are implemented with the same class, and all fully-qualified keys
     * are replaced with their representative nested structure.
     *
     * @param array An IData[] document list to be normalized.
     * @return      A new normalized version of the given IData[] document list.
     */
    public static IData[] normalize(IData[] array) {
        if (array == null) return null;

        IData[] output = new IData[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = normalize(array[i]);
        }

        return output;
    }

    /**
     * Normalizes a com.wm.util.Table object to an IData[] representation.
     *
     * @param table A com.wm.util.Table object to be normalized.
     * @return      An IData[] representation of the given com.wm.util.Table object.
     */
    public static IData[] normalize(Table table) {
        return normalize(toIDataArray(table));
    }

    /**
     * Normalizes a Map[] object to an IData[] representation.
     *
     * @param array A Map[] object to be normalized.
     * @return      An IData[] representation of the given Map[] object.
     */
    public static IData[] normalize(Map[] array) {
        return normalize(toIDataArray(array));
    }

    /**
     * Removes all key value pairs from the given IData document.
     *
     * @param document An IData document to be cleared.
     */
    public static void clear(IData document) {
        clear(document, (String)null);
    }

    /**
     * Removes all key value pairs from the given IData document except those with a specified key.
     *
     * @param document          An IData document to be cleared.
     * @param keysToBePreserved List of simple or fully-qualified keys identifying items that should not be removed.
     */
    public static void clear(IData document, String... keysToBePreserved) {
        if (document == null) return;

        IData saved = IDataFactory.create();
        if (keysToBePreserved != null) {
            for (String key : keysToBePreserved) {
                if (key != null) put(saved, key, get(document, key), false, false);
            }
        }

        IDataCursor cursor = document.getCursor();
        cursor.first();
        while (cursor.delete());
        cursor.destroy();

        if (keysToBePreserved != null) IDataUtil.merge(saved, document);
    }

    /**
     * Returns the value associated with the given key as a one-dimensional array; if the value is a
     * multi-dimensional array it is first flattened.
     *
     * @param document      The IData document which contains the values to be flattened.
     * @param keys          One or more fully-qualified keys identifying the values to be flattened.
     * @return              A one-dimensional flattened array containing the values associated with the given keys.
     */
    public static Object[] flatten(IData document, String... keys) {
        return flatten(document, false, keys);
    }

    /**
     * Returns the value associated with the given key as a one-dimensional array; if the value is a
     * multi-dimensional array it is first flattened.
     *
     * @param document      The IData document which contains the values to be flattened.
     * @param includeNulls  If true null values will be included in the returned array.
     * @param keys          One or more fully-qualified keys identifying the values to be flattened.
     * @return              A one-dimensional flattened array containing the values associated with the given keys.
     */
    public static Object[] flatten(IData document, boolean includeNulls, String... keys) {
        if (document == null || keys == null) return null;

        ArrayList<Object> list = new ArrayList<Object>();

        for (String key : keys) {
            Object value = get(document, key);
            if (value instanceof Object[]) {
                ArrayHelper.flatten((Object[])value, list, includeNulls);
            } else if (includeNulls || value != null) {
                list.add(value);
            }
        }

        return list.size() == 0 ? null : ArrayHelper.normalize(list);
    }

    /**
     * Returns the value associated with the given key from the given IData document.
     *
     * @param document  An IData document.
     * @param key       A simple or fully-qualified key identifying the value in the given IData document to be
     *                  returned.
     * @return          The value associated with the given key in the given IData document.
     */
    public static Object get(IData document, String key) {
        return get(null, document, key, Object.class);
    }

    /**
     * Returns the value associated with the given key from the given IData document.
     *
     * @param document  An IData document.
     * @param key       A simple or fully-qualified key identifying the value in the given IData document to be
     *                  returned.
     * @param klass     The class of the value to be returned.
     * @param <T>       The type of value to be returned.
     * @return          The value associated with the given key in the given IData document.
     */
    public static <T> T get(IData document, String key, Class<T> klass) {
        return get(null, document, key, klass);
    }

    /**
     * Returns the value associated with the given key from the given scope (if relative) or pipeline (if absolute).
     *
     * @param pipeline The pipeline, required if the key is an absolute path.
     * @param scope    An IData document used to scope the key if it is relative.
     * @param key      A simple or fully-qualified key identifying the value in the given IData document to be
     *                 returned.
     * @return         The value associated with the given key in the given IData document.
     */
    public static Object get(IData pipeline, IData scope, String key) {
        return get(pipeline, scope, key, false, Object.class);
    }

    /**
     * Returns the value associated with the given key from the given scope (if relative) or pipeline (if absolute).
     *
     * @param pipeline The pipeline, required if the key is an absolute path.
     * @param scope    An IData document used to scope the key if it is relative.
     * @param key      A simple or fully-qualified key identifying the value in the given IData document to be
     *                 returned.
     * @param klass    The class of the value to be returned.
     * @param <T>      The type of value to be returned.
     * @return         The value associated with the given key in the given IData document.
     */
    public static <T> T get(IData pipeline, IData scope, String key, Class<T> klass) {
        return get(pipeline, scope, key, false, klass);
    }

    /**
     * Returns the value associated with the given key from the given IData document.
     *
     * @param document An IData document.
     * @param key      A simple or fully-qualified key identifying the value in the given IData document to be
     *                 returned.
     * @param literal  If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                 key.
     * @return         The value associated with the given key in the given IData document.
     */
    public static Object get(IData document, String key, boolean literal) {
        return get(null, document, key, literal, Object.class);
    }

    /**
     * Returns the value associated with the given key from the given IData document.
     *
     * @param document An IData document.
     * @param key      A simple or fully-qualified key identifying the value in the given IData document to be
     *                 returned.
     * @param literal  If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                 key.
     * @param klass    The class of the value to be returned.
     * @param <T>      The type of value to be returned.
     * @return         The value associated with the given key in the given IData document.
     */
    public static <T> T get(IData document, String key, boolean literal, Class<T> klass) {
        return get(null, document, key, literal, klass);
    }

    /**
     * Returns the value associated with the given key from the given IData document, or if null the specified default
     * value.
     *
     * @param document      An IData document.
     * @param key           A simple or fully-qualified key identifying the value in the given IData document to be
     *                      returned.
     * @param defaultValue  A default value to be returned if the existing value associated with the given key is null.
     * @return              Either the value associated with the given key in the given IData document, or the given
     *                      defaultValue if null.
     */
    public static Object get(IData document, String key, Object defaultValue) {
        return get(document, key, defaultValue, false, Object.class);
    }

    /**
     * Returns the value associated with the given key from the given IData document, or if null the specified default
     * value.
     *
     * @param document      An IData document.
     * @param key           A simple or fully-qualified key identifying the value in the given IData document to be
     *                      returned.
     * @param defaultValue  A default value to be returned if the existing value associated with the given key is null.
     * @param klass         The class of the value to be returned.
     * @param <T>           The type of value to be returned.
     * @return              Either the value associated with the given key in the given IData document, or the given
     *                      defaultValue if null.
     */
    public static <T> T get(IData document, String key, T defaultValue, Class<T> klass) {
        return get(document, key, defaultValue, false, klass);
    }

    /**
     * Returns the value associated with the given key from the given scope (if relative) or pipeline (if absolute).
     *
     * @param pipeline      The pipeline, required if the key is an absolute path.
     * @param scope         An IData document used to scope the key if it is relative.
     * @param key           A simple or fully-qualified key identifying the value in the given IData document to be
     *                      returned.
     * @param defaultValue  A default value to be returned if the existing value associated with the given key is null.
     * @return              Either the value associated with the given key in the given IData document, or the given
     *                      defaultValue if null.
     */
    public static Object get(IData pipeline, IData scope, String key, Object defaultValue) {
        return get(pipeline, scope, key, defaultValue, false, Object.class);
    }

    /**
     * Returns the value associated with the given key from the given scope (if relative) or pipeline (if absolute).
     *
     * @param pipeline      The pipeline, required if the key is an absolute path.
     * @param scope         An IData document used to scope the key if it is relative.
     * @param key           A simple or fully-qualified key identifying the value in the given IData document to be
     *                      returned.
     * @param defaultValue  A default value to be returned if the existing value associated with the given key is null.
     * @param klass         The class of the value to be returned.
     * @param <T>           The type of value to be returned.
     * @return              Either the value associated with the given key in the given IData document, or the given
     *                      defaultValue if null.
     */
    public static <T> T get(IData pipeline, IData scope, String key, T defaultValue, Class<T> klass) {
        return get(pipeline, scope, key, defaultValue, false, klass);
    }

    /**
     * Returns the value associated with the given key from the given IData document, or if null the specified default
     * value.
     *
     * @param document      An IData document.
     * @param key           A simple or fully-qualified key identifying the value in the given IData document to be
     *                      returned.
     * @param defaultValue  A default value to be returned if the existing value associated with the given key is null.
     * @param literal       If true, the key will be treated as a literal key, rather than potentially as a
     *                      fully-qualified key.
     * @return              Either the value associated with the given key in the given IData document, or the given
     *                      defaultValue if null.
     */
    public static Object get(IData document, String key, Object defaultValue, boolean literal) {
        return get(null, document, key, defaultValue, literal, Object.class);
    }

    /**
     * Returns the value associated with the given key from the given IData document, or if null the specified default
     * value.
     *
     * @param document      An IData document.
     * @param key           A simple or fully-qualified key identifying the value in the given IData document to be
     *                      returned.
     * @param defaultValue  A default value to be returned if the existing value associated with the given key is null.
     * @param literal       If true, the key will be treated as a literal key, rather than potentially as a
     *                      fully-qualified key.
     * @param klass         The class of the value to be returned.
     * @param <T>           The type of value to be returned.
     * @return              Either the value associated with the given key in the given IData document, or the given
     *                      defaultValue if null.
     */
    public static <T> T get(IData document, String key, T defaultValue, boolean literal, Class<T> klass) {
        return get(null, document, key, defaultValue, literal, klass);
    }

    /**
     * Returns the value associated with the given key from the given IData document, or if null the specified default
     * value.
     *
     * @param pipeline      An IData document against which absolute variables are resolved.
     * @param scope         An IData document against which relative variables are resolved.
     * @param key           A simple or fully-qualified key identifying the value in the given IData document to be
     *                      returned.
     * @param defaultValue  A default value to be returned if the existing value associated with the given key is null.
     * @param literal       If true, the key will be treated as a literal key, rather than potentially as a
     *                      fully-qualified key.
     * @return              Either the value associated with the given key in the given IData document, or the given
     *                      defaultValue if null.
     */
    public static Object get(IData pipeline, IData scope, String key, Object defaultValue, boolean literal) {
        return get(pipeline, scope, key, defaultValue, literal, Object.class);
    }

    /**
     * Returns the value associated with the given key from the given IData document, or if null the specified default
     * value.
     *
     * @param pipeline      An IData document against which absolute variables are resolved.
     * @param scope         An IData document against which relative variables are resolved.
     * @param key           A simple or fully-qualified key identifying the value in the given IData document to be
     *                      returned.
     * @param defaultValue  A default value to be returned if the existing value associated with the given key is null.
     * @param literal       If true, the key will be treated as a literal key, rather than potentially as a
     *                      fully-qualified key.
     * @param klass         The class of the value to be returned.
     * @param <T>           The type of value to be returned.
     * @return              Either the value associated with the given key in the given IData document, or the given
     *                      defaultValue if null.
     */
    public static <T> T get(IData pipeline, IData scope, String key, T defaultValue, boolean literal, Class<T> klass) {
        T value = get(pipeline, scope, key, literal, klass);
        if (value == null) value = defaultValue;
        return value;
    }

    /**
     * Returns the value associated with the given key from the given scope (if relative) or pipeline (if absolute).
     *
     * @param pipeline          The pipeline, required if the key is an absolute path.
     * @param scope             An IData document used to scope the key if it is relative.
     * @param key               A simple or fully-qualified key identifying the value in the given IData document to be
     *                          returned.
     * @param literal           If true, the key will be treated as a literal key, rather than potentially as a fully-
     *                          qualified key.
     * @return                  The value associated with the given key in the given IData document.
     */
    public static Object get(IData pipeline, IData scope, String key, boolean literal) {
        return get(pipeline, scope, key, literal, null, Object.class);
    }

    /**
     * Returns the value associated with the given key from the given scope (if relative) or pipeline (if absolute).
     *
     * @param pipeline          The pipeline, required if the key is an absolute path.
     * @param scope             An IData document used to scope the key if it is relative.
     * @param key               A simple or fully-qualified key identifying the value in the given IData document to be
     *                          returned.
     * @param literal           If true, the key will be treated as a literal key, rather than potentially as a fully-
     *                          qualified key.
     * @param klass             The class of the value to be returned.
     * @param <T>               The type of value to be returned.
     * @return                  The value associated with the given key in the given IData document.
     */
    public static <T> T get(IData pipeline, IData scope, String key, boolean literal, Class<T> klass) {
        return get(pipeline, scope, key, literal, null, klass);
    }

    /**
     * Returns the value associated with the given key from the given scope (if relative) or pipeline (if absolute).
     *
     * @param pipeline          The pipeline, required if the key is an absolute path.
     * @param scope             An IData document used to scope the key if it is relative.
     * @param key               A simple or fully-qualified key identifying the value in the given IData document to be
     *                          returned.
     * @param literal           If true, the key will be treated as a literal key, rather than potentially as a fully-
     *                          qualified key.
     * @param namespaceContext  The namespace context used when resolving XPath expressions against nodes.
     * @return                  The value associated with the given key in the given IData document.
     */
    public static Object get(IData pipeline, IData scope, String key, boolean literal, NamespaceContext namespaceContext) {
        return get(pipeline, scope, key, literal, namespaceContext, Object.class);
    }

    /**
     * Returns the value associated with the given key from the given scope (if relative) or pipeline (if absolute).
     *
     * @param pipeline          The pipeline, required if the key is an absolute path.
     * @param scope             An IData document used to scope the key if it is relative.
     * @param key               A simple or fully-qualified key identifying the value in the given IData document to be
     *                          returned.
     * @param literal           If true, the key will be treated as a literal key, rather than potentially as a fully-
     *                          qualified key.
     * @param namespaceContext  The namespace context used when resolving XPath expressions against nodes.
     * @param klass             The class of the value to be returned.
     * @param <T>               The type of value to be returned.
     * @return                  The value associated with the given key in the given IData document.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(IData pipeline, IData scope, String key, boolean literal, NamespaceContext namespaceContext, Class<T> klass) {
        if (klass == null) throw new NullPointerException("class must not be null");
        if (key == null) return null;

        Object value = null;
        IDataCursor cursor = null;

        try {
            if (scope != null) cursor = scope.getCursor();

            // try finding a value that matches the literal key, and if not found try finding a value
            // associated with the leaf key if the key is considered fully-qualified
            if (scope != null && cursor != null && cursor.first(key)) {
                value = cursor.getValue();
            } else if (pipeline != null && IDataKey.isAbsolute(key, literal)) {
                value = get(null, pipeline, key.substring(1), literal, namespaceContext, klass);
            } else if (scope != null && IDataKey.isFullyQualified(key, literal)) {
                // support resolving XPath expressions against nodes
                Matcher matcher = KEY_NODE_XPATH_REGULAR_EXPRESSION_PATTERN.matcher(key);
                if (matcher.matches()) {
                    String variable = matcher.group(1);
                    String expression = matcher.group(2);

                    Object node = get(pipeline, scope, variable, true);

                    if (node instanceof Node) {
                        try {
                            XPathExpression compiledExpression = XPathHelper.compile(expression, namespaceContext);
                            Nodes nodes = XPathHelper.get((Node)node, compiledExpression);

                            if (nodes != null && nodes.size() > 0) {
                                value = NodeHelper.getValue(nodes.get(0));
                            }
                        } catch (XPathExpressionException ex) {
                            // do nothing, assume a normal IData fully-qualified key was specified rather than an XPath expression
                        }
                    } else {
                        value = get(scope, IDataKey.of(key, literal), klass);
                    }
                } else {
                    value = get(scope, IDataKey.of(key, literal), klass);
                }
            }
        } finally {
            if (cursor != null) cursor.destroy();
        }

        return klass.isInstance(value) ? (T)value : null;
    }

    /**
     * Returns the value associated with the given fully-qualified key from the given IData document.
     *
     * @param document An IData document.
     * @param key      A fully-qualified key identifying the value in the given IData document to be returned.
     * @return         The value associated with the given key in the given IData document.
     */
    private static Object get(IData document, IDataKey key) {
        return get(document, key, Object.class);
    }

    /**
     * Returns the value associated with the given fully-qualified key from the given IData document.
     *
     * @param document An IData document.
     * @param key      A fully-qualified key identifying the value in the given IData document to be returned.
     * @param klass     The class of the value to be returned.
     * @param <T>       The type of value to be returned.
     * @return         The value associated with the given key in the given IData document.
     */
    @SuppressWarnings("unchecked")
    private static <T> T get(IData document, IDataKey key, Class<T> klass) {
        if (klass == null) throw new NullPointerException("class must not be null");

        Object value = null;

        if (document != null && key != null && key.size() > 0) {
            IDataCursor cursor = document.getCursor();
            IDataKey.Part keyPart = key.remove();

            if (key.size() > 0) {
                if (keyPart.hasArrayIndex()) {
                    value = get(ArrayHelper.get(toIDataArray(IDataUtil.get(cursor, keyPart.getKey())), keyPart.getIndex()), key, klass);
                } else if (keyPart.hasKeyIndex()) {
                    value = get(toIData(get(document, keyPart.getKey(), keyPart.getIndex())), key, klass);
                } else {
                    Object object = IDataUtil.get(cursor, keyPart.getKey());
                    IData parent = toIData(object);
                    if (parent != null) {
                        value = get(parent, key, klass);
                    } else {
                        IData[] array = toIDataArray(object);
                        if (array != null) {
                            List<Object> values = new ArrayList<Object>(array.length);
                            // if we are referencing an IData[], create a new array of values from the individual values in each IData
                            for (IData item : array) {
                                values.add(get(item, key.clone(), klass));
                            }
                            value = ArrayHelper.normalize(values);
                        }
                    }
                }
            } else {
                if (keyPart.hasArrayIndex()) {
                    value = IDataUtil.get(cursor, keyPart.getKey());
                    if (value != null) {
                        if (value instanceof Object[] || value instanceof Table) {
                            Object[] array = value instanceof Object[] ? (Object[])value : ((Table)value).getValues();
                            value = ArrayHelper.get(array, keyPart.getIndex());
                        } else {
                            value = null;
                        }
                    }
                } else if (keyPart.hasKeyIndex()) {
                    value = get(document, keyPart.getKey(), keyPart.getIndex(), klass);
                } else {
                    value = IDataUtil.get(cursor, keyPart.getKey());
                }
            }

            cursor.destroy();
        }

        return klass.isInstance(value) ? (T)value : null;
    }

    /**
     * Returns the nth value associated with the given key.
     *
     * @param document  The IData document to return the value from.
     * @param key       The key whose associated value is to be returned.
     * @param n         Determines which occurrence of the key to return the value for.
     * @return          The value associated with the nth occurrence of the given key in the given IData document.
     */
    private static Object get(IData document, String key, int n) {
        return get(document, key, n, Object.class);
    }

    /**
     *
     * @param document  The IData document to return the value from.
     * @param key       The key whose associated value is to be returned.
     * @param n         Determines which occurrence of the key to return the value for.
     * @param klass     The class of the value to be returned.
     * @param <T>       The type of value to be returned.
     * @return          The value associated with the nth occurrence of the given key in the given IData document.
     */
    @SuppressWarnings("unchecked")
    private static <T> T get(IData document, String key, int n, Class<T> klass) {
        if (klass == null) throw new NullPointerException("class must not be null");
        if (document == null || key == null || n < 0) return null;

        Object value = null;
        int i = 0;

        IDataCursor cursor = document.getCursor();
        while (cursor.next(key) && i++ < n) ;
        if (i > n) value = cursor.getValue();
        cursor.destroy();

        return klass.isInstance(value) ? (T)value : null;
    }


    /**
     * Returns the value associated with the given key from the given IData document as an array.
     *
     * @param document  An IData document.
     * @param key       A simple or fully-qualified key identifying the value in the given IData document to be
     *                  returned.
     * @return          The value associated with the given key in the given IData document as an array.
     */
    public static Object[] getAsArray(IData document, String key) {
        return getAsArray(document, key, false);
    }

    /**
     * Returns the value associated with the given key from the given IData document as an array.
     *
     * @param document  An IData document.
     * @param key       A simple or fully-qualified key identifying the value in the given IData document to be
     *                  returned.
     * @param literal   If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                  key.
     * @return          The value associated with the given key in the given IData document as an array.
     */
    public static Object[] getAsArray(IData document, String key, boolean literal) {
        if (document == null || key == null) return null;

        Object[] output = null;
        IDataCursor cursor = document.getCursor();

        // try finding a value that matches the literal key, and if not found try finding a value
        // associated with the leaf key if the key is considered fully-qualified
        if (cursor.next(key)) {
            List<Object> list = new ArrayList<Object>();
            do {
                list.addAll(ObjectHelper.listify(cursor.getValue()));
            } while (cursor.next(key));
            output = ArrayHelper.toArray(list);
        } else if (IDataKey.isFullyQualified(key, literal)) {
            output = getAsArray(document, IDataKey.of(key, literal));
        }

        cursor.destroy();

        return output;
    }

    /**
     * Returns the value associated with the given fully-qualified key from the given IData document as an array.
     *
     * @param document  An IData document.
     * @param key       A fully-qualified key identifying the value in the given IData document to be returned.
     * @return          The value associated with the given key in the given IData document as an array.
     */
    private static Object[] getAsArray(IData document, IDataKey key) {
        Object[] output = null;

        if (document != null && key != null && key.size() > 0) {
            IDataCursor cursor = document.getCursor();
            IDataKey.Part keyPart = key.remove();

            if (key.size() > 0) {
                if (keyPart.hasArrayIndex()) {
                    output = getAsArray(ArrayHelper.get(toIDataArray(IDataUtil.get(cursor, keyPart.getKey())), keyPart.getIndex()), key);
                } else if (keyPart.hasKeyIndex()) {
                    output = getAsArray(toIData(get(document, keyPart.getKey(), keyPart.getIndex())), key);
                } else {
                    output = getAsArray(IDataUtil.getIData(cursor, keyPart.getKey()), key);
                }
            } else {
                List<Object> list = new ArrayList<Object>();
                if (keyPart.hasArrayIndex()) {
                    Object value = IDataUtil.get(cursor, keyPart.getKey());
                    if (value != null) {
                        if (value instanceof Object[] || value instanceof Table) {
                            Object[] array = value instanceof Object[] ? (Object[])value : ((Table)value).getValues();
                            value = ArrayHelper.get(array, keyPart.getIndex());
                        } else {
                            value = null;
                        }
                    }
                    list.addAll(ObjectHelper.listify(value));
                } else if (keyPart.hasKeyIndex()) {
                    list.addAll(ObjectHelper.listify(get(document, keyPart.getKey(), keyPart.getIndex())));
                } else {
                    while (cursor.next(keyPart.getKey())) {
                        list.addAll(ObjectHelper.listify(cursor.getValue()));
                    }
                }
                output = ArrayHelper.toArray(list);
            }

            cursor.destroy();
        }

        return output;
    }

    /**
     * Removes the given key and its associated value from the given cursor.
     *
     * @param cursor    The cursor to remove the key from.
     * @param key       The key to be removed.
     * @return          The value that was associated with the key.
     */
    public static Object remove(IDataCursor cursor, String key) {
        return remove(cursor, key, false);
    }

    /**
     * Removes the given key and its associated value from the given cursor.
     *
     * @param cursor    The cursor to remove the key from.
     * @param key       The key to be removed.
     * @param required  Throws an exception if true and a non-null value is not associated with the given key.
     * @return          The value that was associated with the key.
     */
    public static Object remove(IDataCursor cursor, String key, boolean required) {
        if (cursor == null || key == null) return null;

        Object value = null;

        if (cursor.first(key)) {
            value = cursor.getValue();
            cursor.delete();
        }

        if (value == null && required) {
            throw new RuntimeException(new NoSuchFieldException(MessageFormat.format("Key \"{0}\" either does not exist or is associated with a null value", key)));
        }

        return value;
    }

    /**
     * Removes the given key and its associated value from the given cursor.
     *
     * @param cursor    The cursor to remove the key from.
     * @param key       The key to be removed.
     * @param klass     The class the associated value is required to be an instance of.
     * @param <T>       The class the associated value is required to be an instance of.
     * @return          The value that was associated with the key.
     */
    @SuppressWarnings("unchecked")
    public static <T> T remove(IDataCursor cursor, String key, Class<T> klass) {
        return remove(cursor, key, klass, false);
    }

    /**
     * Removes the given key and its associated value from the given cursor.
     *
     * @param cursor    The cursor to remove the key from.
     * @param key       The key to be removed.
     * @param klass     The class the associated value is required to be an instance of.
     * @param required  Throws an exception if true and a value with the required class is not associated with the
     *                  given key.
     * @param <T>       The class the associated value is required to be an instance of.
     * @return          The value that was associated with the key.
     */
    @SuppressWarnings("unchecked")
    public static <T> T remove(IDataCursor cursor, String key, Class<T> klass, boolean required) {
        if (cursor == null || key == null) return null;

        T value = null;

        if (cursor.first(key)) {
            do {
                value = ObjectHelper.convert(cursor.getValue(), klass);
            } while(value == null && cursor.next(key));
            cursor.delete();
        }

        if (value == null && required) {
            throw new RuntimeException(new NoSuchFieldException(MessageFormat.format("Key \"{0}\" either does not exist or is not associated with a value compatible with the required class {1}", key, klass == null ? "null" : klass.getName())));
        }

        return value;
    }

    /**
     * Removes the given key and its associated value from the given cursor.
     *
     * @param cursor        The cursor to remove the key from.
     * @param key           The key to be removed.
     * @param defaultValue  The value to return if the associated value is null or the key does not exist.
     * @return              The value that was associated with the key.
     */
    @SuppressWarnings("unchecked")
    public static Object removeOrDefault(IDataCursor cursor, String key, Object defaultValue) {
        Object value = remove(cursor, key, false);
        return value == null ? defaultValue : value;
    }

    /**
     * Removes the given key and its associated value from the given cursor.
     *
     * @param cursor        The cursor to remove the key from.
     * @param key           The key to be removed.
     * @param klass         The class the associated value is required to be an instance of.
     * @param defaultValue  The value to return if the associated value is null or the key does not exist.
     * @param <T>           The class the associated value is required to be an instance of.
     * @return              The value that was associated with the key.
     */
    @SuppressWarnings("unchecked")
    public static <T> T removeOrDefault(IDataCursor cursor, String key, Class<T> klass, T defaultValue) {
        T value = remove(cursor, key, klass, false);
        return value == null ? defaultValue : value;
    }

    /**
     * Returns the value associated with the given key from the IDataCursor.
     *
     * @param cursor        The IDataCursor to add the key value association to.
     * @param key           The key literal to be added.
     * @return              The value associated with the given key, if one exists that is an instance of the given
     *                      class.
     */
    public static Object get(IDataCursor cursor, String key) {
        return get(cursor, key, false);
    }

    /**
     * Returns the value associated with the given key from the IDataCursor.
     *
     * @param cursor        The IDataCursor to add the key value association to.
     * @param key           The key literal to be added.
     * @return              The value associated with the given key, if one exists that is an instance of the given
     *                      class.
     */
    public static Object get(IDataCursor cursor, String key, boolean required) {
        if (cursor == null || key == null) return null;

        Object value = null;

        if (cursor.first(key)) {
            value = cursor.getValue();
        }

        if (value == null && required) {
            throw new RuntimeException(new NoSuchFieldException(MessageFormat.format("Key \"{0}\" either does not exist or is associated with null value", key)));
        }

        return value;
    }

    /**
     * Returns the value associated with the given key from the IDataCursor, if it is an instance of the given class.
     *
     * @param cursor        The IDataCursor to add the key value association to.
     * @param key           The key literal to be added.
     * @param klass         The class the returned value is required to be an instance of.
     * @param <T>           The class the returned value is required to be an instance of.
     * @return              The value associated with the given key, if one exists that is an instance of the given
     *                      class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(IDataCursor cursor, String key, Class<T> klass) {
        return get(cursor, key, klass, false);
    }

    /**
     * Returns the value associated with the given key from the IDataCursor, if it is an instance of the given class.
     *
     * @param cursor        The IDataCursor to add the key value association to.
     * @param key           The key literal to be added.
     * @param klass         The class the returned value is required to be an instance of.
     * @param required      Throws an exception if true and a value with the required class is not associated with the
     *                      given key.
     * @param <T>           The class the returned value is required to be an instance of.
     * @return              The value associated with the given key, if one exists that is an instance of the given
     *                      class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(IDataCursor cursor, String key, Class<T> klass, boolean required) {
        if (cursor == null || key == null || klass == null) return null;

        T value = null;

        if (cursor.first(key)) {
            do {
                value = ObjectHelper.convert(cursor.getValue(), klass);
            } while(value == null && cursor.next(key));
        }

        if (value == null && required) {
            throw new RuntimeException(new NoSuchFieldException(MessageFormat.format("Key \"{0}\" either does not exist or is not associated with a value compatible with the required class {1}", key, klass.getName())));
        }

        return value;
    }

    /**
     * Returns the value associated with the given key from the IDataCursor, if it is an instance of the given class.
     *
     * @param cursor        The IDataCursor to add the key value association to.
     * @param key           The key literal to be added.
     * @param klass         The class the returned value is required to be an instance of.
     * @param defaultValue  The value to return if the associated value is null or the key does not exist.
     * @param <T>           The class the returned value is required to be an instance of.
     * @return              The value associated with the given key, if one exists that is an instance of the given
     *                      class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(IDataCursor cursor, String key, Class<T> klass, T defaultValue) {
        T value = get(cursor, key, klass, false);
        return value == null ? defaultValue : value;
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor    The IDataCursor to add the key value association to.
     * @param key       The key literal to be added.
     * @param value     The value to be associated with the given key.
     */
    public static <T> void put(IDataCursor cursor, String key, Class<T> klass, Object value) {
        put(cursor, key, ObjectHelper.convert(value, klass), true, true);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor    The IDataCursor to add the key value association to.
     * @param key       The key literal to be added.
     * @param value     The value to be associated with the given key.
     */
    public static void put(IDataCursor cursor, String key, Object value) {
        put(cursor, key, value, true);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor            The IDataCursor to add the key value association to.
     * @param key               The key literal to be added.
     * @param value             The value to be associated with the given key. If null, no change is made to the cursor.
     * @param includeNullValue  If true and the given value is null, no change is made to the cursor. In all other cases
     *                          the given value will be associated with the given key.
     */
    public static void put(IDataCursor cursor, String key, Object value, boolean includeNullValue) {
        put(cursor, key, value, includeNullValue, true);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor            The IDataCursor to add the key value association to.
     * @param key               The key literal to be added.
     * @param value             The value to be associated with the given key. If null, no change is made to the cursor.
     * @param includeNullValue  If false and the given value is null, no change is made to the cursor. In all other
     *                          cases the given value will be associated with the given key.
     * @param includeEmptyValue If false and the given value is an empty array or empty string, no change is made to the
     *                          cursor. In all other cases the given value will be associated with the given key.
     */
    public static void put(IDataCursor cursor, String key, Object value, boolean includeNullValue, boolean includeEmptyValue) {
        put(cursor, key, value, includeNullValue, includeEmptyValue, true);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor            The IDataCursor to add the key value association to.
     * @param key               The key literal to be added.
     * @param value             The value to be associated with the given key. If null, no change is made to the cursor.
     * @param includeNullValue  If false and the given value is null, no change is made to the cursor. In all other
     *                          cases the given value will be associated with the given key.
     * @param includeEmptyValue If false and the given value is an empty array or empty string, no change is made to the
     *                          cursor. In all other cases the given value will be associated with the given key.
     * @param replace           If a value is already associated with the given key, replace it, rather than add a new
     *                          instance of the key.
     */
    public static void put(IDataCursor cursor, String key, Object value, boolean includeNullValue, boolean includeEmptyValue, boolean replace) {
        if (!includeNullValue && value == null) return;
        if (!includeEmptyValue && ObjectHelper.isEmpty(value)) return;

        if (replace && cursor.first(key)) {
            cursor.setValue(value);
        } else {
            cursor.insertAfter(key, value);
        }
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor            The IDataCursor to add the key value association to.
     * @param key               The key literal to be added.
     * @param value             The value to be associated with the given key. If null, no change is made to the cursor.
     * @param klass             The required class of the value; the value will be coerced to this class if possible.
     */
    public static <T> void put(IDataCursor cursor, String key, Object value, Class<T> klass) {
        put(cursor, key, value, klass, true);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor            The IDataCursor to add the key value association to.
     * @param key               The key literal to be added.
     * @param value             The value to be associated with the given key. If null, no change is made to the cursor.
     * @param klass             The required class of the value; the value will be coerced to this class if possible.
     * @param includeNullValue  If false and the given value is null, no change is made to the cursor. In all other
     *                          cases the given value will be associated with the given key.
     */
    public static <T> void put(IDataCursor cursor, String key, Object value, Class<T> klass, boolean includeNullValue) {
        put(cursor, key, value, klass, includeNullValue, true);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor            The IDataCursor to add the key value association to.
     * @param key               The key literal to be added.
     * @param value             The value to be associated with the given key. If null, no change is made to the cursor.
     * @param klass             The required class of the value; the value will be coerced to this class if possible.
     * @param includeNullValue  If false and the given value is null, no change is made to the cursor. In all other
     *                          cases the given value will be associated with the given key.
     * @param includeEmptyValue If false and the given value is an empty array or empty string, no change is made to the
     *                          cursor. In all other cases the given value will be associated with the given key.
     */
    public static <T> void put(IDataCursor cursor, String key, Object value, Class<T> klass, boolean includeNullValue, boolean includeEmptyValue) {
        put(cursor, key, value, klass, includeNullValue, includeEmptyValue, true);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor            The IDataCursor to add the key value association to.
     * @param key               The key literal to be added.
     * @param value             The value to be associated with the given key. If null, no change is made to the cursor.
     * @param klass             The required class of the value; the value will be coerced to this class if possible.
     * @param includeNullValue  If false and the given value is null, no change is made to the cursor. In all other
     *                          cases the given value will be associated with the given key.
     * @param includeEmptyValue If false and the given value is an empty array or empty string, no change is made to the
     *                          cursor. In all other cases the given value will be associated with the given key.
     * @param replace           If a value is already associated with the given key, replace it, rather than add a new
     *                          instance of the key.
     */
    public static <T> void put(IDataCursor cursor, String key, Object value, Class<T> klass, boolean includeNullValue, boolean includeEmptyValue, boolean replace) {
        put(cursor, key, ObjectHelper.convert(value, klass, false), includeNullValue, includeEmptyValue, replace);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor    The IDataCursor to add the key value association to.
     * @param key       The key literal to be added.
     * @param value     The value to be associated with the given key.
     */
    public static void putOrDefault(IDataCursor cursor, String key, Object value, Object defaultValue) {
        put(cursor, key, value == null ? defaultValue : value, true, true);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor    The IDataCursor to add the key value association to.
     * @param key       The key literal to be added.
     * @param value     The value to be associated with the given key.
     */
    public static <T> void putOrDefault(IDataCursor cursor, String key, Object value, Class<T> klass, T defaultValue) {
        put(cursor, key, value == null ? defaultValue : value, klass, true, true, true);
    }

    /**
     * Sets the value associated with the given key in the given IData document. Note that this method mutates the given
     * IData document in place.
     *
     * @param document  An IData document.
     * @param key       A simple or fully-qualified key identifying the value to be set.
     * @param value     The value to be set.
     * @return          The input IData document with the value set.
     */
    public static IData put(IData document, String key, Object value) {
        return put(document, key, value, false);
    }

    /**
     * Sets the value associated with the given key in the given IData document. Note that this method mutates the given
     * IData document in place.
     *
     * @param document  An IData document.
     * @param key       A simple or fully-qualified key identifying the value to be set.
     * @param value     The value to be set.
     * @param literal   If true, the key will be treated as a literal key, rather than potentially as a fully-qualified
     *                  key.
     * @return          The input IData document with the value set.
     */
    public static IData put(IData document, String key, Object value, boolean literal) {
        return put(document, key, value, literal, true);
    }

    /**
     * Sets the value associated with the given key in the given IData document. Note that this method mutates the given
     * IData document in place.
     *
     * @param document      An IData document.
     * @param key           A simple or fully-qualified key identifying the value to be set.
     * @param value         The value to be set.
     * @param literal       If true, the key will be treated as a literal key, rather than potentially as a
     *                      fully-qualified key.
     * @param includeNull   When true the value is set even when null, otherwise the value is only set when it is not
     *                      null.
     * @return              The input IData document with the value set.
     */
    public static IData put(IData document, String key, Object value, boolean literal, boolean includeNull) {
        return put(document, IDataKey.of(key, literal), value, includeNull);
    }

    /**
     * Sets the value associated with the given key in the given IData document. Note that this method mutates the given
     * IData document in place.
     *
     * @param document      An IData document.
     * @param key           A fully-qualified key identifying the value to be set.
     * @param value         The value to be set.
     * @param includeNull   When true the value is set even when null, otherwise the value is only set when it is
     *                      not null.
     * @return              The input IData document with the value set.
     */
    private static IData put(IData document, IDataKey key, Object value, boolean includeNull) {
        if (!includeNull && value == null) return document;

        if (key != null && key.size() > 0) {
            if (document == null) document = IDataFactory.create();

            IDataCursor cursor = document.getCursor();
            IDataKey.Part keyPart = key.remove();

            if (key.size() > 0) {
                if (keyPart.hasArrayIndex()) {
                    IData[] array = IDataUtil.getIDataArray(cursor, keyPart.getKey());
                    IData child = null;
                    try {
                        child = ArrayHelper.get(array, keyPart.getIndex());
                    } catch(ArrayIndexOutOfBoundsException ex) {
                        // ignore exception
                    }
                    value = ArrayHelper.put(array, put(child, key, value, includeNull), keyPart.getIndex(), IData.class);
                } else if (keyPart.hasKeyIndex()) {
                    value = put(toIData(get(document, keyPart.getKey(), keyPart.getIndex())), key, value, includeNull);
                } else {
                    value = put(IDataUtil.getIData(cursor, keyPart.getKey()), key, value, includeNull);
                }
            } else if (keyPart.hasArrayIndex()) {
                Class klass = Object.class;
                if (value != null) {
                    if (value instanceof String) {
                        klass = String.class;
                    } else if (value instanceof IData) {
                        klass = IData.class;
                    }
                }
                value = ArrayHelper.put(IDataUtil.getObjectArray(cursor, keyPart.getKey()), value, keyPart.getIndex(), klass);
            }

            if (keyPart.hasKeyIndex()) {
                put(document, keyPart.getKey(), keyPart.getIndex(), value);
            } else {
                IDataUtil.put(cursor, keyPart.getKey(), value);
            }
            cursor.destroy();
        }

        return document;
    }

    /**
     * Sets the value associated with the given nth key in the given IData document. Note that this method mutates the
     * given IData document in place.
     *
     * @param document  The IData document to set the key's associated value in.
     * @param key       The key whose value is to be set.
     * @param n         Determines which occurrence of the key to set the value for.
     * @param value     The value to be set.
     * @return          The IData document with the given nth key set to the given value.
     */
    private static IData put(IData document, String key, int n, Object value) {
        if (document == null || key == null || n < 0) return null;

        IDataCursor cursor = document.getCursor();
        for (int i = 0; i < n; i++) {
            if (!cursor.next(key)) cursor.insertAfter(key, null);
        }
        cursor.insertAfter(key, value);
        cursor.destroy();

        return document;
    }

    /**
     * Converts the given object to a Map object, if possible.
     *
     * @param object    The object to be converted.
     * @return          A Map representation of the given object if its type is compatible (IData, IDataCodable,
     *                  IDataPortable, ValuesCodable), otherwise null.
     */
    private static Map<String, Object> toMap(Object object) {
        if (object == null) return null;

        Map<String, Object> output = null;

        if (object instanceof IData) {
            output = toMap((IData)object);
        } else if (object instanceof IDataCodable) {
            output = toMap((IDataCodable)object);
        } else if (object instanceof IDataPortable) {
            output = toMap((IDataPortable)object);
        } else if (object instanceof ValuesCodable) {
            output = toMap((ValuesCodable)object);
        }

        return output;
    }

    /**
     * Converts an IData object to a Map object.
     *
     * @param document  An IData object to be converted.
     * @return          A Map representation of the given IData object.
     */
    public static Map<String, Object> toMap(IData document) {
        if (document == null) return null;

        IDataCursor cursor = document.getCursor();
        int size = IDataUtil.size(cursor);
        cursor.destroy();

        Map<String, Object> output = new java.util.LinkedHashMap<String, Object>(size);

        for (Map.Entry<String, Object> entry : IDataMap.of(document)) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                value = toList(value);
            } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                value = toMap(value);
            }
            output.put(key, value);
        }

        cursor.destroy();

        return output;
    }

    /**
     * Converts an IDataCodable object to a Map object.
     *
     * @param document  An IDataCodable object to be converted.
     * @return          A Map representation of the given IDataCodable object.
     */
    public static Map<String, Object> toMap(IDataCodable document) {
        return toMap(toIData(document));
    }

    /**
     * Converts an IDataPortable object to a Map object.
     *
     * @param document  An IDataPortable object to be converted.
     * @return          A Map representation of the given IDataPortable object.
     */
    public static Map<String, Object> toMap(IDataPortable document) {
        return toMap(toIData(document));
    }

    /**
     * Converts an ValuesCodable object to a Map object.
     *
     * @param document  An ValuesCodable object to be converted.
     * @return          A Map representation of the given ValuesCodable object.
     */
    public static Map<String, Object> toMap(ValuesCodable document) {
        return toMap(toIData(document));
    }

    /**
     * Converts an object to a List object, if possible.
     *
     * @param object    An object to be converted.
     * @return          A List representation of the given object, if the object was a compatible type (IData[],
     *                  Table, IDataCodable[], IDataPortable[], ValuesCodable[]), otherwise null.
     */
    private static List<Map<String, Object>> toList(Object object) {
        if (object == null) return null;

        List<Map<String, Object>> output = null;

        if (object instanceof IData[]) {
            output = toList((IData[])object);
        } else if (object instanceof Table) {
            output = toList((Table)object);
        } else if (object instanceof IDataCodable[]) {
            output = toList((IDataCodable[])object);
        } else if (object instanceof IDataPortable[]) {
            output = toList((IDataPortable[])object);
        } else if (object instanceof ValuesCodable[]) {
            output = toList((ValuesCodable[])object);
        }

        return output;
    }

    /**
     * Converts an IData[] object to a List object.
     *
     * @param array An IData[] object to be converted.
     * @return      A List representation of the given IData[] object.
     */
    public static List<Map<String, Object>> toList(IData[] array) {
        if (array == null) return null;

        List<Map<String, Object>> output = new java.util.ArrayList<Map<String, Object>>(array.length);

        for (IData item : array) {
            output.add(toMap(item));
        }

        return output;
    }

    /**
     * Converts a Table object to a List object.
     *
     * @param table An Table object to be converted.
     * @return      A List representation of the given Table object.
     */
    public static List<Map<String, Object>> toList(Table table) {
        return toList(toIDataArray(table));
    }

    /**
     * Converts an IDataCodable[] object to a List object.
     *
     * @param array An IDataCodable[] object to be converted.
     * @return      A List representation of the given IDataCodable[] object.
     */
    public static List<Map<String, Object>> toList(IDataCodable[] array) {
        return toList(toIDataArray(array));
    }

    /**
     * Converts an IDataPortable[] object to a List object.
     *
     * @param array An IDataPortable[] object to be converted.
     * @return      A List representation of the given IDataPortable[] object.
     */
    public static List<Map<String, Object>> toList(IDataPortable[] array) {
        return toList(toIDataArray(array));
    }

    /**
     * Converts an ValuesCodable[] object to a java.util.List object.
     *
     * @param array An ValuesCodable[] object to be converted.
     * @return      A List representation of the given ValuesCodable[] object.
     */
    public static List<Map<String, Object>> toList(ValuesCodable[] array) {
        return toList(toIDataArray(array));
    }

    /**
     * Returns an IData representation of the given object, if possible.
     *
     * @param object    The object to convert.
     * @return          An IData representing the given object if its type is compatible (IData, IDataCodable,
     *                  IDataPortable, ValuesCodable), otherwise null.
     */
    public static IData toIData(Object object) {
        if (object == null) return null;

        IData output = null;

        if (object instanceof IData) {
            output = (IData)object;
        } else if (object instanceof IDataCodable) {
            output = toIData((IDataCodable)object);
        } else if (object instanceof IDataPortable) {
            output = toIData((IDataPortable)object);
        } else if (object instanceof ValuesCodable) {
            output = toIData((ValuesCodable)object);
        } else if (object instanceof Map) {
            output = toIData((Map)object);
        }

        return output;
    }

    /**
     * Returns an IData representation of the given IDataCodable object.
     *
     * @param document  The IDataCodable object to be converted to an IData object.
     * @return          An IData representation of the give IDataCodable object.
     */
    public static IData toIData(IDataCodable document) {
        if (document == null) return null;
        return document.getIData();
    }

    /**
     * Returns an IData representation of the given IDataPortable object.
     *
     * @param document  The IDataPortable object to be converted to an IData object.
     * @return          An IData representation of the give IDataPortable object.
     */
    public static IData toIData(IDataPortable document) {
        if (document == null) return null;
        return document.getAsData();
    }

    /**
     * Returns an IData representation of the given ValuesCodable object.
     *
     * @param document  The ValuesCodable object to be converted to an IData object.
     * @return          An IData representation of the give ValuesCodable object.
     */
    public static IData toIData(ValuesCodable document) {
        if (document == null) return null;
        return document.getValues();
    }

    /**
     * Returns an IData representation of the given Map.
     *
     * @param map   The Map to be converted.
     * @return      An IData representation of the given map.
     */
    public static IData toIData(Map map) {
        if (map == null) return null;

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();
        for (Object key : map.keySet()) {
            if (key != null) {
                put(output, key.toString(), normalize(map.get(key)), true);
            }
        }
        cursor.destroy();

        return output;
    }

    /**
     * Returns an IData[] representation of the given object, if possible.
     *
     * @param object    The Table object to be converted to an IData[] object.
     * @return          An IData[] representation of the give object if the object was a compatible type (IData[],
     *                  Table, IDataCodable[], IDataPortable[], ValuesCodable[]), otherwise null.
     */
    public static IData[] toIDataArray(Object object) {
        if (object == null) return null;

        IData[] output = null;

        if (object instanceof Table) {
            output = toIDataArray((Table)object);
        } else if (object instanceof IDataCodable[]) {
            output = toIDataArray((IDataCodable[])object);
        } else if (object instanceof IDataPortable[]) {
            output = toIDataArray((IDataPortable[])object);
        } else if (object instanceof ValuesCodable[]) {
            output = toIDataArray((ValuesCodable[])object);
        } else if (object instanceof Map[]) {
            output = toIDataArray((Map[])object);
        } else if (object instanceof IData[]) {
            output = (IData[])object;
        }

        return output;
    }

    /**
     * Returns an IData[] representation of the given Table object.
     *
     * @param table The Table object to be converted to an IData[] object.
     * @return      An IData[] representation of the give Table object.
     */
    public static IData[] toIDataArray(Table table) {
        if (table == null) return null;
        return table.getValues();
    }

    /**
     * Returns an IData[] representation of the given IDataCodable[] object.
     *
     * @param array The IDataCodable[] object to be converted to an IData[] object.
     * @return      An IData[] representation of the give IDataCodable[] object.
     */
    public static IData[] toIDataArray(IDataCodable[] array) {
        if (array == null) return null;
        IData[] output = new IData[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = toIData(array[i]);
        }
        return output;
    }

    /**
     * Returns an IData[] representation of the given IDataPortable[] object.
     *
     * @param array The IDataPortable[] object to be converted to an IData[] object.
     * @return      An IData[] representation of the give IDataPortable[] object.
     */
    public static IData[] toIDataArray(IDataPortable[] array) {
        if (array == null) return null;
        IData[] output = new IData[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = toIData(array[i]);
        }
        return output;
    }

    /**
     * Returns an IData[] representation of the given ValuesCodable[] object.
     *
     * @param array The ValuesCodable[] object to be converted to an IData[] object.
     * @return      An IData[] representation of the give ValuesCodable[] object.
     */
    public static IData[] toIDataArray(ValuesCodable[] array) {
        if (array == null) return null;
        IData[] output = new IData[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = toIData(array[i]);
        }
        return output;
    }

    /**
     * Returns an IData[] representation of the given Map[] object.
     *
     * @param array The Map[] object to be converted to an IData[] object.
     * @return      An IData[] representation of the give Map[] object.
     */
    public static IData[] toIDataArray(Map[] array) {
        if (array == null) return null;
        IData[] output = new IData[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = toIData(array[i]);
        }
        return output;
    }

    /**
     * Returns the union set of keys present in every item in the given IData[] document list.
     *
     * @param array An IData[] to retrieve the union set of keys from.
     * @return      The union set of keys from the given IData[].
     */
    public static String[] getKeys(IData[] array) {
        return getKeys(array, (Pattern)null);
    }

    /**
     * Returns the union set of keys present in every item in the given IData[] document list that match the given
     * regular expression pattern.
     *
     * @param array         An IData[] to retrieve the union set of keys from.
     * @param patternString A regular expression pattern the returned keys must match.
     * @return              The union set of keys from the given IData[].
     */
    public static String[] getKeys(IData[] array, String patternString) {
        return getKeys(array, patternString == null ? null : Pattern.compile(patternString));
    }

    /**
     * Returns the union set of keys present in every item in the given IData[] document list that match the given
     * regular expression pattern.
     *
     * @param array     An IData[] to retrieve the union set of keys from.
     * @param pattern   A regular expression pattern the returned keys must match.
     * @return          The union set of keys from the given IData[].
     */
    public static String[] getKeys(IData[] array, Pattern pattern) {
        java.util.Set<String> keys = new java.util.LinkedHashSet<String>();

        if (array != null) {
            for (IData document : array) {
                if (document != null) {
                    for (Map.Entry<String, Object> entry : IDataMap.of(document)) {
                        String key = entry.getKey();
                        if (pattern == null) {
                            keys.add(key);
                        } else {
                            Matcher matcher = pattern.matcher(key);
                            if (matcher.matches()) keys.add(key);
                        }
                    }
                }
            }
        }

        return keys.toArray(new String[keys.size()]);
    }

    /**
     * Converts an IData document to an IData[] document list with each item representing each key value tuple from the
     * given document.
     *
     * @param document  An IData document to pivot.
     * @param recurse   Whether to recursively pivot embedded IData objects.
     * @return          The given IData document pivoted.
     */
    public static IData[] pivot(IData document, boolean recurse) {
        if (document == null) return null;

        IDataCursor cursor = document.getCursor();
        List<IData> pivot = new ArrayList<IData>();

        while (cursor.next()) {
            String key = cursor.getKey();
            Object value = cursor.getValue();

            if (recurse && value != null) {
                if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                    IData[] array = toIDataArray(value);
                    if (array != null) {
                        List<IData[]> list = new ArrayList<IData[]>(array.length);

                        for (int i = 0; i < array.length; i++) {
                            list.add(pivot(array[i], recurse));
                        }

                        value = list.toArray(new IData[0][0]);
                    }
                } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                    value = pivot(toIData(value), recurse);
                }
            }

            IData item = IDataFactory.create();
            IDataCursor ic = item.getCursor();
            IDataUtil.put(ic, "key", key);
            IDataUtil.put(ic, "value", value);
            ic.destroy();

            pivot.add(item);
        }

        return pivot.toArray(new IData[pivot.size()]);
    }

    /**
     * Returns an IData document where the keys are the values associated with given pivot key from the given IData[]
     * document list, and the values are the IData[] document list items associated with each pivot key.
     *
     * @param array     The IData[] to be pivoted.
     * @param delimiter The delimiter to use when building a compound key.
     * @param pivotKeys The keys to pivot on.
     * @return          The IData document representing the pivoted IData[].
     */
    public static IData pivot(IData[] array, String delimiter, String... pivotKeys) {
        if (array == null || pivotKeys == null || pivotKeys.length == 0) return null;
        if (delimiter == null) delimiter = "/";

        IData output = IDataFactory.create();

        outer:
        for (IData item : array) {
            if (item != null) {
                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < pivotKeys.length; i++) {
                    Object value = get(item, pivotKeys[i]);
                    if (value == null) {
                        continue outer;
                    } else {
                        buffer.append(value.toString());
                    }
                    if (i < (pivotKeys.length - 1)) buffer.append(delimiter);
                }
                String key = buffer.toString();
                if (get(output, key) == null) put(output, key, item);
            }
        }

        return output;
    }

    /**
     * Returns a new IData document containing all denormalized items from the given input IData document.
     *
     * Array items are denormalized to individual items in the output IData document with their keys suffixed
     * with "[n]" where n is the item's array index.
     *
     * Items in nested IData documents are denormalized to items included directly in the output IData document
     * with their keys prefixed with the associated fully-qualified nested path.
     *
     * @param input An IData document to be denormalized.
     * @return      The denormalized IData document.
     */
    public static IData denormalize(IData input) {
        if (input == null) return null;

        IData output = IDataFactory.create();

        IDataCursor inputCursor = input.getCursor();
        IDataCursor outputCursor = output.getCursor();

        denormalize(inputCursor, outputCursor, null);

        inputCursor.destroy();
        outputCursor.destroy();

        return output;
    }

    /**
     * Inserts each item of the given input IDataCursor to the end of the given output IDataCursor with the keys
     * denormalized to a fully-qualified key.
     *
     * @param inputCursor   The cursor to source items to be denormalized from.
     * @param outputCursor  The cursor to insert the denormalized items into.
     * @param path          The original path to the IData document being denormalized from the inputCursor, or null.
     */
    private static void denormalize(IDataCursor inputCursor, IDataCursor outputCursor, String path) {
        if (inputCursor == null || outputCursor == null) return;

        while(inputCursor.next()) {
            String key = inputCursor.getKey();
            Object value = inputCursor.getValue();

            if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                denormalize(toIDataArray(value), outputCursor, path == null ? key : path + "/" + key);
            } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                IData child = toIData(value);
                IDataCursor childCursor = child.getCursor();
                denormalize(childCursor, outputCursor, path == null ? key : path + "/" + key);
                childCursor.destroy();
            } else if (value instanceof Object[][]) {
                denormalize((Object[][])value, outputCursor, path == null ? key : path + "/" + key);
            } else if (value instanceof Object[]) {
                denormalize((Object[])value, outputCursor, path == null ? key : path + "/" + key);
            } else {
                outputCursor.insertAfter(path == null ? key : path + "/" + key, value);
            }
        }

        inputCursor.destroy();
        outputCursor.destroy();
    }

    /**
     * Inserts each item of the given array to the end of the given IDataCursor with the given
     * key suffixed with "[n]" where n is the item's array index.
     *
     * @param array         An array to be denormalized into the given IDataCursor.
     * @param outputCursor  The cursor to insert the denormalized array items into.
     * @param key           The original key associated with this array in the IData document being denormalized.
     */
    private static void denormalize(IData[] array, IDataCursor outputCursor, String key) {
        if (array == null || array.length == 0 || outputCursor == null || key == null) return;

        for (int i = 0; i < array.length; i++) {
            IData child = array[i];
            if (child != null) {
                IDataCursor inputCursor = child.getCursor();
                denormalize(inputCursor, outputCursor, key + "[" + i + "]");
                inputCursor.destroy();
            }

        }
    }

    /**
     * Inserts each item of the given two dimensional array to the end of the given IDataCursor with the given
     * key suffixed with "[n][m]" where n and m are the item's array indexes.
     *
     * @param array         An array to be denormalized into the given IDataCursor.
     * @param outputCursor  The cursor to insert the denormalized array items into.
     * @param key           The original key associated with this array in the IData document being denormalized.
     */
    private static void denormalize(Object[][] array, IDataCursor outputCursor, String key) {
        if (array == null || array.length == 0 || outputCursor == null || key == null) return;

        for (int i = 0; i < array.length; i++) {
            denormalize(array[i], outputCursor, key + "[" + i + "]");
        }
    }

    /**
     * Inserts each item of the given array to the end of the given IDataCursor with the given
     * key suffixed with "[n]" where n is the item's array index.
     *
     * @param array         An array to be denormalized into the given IDataCursor.
     * @param outputCursor  The cursor to insert the denormalized array items into.
     * @param key           The original key associated with this array in the IData document being denormalized.
     */
    private static void denormalize(Object[] array, IDataCursor outputCursor, String key) {
        if (array == null || array.length == 0 || outputCursor == null || key == null) return;

        for (int i = 0; i < array.length; i++) {
            outputCursor.insertAfter(key + "[" + i + "]", array[i]);
        }

        outputCursor.destroy();
    }

    /**
     * Sorts the given IData document by its keys in natural ascending order.
     *
     * @param document  An IData document to be sorted by its keys.
     * @return          A new IData document which is duplicate of the given input IData document but with its keys
     *                  sorted in natural ascending order.
     */
    public static IData sort(IData document) {
        return sort(document, true);
    }

    /**
     * Sorts the given IData document by its keys in natural ascending order.
     *
     * @param document  An IData document to be sorted by its keys.
     * @param recurse   A boolean which when true will also recursively sort nested IData document and IData[] document
     *                  lists.
     * @return          A new IData document which is duplicate of the given input IData document but with its keys
     *                  sorted in natural ascending order.
     */
    public static IData sort(IData document, boolean recurse) {
        return sort(document, recurse, false);
    }

    /**
     * Sorts the given IData document by its keys in natural ascending or descending order.
     *
     * @param document      An IData document to be sorted by its keys.
     * @param recurse       A boolean which when true will also recursively sort nested IData document and IData[]
     *                      document lists.
     * @param descending    Whether to sort in descending or ascending order.
     * @return              A new IData document which is duplicate of the given input IData document but with its keys
     *                      sorted in natural ascending order.
     */
    public static IData sort(IData document, boolean recurse, boolean descending) {
        if (document == null) return null;

        String[] keys = ArrayHelper.sort(getKeys(document), descending);

        IData output = IDataFactory.create();
        IDataCursor ic = document.getCursor();
        IDataCursor oc = output.getCursor();

        for (int i = 0; i < keys.length; i++) {
            boolean result;

            if (i > 0 && keys[i].equals(keys[i - 1])) {
                result = ic.next(keys[i]);
            } else {
                result = ic.first(keys[i]);
            }

            if (result) {
                Object value = ic.getValue();
                if (recurse) {
                    if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                        IData[] array = toIDataArray(value);
                        for (int j = 0; j < array.length; j++) {
                            array[j] = sort(array[j], recurse);
                        }
                        value = array;
                    } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                        value = sort(toIData(value), recurse);
                    }
                }
                oc.insertAfter(keys[i], value);
            }
        }

        ic.destroy();
        oc.destroy();

        return output;
    }

    /**
     * Returns a new IData[] array with all elements sorted in ascending order by the values associated with the given
     * key.
     *
     * @param array An IData[] array to be sorted.
     * @param key   The key to use to sort the array.
     * @return      A new IData[] array sorted by the given key.
     */
    public static IData[] sort(IData[] array, String key) {
        return sort(array, key, true);
    }

    /**
     * Returns a new IData[] array with all elements sorted in either ascending or descending order by the values
     * associated with the given key.
     *
     * @param array     An IData[] array to be sorted.
     * @param key       The key to use to sort the array.
     * @param ascending When true, the array will be sorted in ascending order, otherwise it will be sorted in
     *                  descending order.
     * @return          A new IData[] array sorted by the given key.
     */
    public static IData[] sort(IData[] array, String key, boolean ascending) {
        String[] keys = null;
        if (key != null) {
            keys = new String[1];
            keys[0] = key;
        }

        return sort(array, keys, ascending);
    }

    /**
     * Returns a new IData[] array with all elements sorted in ascending order by the values associated with the given
     * keys.
     *
     * @param array An IData[] array to be sorted.
     * @param keys  The list of keys in order of precedence to use to sort the array.
     * @return      A new IData[] array sorted by the given keys.
     */
    public static IData[] sort(IData[] array, String[] keys) {
        return sort(array, keys, true);
    }

    /**
     * Returns a new IData[] array with all elements sorted in either ascending or descending order by the values
     * associated with the given keys.
     *
     * @param array     An IData[] array to be sorted.
     * @param keys      The list of keys in order of precedence to use to sort the array.
     * @param ascending When true, the array will be sorted in ascending order, otherwise it will be sorted in
     *                  descending order.
     * @return          A new IData[] array sorted by the given keys.
     */
    public static IData[] sort(IData[] array, String[] keys, boolean ascending) {
        if (array == null || array.length < 2 || keys == null || keys.length == 0) return array;

        IDataComparisonCriterion[] criteria = new IDataComparisonCriterion[keys.length];
        for (int i = 0; i < keys.length; i++) {
            criteria[i] = new IDataComparisonCriterion(keys[i], !ascending);
        }

        return sort(array, criteria);
    }

    /**
     * Returns a new IData[] array with all elements sorted according to the specified criteria.
     *
     * @param array     An IData[] array to be sorted.
     * @param criteria  One or more sort criteria.
     * @return          A new IData[] array sorted by the given criteria.
     */
    public static IData[] sort(IData[] array, IDataComparisonCriterion... criteria) {
        if (array == null) return null;

        if (criteria != null && criteria.length > 0) {
            array = ArrayHelper.sort(array, new CriteriaBasedIDataComparator(criteria));
        } else {
            array = Arrays.copyOf(array, array.length);
        }

        return array;
    }

    /**
     * Returns a new IData[] array with all elements sorted according to the specified criteria.
     *
     * @param array     An IData[] array to be sorted.
     * @param criteria  One or more sort criteria specified as an IData[].
     * @return          A new IData[] array sorted by the given criteria.
     */
    public static IData[] sort(IData[] array, IData[] criteria) {
        return sort(array, IDataComparisonCriterion.of(criteria));
    }

    /**
     * Returns a new IData[] array with all elements sorted according to the specified criteria.
     *
     * @param array         An IData[] array to be sorted.
     * @param comparator    An IDataComparator object used to determine element ordering.
     * @return              A new IData[] array sorted by the given criteria.
     */
    public static IData[] sort(IData[] array, IDataComparator comparator) {
        if (array == null) return null;
        return ArrayHelper.sort(array, comparator);
    }

    /**
     * Returns the values associated with the given key from each item in the given IData[] document list.
     *
     * @param array         An IData[] array to return values from.
     * @param key           A fully-qualified key identifying the values to return.
     * @param defaultValue  The default value returned if the key does not exist.
     * @return              The values associated with the given key from each IData item in the given array.
     */
    public static Object[] getValues(IData[] array, String key, Object defaultValue) {
        if (array == null || key == null) return null;

        List<Object> list = new ArrayList<Object>(array.length);

        for (IData item : array) {
            list.add(get(item, key, defaultValue));
        }

        return ArrayHelper.normalize(list);
    }

    /**
     * Converts all the keys in the given IData document to lower case.
     *
     * @param input     The IData whose keys are to be converted to lower case.
     * @return          The given IData duplicated with all keys converted to lower case.
     */
    public static IData keysToLowerCase(IData input) {
        return keysToLowerCase(input, true);
    }

    /**
     * Converts all the keys in the given IData document to lower case.
     *
     * @param input     The IData whose keys are to be converted to lower case.
     * @param recurse   Whether child IData and IData[] objects should also have their keys converted to lower case.
     * @return          The given IData duplicated with all keys converted to lower case.
     */
    public static IData keysToLowerCase(IData input, boolean recurse) {
        if (input == null) return null;

        IData output = IDataFactory.create();
        IDataCursor inputCursor = input.getCursor();
        IDataCursor outputCursor = output.getCursor();

        while(inputCursor.next()) {
            String key = inputCursor.getKey();
            Object value = inputCursor.getValue();

            if (recurse) {
                if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                    value = keysToLowerCase(toIDataArray(value), recurse);
                } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                    value = keysToLowerCase(toIData(value), recurse);
                }
            }

            outputCursor.insertAfter(key.toLowerCase(), value);
        }
        inputCursor.destroy();
        outputCursor.destroy();

        return output;
    }

    /**
     * Converts all the keys in the given IData[] document list to lower case.
     *
     * @param input     The IData[] whose keys are to be converted to lower case.
     * @return          The given IData[] duplicated with all keys converted to lower case.
     */
    public static IData[] keysToLowerCase(IData[] input) {
        return keysToLowerCase(input, true);
    }

    /**
     * Converts all the keys in the given IData[] document list to lower case.
     *
     * @param input     The IData[] whose keys are to be converted to lower case.
     * @param recurse   Whether child IData and IData[] objects should also have their keys converted to lower case.
     * @return          The given IData[] duplicated with all keys converted to lower case.
     */
    public static IData[] keysToLowerCase(IData[] input, boolean recurse) {
        if (input == null) return null;

        IData[] output = new IData[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = keysToLowerCase(input[i], recurse);
        }

        return output;
    }

    /**
     * Groups the given IData[] by the given keys.
     *
     * @param array The IData[] to be grouped.
     * @param keys  The keys to group items by.
     * @return      The grouped IData[].
     */
    public static IData[] group(IData[] array, String... keys) {
        Map<CompoundKey, List<IData>> groups = group(array, IDataComparisonCriterion.of(keys));
        List<IData> result;

        if (groups.size() == 0) {
            result = new ArrayList<IData>(1);

            IData document = IDataFactory.create();
            IDataCursor cursor = document.getCursor();
            IDataUtil.put(cursor, "group", IDataFactory.create());
            IDataUtil.put(cursor, "items", array);
            cursor.destroy();

            result.add(document);
        } else {
            result = new ArrayList<IData>(groups.size());

            for (Map.Entry<CompoundKey, List<IData>> entry : groups.entrySet()) {
                CompoundKey key = entry.getKey();
                List<IData> items = entry.getValue();

                IData group = IDataFactory.create();
                IDataCursor cursor = group.getCursor();
                IDataUtil.put(cursor, "group", key.getIData());
                IDataUtil.put(cursor, "items", items.toArray(new IData[items.size()]));
                cursor.destroy();

                result.add(group);
            }
        }

        return result.toArray(new IData[result.size()]);
    }

    /**
     * Performs a multi-level grouping of the given IData[] by the given criteria.
     *
     * @param array     The IData[] to be grouped.
     * @param criteria  The multi-level grouping criteria.
     * @return          The grouped IData[].
     */
    public static IData[] group(IData[] array, IData criteria) {
        if (array == null) return null;

        List<IData> result;

        if (criteria == null) {
            result = new ArrayList<IData>(1);

            IData document = IDataFactory.create();
            IDataCursor cursor = document.getCursor();
            IDataUtil.put(cursor, "by", IDataFactory.create());
            IDataUtil.put(cursor, "items", array);
            cursor.destroy();

            result.add(document);
        } else {
            IDataCursor criteriaCursor = criteria.getCursor();
            IData[] by = IDataUtil.getIDataArray(criteriaCursor, "by");
            IData then = IDataUtil.getIData(criteriaCursor, "then");
            criteriaCursor.destroy();

            Map<CompoundKey, List<IData>> groups = group(array, IDataComparisonCriterion.of(by));
            result = new ArrayList<IData>(groups.size());

            for (Map.Entry<CompoundKey, List<IData>> entry : groups.entrySet()) {
                CompoundKey key = entry.getKey();
                List<IData> value = entry.getValue();
                IData[] items = value.toArray(new IData[value.size()]);

                IData group = IDataFactory.create();
                IDataCursor cursor = group.getCursor();
                IDataUtil.put(cursor, "by", key.getIData());
                IDataUtil.put(cursor, "items", items);
                if (then != null) IDataUtil.put(cursor, "then", group(items, then));

                cursor.destroy();

                result.add(group);
            }
        }

        return result.toArray(new IData[result.size()]);
    }

    /**
     * Groups the given IData[] by the given keys.
     *
     * @param array    The IData[] to be grouped.
     * @param criteria The criteria to group items by.
     * @return         A Map containing the groups and their items.
     */
    public static Map<CompoundKey, List<IData>> group(IData[] array, IDataComparisonCriterion[] criteria) {
        Map<CompoundKey, List<IData>> groups = new TreeMap<CompoundKey, List<IData>>();

        if (array != null && criteria != null || criteria.length == 0) {
            for (IData item : array) {
                if (item != null) {
                    CompoundKey key = new CompoundKey(criteria, item);
                    List<IData> list = groups.get(key);
                    if (list == null) {
                        list = new ArrayList<IData>();
                        groups.put(key, list);
                    }
                    list.add(item);
                }
            }
        }

        return groups;
    }

    /**
     * Returns a new IData[] document list that only contains unique IData objects from the input IData[] document
     * list.
     *
     * @param array The IData[] document list to find the unique set of.
     * @return      A new IData[] document list only containing the first occurrence of each IData containing a
     *              distinct set of values.
     */
    public static IData[] unique(IData[] array) {
        return unique(array, (String[])null);
    }

    /**
     * Returns a new IData[] document list that only contains unique IData objects from the input IData[] document list,
     * where uniqueness is determined by the values associated with the given list of keys.
     *
     * @param array The IData[] document list to find the unique set of.
     * @param keys  The keys whose associated values will be used to determine uniqueness. If not specified, all keys
     *              will be used to determine uniqueness.
     * @return      A new IData[] document list only containing the first occurrence of each IData containing a distinct
     *              set of values associated with the given list of keys.
     */
    public static IData[] unique(IData[] array, String... keys) {
        IData[] output = null;

        if (array != null) {
            if (array.length <= 1) {
                output = Arrays.copyOf(array, array.length);
            } else {
                if (keys == null || keys.length == 0) keys = getKeys(array);

                Map<CompoundKey, IData> set = new TreeMap<CompoundKey, IData>();
                for (IData item : array) {
                    if (item != null) {
                        CompoundKey key = new CompoundKey(keys, item);
                        if (!set.containsKey(key)) set.put(key, item);
                    }
                }

                output = set.values().toArray(new IData[set.size()]);
            }
        }

        return output;
    }

    /**
     * Represents a compound key which can be used for grouping IData documents together.
     */
    private static class CompoundKey implements Comparable<CompoundKey>, IDataCodable {
        /**
         * The comparator used for comparison with other compound keys.
         */
        private CriteriaBasedIDataComparator comparator;
        /**
         * The IData document containing the values referenced by the compound key.
         */
        private IData document;

        /**
         * Constructs a new compound key for the given list of keys and their associated values from the given IData
         * document.
         *
         * @param keys     The keys which together form this compound key.
         * @param document The IData document containing the values associated with the given keys.
         */
        public CompoundKey(String[] keys, IData document) {
            this(new CriteriaBasedIDataComparator(IDataComparisonCriterion.of(keys)), document);
        }

        /**
         * Constructs a new compound key for the given comparison criteria and their associated values from the given
         * IData document.
         *
         * @param criteria The comparison criteria which together form this compound key.
         * @param document The IData document containing the values associated with the given keys.
         */
        public CompoundKey(IDataComparisonCriterion[] criteria, IData document) {
            this(new CriteriaBasedIDataComparator(criteria), document);
        }

        /**
         * Constructs a new compound key for the given comparison criteria and their associated values from the given
         * IData document.
         *
         * @param criteria The comparison criteria which together form this compound key.
         * @param document The IData document containing the values associated with the given keys.
         */
        public CompoundKey(List<IDataComparisonCriterion> criteria, IData document) {
            this(new CriteriaBasedIDataComparator(criteria), document);
        }

        /**
         * Constructs a new compound key for the given comparison criteria and their associated values from the given
         * IData document.
         *
         * @param comparator The comparator used for comparison with other compound keys.
         * @param document   The IData document containing the values associated with the given keys.
         */
        private CompoundKey(CriteriaBasedIDataComparator comparator, IData document) {
            if (comparator == null) throw new NullPointerException("comparator must not be null");
            if (document == null) throw new NullPointerException("document must not be null");
            this.comparator = comparator;
            this.document = document;
        }

        /**
         * Returns the IData document containing the values used by this compound key.
         *
         * @return The IData document containing the values used by this compound key.
         */
        public IData getDocument() {
            return this.document;
        }

        /**
         * Sets the IData document containing the values used for comparison by this compound key.
         *
         * @param document The IData document containing the values to be used for comparison by this compound key.
         */
        public void setDocument(IData document) {
            if (document == null) throw new NullPointerException("document must not be null");
            this.document = document;
        }

        /**
         * Returns the comparator used for comparisons by this compound key.
         *
         * @return The comparator used for comparisons by this compound key.
         */
        public CriteriaBasedIDataComparator getComparator() {
            return this.comparator;
        }

        /**
         * Sets the comparator to be used by this compound key in comparisons.
         *
         * @param comparator The comparator to be used by this compound key in comparisons.
         */
        public void setComparator(CriteriaBasedIDataComparator comparator) {
            this.comparator = comparator;
        }

        /**
         * Returns an IData representation of this compound key.
         *
         * @return An IData representation of this compound key.
         */
        public IData getIData() {
            IData output = IDataFactory.create();
            for (IDataComparisonCriterion criterion : comparator.getCriteria()) {
                IDataHelper.put(output, criterion.getKey(), IDataHelper.get(document, criterion.getKey()));
            }
            return output;
        }

        /**
         * This method is not implemented.
         *
         * @param  document                         Not used.
         * @throws UnsupportedOperationException    as this method is not implemented.
         */
        public void setIData(IData document) {
            throw new UnsupportedOperationException("method not implemented");
        }

        /**
         * Compares this compound key with another compound key.
         *
         * @param  other    The other key to be compared with.
         * @return          0 if the two keys are equal, less than 0 if this key is less than the other key,
         *                  greater than 0 if this key is greater than the other key.
         */
        public int compareTo(CompoundKey other) {
            if (other == null) return 1;
            return comparator.compare(this.document, other.getIData());
        }

        /**
         * Returns true if this object is equal to the other object.
         *
         * @param  other    The object to compare for equality with.
         * @return          True if this object is equal to the other object.
         */
        public boolean equals(Object other) {
            boolean result = false;

            if (other instanceof CompoundKey) {
                result = this.compareTo((CompoundKey)other) == 0;
            }

            return result;
        }
    }
}
