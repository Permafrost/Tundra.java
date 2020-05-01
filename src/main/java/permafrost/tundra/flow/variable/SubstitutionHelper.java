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

package permafrost.tundra.flow.variable;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataPortable;
import com.wm.data.IDataUtil;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.ObjectHelper;
import permafrost.tundra.lang.StringHelper;
import permafrost.tundra.net.uri.URIHelper;
import permafrost.tundra.server.ServiceHelper;
import permafrost.tundra.util.regex.ReplacementHelper;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for performing dynamic variable substitution.
 */
public final class SubstitutionHelper {
    /**
     * A regular expression pattern for detecting variable substitution statements in strings.
     */
    protected static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("%([^%]+)%");

    /**
     * Disallow instantiation of this class.
     */
    private SubstitutionHelper() {}

    /**
     * Returns a regular expression matcher which matches percent-delimited variable substitution statements in the
     * given string.
     *
     * @param substitutionString The string to match against.
     * @return                   A regular expression matcher which returns match results for variable substitution
     *                           statements.
     */
    public static Matcher matcher(String substitutionString) {
        return SUBSTITUTION_PATTERN.matcher(substitutionString);
    }

    /**
     * Performs variable substitution on the given string by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param substitutionString A string to perform variable substitution on.
     * @param valueClass         The class of value to be returned.
     * @param defaultValue       A default value to be substituted when the variable being substituted has a value of
     *                           null.
     * @param substitutionType   The type of substitution to perform.
     * @param scopes             One or more IData documents containing the variables being substituted.
     * @param <T>                The class of value to be returned.
     * @return                   The result of the variable substitution.
     */
    @SuppressWarnings("unchecked")
    public static <T> T substitute(String substitutionString, Class<T> valueClass, Object defaultValue, SubstitutionType substitutionType, IData... scopes) {
        if (valueClass == null) throw new NullPointerException("valueClass must not be null");
        if (substitutionString == null || scopes == null || scopes.length == 0) return null;

        Matcher matcher = matcher(substitutionString);
        T output = null;

        if (matcher.matches()) {
            output = resolve(matcher.group(1), valueClass, defaultValue == null ? matcher.group(0) : defaultValue, substitutionType, scopes);
        } else if (valueClass.isAssignableFrom(String.class)) {
            StringBuffer buffer = new StringBuffer();
            matcher.reset();
            while (matcher.find()) {
                matcher.appendReplacement(buffer, ReplacementHelper.quote(resolve(matcher.group(1), String.class, defaultValue == null ? matcher.group(0) : defaultValue, substitutionType, scopes)));
            }
            matcher.appendTail(buffer);
            output = (T)buffer.toString();
        }

        return output;
    }

    /**
     * Resolves the given key by replacing it with the associated value from the given scope; if the key has no value,
     * the given defaultValue (if not null) is used instead.
     *
     * @param key                The key whose associated value is to be returned.
     * @param valueClass         The class of value to be returned.
     * @param defaultValue       A default value to be substituted when the variable being substituted has a value of
     *                           null.
     * @param substitutionType   The type of substitution to perform.
     * @param scopes             One or more IData documents containing the variables being substituted.
     * @param <T>                The class of value to be returned.
     * @return                   The result of the variable substitution.
     */
    @SuppressWarnings("unchecked")
    public static <T> T resolve(String key, Class<T> valueClass, Object defaultValue, SubstitutionType substitutionType, IData... scopes) {
        if (valueClass == null) throw new NullPointerException("valueClass must not be null");
        if (key == null || scopes == null || scopes.length == 0) return null;

        T output = null;

        if (exists(key, substitutionType, scopes)) {
            output = getValue(key, valueClass, substitutionType, scopes);
        } else if (isInvoke(key)) {
            output = invoke(key, valueClass);
        }

        if (output == null && defaultValue != null) {
            output = ObjectHelper.convert(defaultValue, valueClass);
        }

        return output;
    }

    /**
     * Returns true if the given key exists in the specified variable substitution types and scopes.
     *
     * @param key                The key to check existence of.
     * @param substitutionType   The type of variable substitution being performed.
     * @param scopes             The scopes against which local variable substitution is resolved.
     * @return                   True if the given key exists.
     */
    private static boolean exists(String key, SubstitutionType substitutionType, IData... scopes) {
        substitutionType = SubstitutionType.normalize(substitutionType);

        boolean exists = false;

        if (substitutionType == SubstitutionType.ALL || substitutionType == SubstitutionType.LOCAL) {
            for (IData scope : scopes) {
                boolean localExists = IDataHelper.exists(scope, key);
                if (localExists) {
                    exists = localExists;
                    break;
                }
            }
        }

        if (!exists && (substitutionType == SubstitutionType.ALL || substitutionType == SubstitutionType.GLOBAL)) {
            exists = GlobalVariableHelper.exists(key);
        }

        return exists;
    }

    /**
     * Returns true if this key represents an invoke URI.
     *
     * @param key   The key to check.
     * @return      True if this key represents an invoke URI.
     */
    private static boolean isInvoke(String key) {
        return key != null && key.startsWith("invoke:");
    }

    /**
     * Processes the given key as an invoke URI.
     *
     * @param key           The key to invoke.
     * @param valueClass    The return class for the result.
     * @param <T>           The return class for the result.
     * @return              The result of the service invocation.
     */
    private static<T> T invoke(String key, Class<T> valueClass) {
        Object value = null;

        try {
            IData uri = URIHelper.parse(key);
            if (uri != null) {
                IDataCursor cursor = uri.getCursor();
                try {
                    String[] body = StringHelper.split(IDataHelper.get(cursor, "body", String.class), "/", true);
                    IData pipeline = IDataHelper.get(cursor, "query", IData.class);

                    if (body == null || body.length == 0) {
                        throw new URISyntaxException(key, "service name to be invoked is required");
                    } else {
                        String service = body[0];
                        String outputKey = null;
                        if (body.length > 1) {
                            outputKey = ArrayHelper.join(Arrays.copyOfRange(body, 1, body.length), "/");
                        }
                        if (pipeline == null) {
                            pipeline = IDataFactory.create();
                        }

                        pipeline = ServiceHelper.invoke(service, pipeline);
                        if (pipeline != null) {
                            if (outputKey == null) {
                                IDataCursor pipelineCursor = pipeline.getCursor();
                                try {
                                    if (pipelineCursor.first()) {
                                        value = pipelineCursor.getValue();
                                    }
                                } finally {
                                    pipelineCursor.destroy();
                                }
                            } else {
                                value = IDataHelper.get(pipeline, outputKey);
                            }
                        }
                    }
                } catch(ServiceException ex) {
                    ExceptionHelper.raiseUnchecked(ex);
                } finally {
                    cursor.destroy();
                }
            }
        } catch(URISyntaxException ex) {
            ExceptionHelper.raiseUnchecked(ex);
        }

        return ObjectHelper.convert(value, valueClass);
    }

    /**
     * Returns the value for the given key based on the specified variable substitution types and scopes.
     *
     * @param key                The key whose value is to be returned.
     * @param valueClass         The class of value to be returned.
     * @param substitutionType   The type of variable substitution being performed.
     * @param scopes             The scopes against which local variable substitution is resolved.
     * @param <T>                The class of value to be returned.
     * @return                   The value associated with the given key.
     */
    @SuppressWarnings("unchecked")
    private static <T> T getValue(String key, Class<T> valueClass, SubstitutionType substitutionType, IData... scopes) {
        substitutionType = SubstitutionType.normalize(substitutionType);

        Object value = null;

        if (substitutionType == SubstitutionType.ALL || substitutionType == SubstitutionType.LOCAL) {
            for (IData scope : scopes) {
                Object localValue = IDataHelper.get(scope, key);
                if (localValue != null) {
                    value = localValue;
                    break;
                }
            }
        }

        if (value == null && (substitutionType == SubstitutionType.ALL || substitutionType == SubstitutionType.GLOBAL)) {
            value = GlobalVariableHelper.get(key);
        }

        return ObjectHelper.convert(value, valueClass);
    }

    /**
     * Performs variable substitution on the given String[] by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param array                 A String[] to perform variable substitution on.
     * @param valueClass            The class of value to be returned.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value
     *                              of null.
     * @param substitutionType      The type of substitution to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @param <T>                   The class of value to be returned.
     * @return                      The String[] after variable substitution has been performed.
     */
    public static <T> T[] substitute(String[] array, Class<T> valueClass, Object defaultValue, SubstitutionType substitutionType, IData... scopes) {
        if (array == null) return null;

        T[] output = ArrayHelper.instantiate(valueClass, array.length);

        for (int i = 0; i < array.length; i++) {
            output[i] = substitute(array[i], valueClass, defaultValue, substitutionType, scopes);
        }

        return output;
    }

    /**
     * Performs variable substitution on the given String[][] by replacing all occurrences of substrings matching
     * "%key%" with the associated value from the given scope; if the key has no value, the given defaultValue (if not
     * null) is used instead.
     *
     * @param table                 A String[][] to perform variable substitution on.
     * @param valueClass            The class of value to be returned.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value of null.
     * @param substitutionType      The type of substitution to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @param <T>                   The class of value to be returned.
     * @return                      The result of the variable substitution.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][] substitute(String[][] table, Class<T> valueClass, Object defaultValue, SubstitutionType substitutionType, IData... scopes) {
        if (table == null) return null;

        T[][] output = (T[][])ArrayHelper.instantiate(valueClass, table.length, 0);

        for (int i = 0; i < table.length; i++) {
            output[i] = substitute(table[i], valueClass, defaultValue, substitutionType, scopes);
        }

        return output;
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document              The IData document to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param includeNulls          Whether null values should be included in the output document.
     * @param substitutionType      The type of substitution to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The variable substituted IData.
     */
    @SuppressWarnings("deprecation")
    public static IData substitute(IData document, Object defaultValue, boolean recurse, boolean includeNulls, SubstitutionType substitutionType, IData... scopes) {
        if (document == null) return null;
        if (scopes == null || scopes.length == 0) {
            scopes = new IData[1];
            scopes[0] = document;
        }

        IData output = IDataFactory.create();
        IDataCursor inputCursor = document.getCursor();
        IDataCursor outputCursor = output.getCursor();

        try {
            while (inputCursor.next()) {
                String key = inputCursor.getKey();
                Object value = inputCursor.getValue();

                if (value != null) {
                    if (recurse && (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[])) {
                        value = substitute(IDataHelper.toIDataArray(value), defaultValue, recurse, includeNulls, substitutionType, scopes);
                    } else if (recurse && (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable)) {
                        value = substitute(IDataHelper.toIData(value), defaultValue, recurse, includeNulls, substitutionType, scopes);
                    } else if (value instanceof String) {
                        value = substitute((String) value, String.class, defaultValue, substitutionType, scopes);
                    } else if (value instanceof String[]) {
                        value = substitute((String[]) value, String.class, defaultValue, substitutionType, scopes);
                    } else if (value instanceof String[][]) {
                        value = substitute((String[][]) value, String.class, defaultValue, substitutionType, scopes);
                    }
                }

                if (value != null || includeNulls) {
                    IDataUtil.put(outputCursor, key, value);
                }
            }
        } finally {
            inputCursor.destroy();
            outputCursor.destroy();
        }

        return output;
    }

    /**
     * Performs variable substitution on all elements of the given IData[].
     *
     * @param array                 The IData[] to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param includeNulls          Whether null values should be included in the output document.
     * @param substitutionType      The type of substitution to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The variable substituted IData[].
     */
    public static IData[] substitute(IData[] array, Object defaultValue, boolean recurse, boolean includeNulls, SubstitutionType substitutionType, IData... scopes) {
        if (array == null) return null;

        IData[] output = new IData[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = substitute(array[i], defaultValue, recurse, includeNulls, substitutionType, scopes);
        }

        return output;
    }
}
