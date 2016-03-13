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
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for performing dynamic variable substitution.
 */
public class SubstitutionHelper {
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
     * with the associated value from the given scope.
     *
     * @param substitutionString A string to perform variable substitution on.
     * @param scope              An IData document containing the variables being substituted.
     * @return                   The string after variable substitution has been performed.
     * @throws ServiceException  If an error occurs retrieving a global variable.
     */
    public static String substitute(String substitutionString, IData scope) throws ServiceException {
        return substitute(substitutionString, null, scope);
    }

    /**
     * Performs variable substitution on the given string by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param substitutionString A string to perform variable substitution on.
     * @param defaultValue       A default value to be substituted when the variable being substituted has a value of
     *                           null.
     * @param scope              An IData document containing the variables being substituted.
     * @return                   The string after variable substitution has been performed.
     * @throws ServiceException  If an error occurs retrieving a global variable.
     */
    public static String substitute(String substitutionString, String defaultValue, IData scope) throws ServiceException {
        return substitute(substitutionString, defaultValue, scope, SubstitutionType.DEFAULT_SUBSTITUTION_SET);
    }

    /**
     * Performs variable substitution on the given string by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param substitutionString A string to perform variable substitution on.
     * @param defaultValue       A default value to be substituted when the variable being substituted has a value of
     *                           null.
     * @param scope              An IData document containing the variables being substituted.
     * @param substitutionType   The type of substitution to perform.
     * @return                   The string after variable substitution has been performed.
     * @throws ServiceException  If an error occurs retrieving a global variable.
     */
    public static String substitute(String substitutionString, String defaultValue, IData scope, SubstitutionType substitutionType) throws ServiceException {
        return substitute(substitutionString, defaultValue, scope, SubstitutionType.normalize(substitutionType));
    }

    /**
     * Performs variable substitution on the given string by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param substitutionString A string to perform variable substitution on.
     * @param defaultValue       A default value to be substituted when the variable being substituted has a value of
     *                           null.
     * @param scope              An IData document containing the variables being substituted.
     * @param substitutionTypes  The type of substitutions to perform.
     * @return                   The string after variable substitution has been performed.
     * @throws ServiceException  If an error occurs retrieving a global variable.
     */
    public static String substitute(String substitutionString, String defaultValue, IData scope, EnumSet<SubstitutionType> substitutionTypes) throws ServiceException {
        if (substitutionString == null || scope == null) return substitutionString;
        substitutionTypes = SubstitutionType.normalize(substitutionTypes);

        Matcher matcher = matcher(substitutionString);
        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = null;

            if (substitutionTypes.contains(SubstitutionType.GLOBAL)) {
                Object globalValue = GlobalVariableHelper.get(key);
                if (globalValue != null) value = globalValue;
            }

            if (substitutionTypes.contains(SubstitutionType.LOCAL)) {
                Object localValue = IDataHelper.get(scope, key);
                if (localValue != null) value = localValue;
            }

            if (value != null && value instanceof String) {
                matcher.appendReplacement(output, Matcher.quoteReplacement((String)value));
            } else if (value == null && defaultValue != null) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(defaultValue));
            } else {
                matcher.appendReplacement(output, Matcher.quoteReplacement(matcher.group(0)));
            }
        }

        matcher.appendTail(output);

        return output.toString();
    }

    /**
     * Performs variable substitution on the given String[] by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param array                 A String[] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value
     *                              of null.
     * @param scope                 An IData document containing the variables being substituted.
     * @return                      The String[] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[] substitute(String[] array, String defaultValue, IData scope) throws ServiceException {
        return substitute(array, defaultValue, scope, SubstitutionType.DEFAULT_SUBSTITUTION_SET);
    }

    /**
     * Performs variable substitution on the given String[] by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param array                 A String[] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value
     *                              of null.
     * @param scope                 An IData document containing the variables being substituted.
     * @param substitutionType      The type of substitution to be performed.
     * @return                      The String[] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[] substitute(String[] array, String defaultValue, IData scope, SubstitutionType substitutionType) throws ServiceException {
        return substitute(array, defaultValue, scope, SubstitutionType.normalize(substitutionType));
    }

    /**
     * Performs variable substitution on the given String[] by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param array                 A String[] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value
     *                              of null.
     * @param scope                 An IData document containing the variables being substituted.
     * @param substitutionTypes     The type of substitutions to be performed.
     * @return                      The String[] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[] substitute(String[] array, String defaultValue, IData scope, EnumSet<SubstitutionType> substitutionTypes) throws ServiceException {
        if (array == null) return null;

        String[] output = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = substitute(array[i], defaultValue, scope, substitutionTypes);
        }

        return output;
    }

    /**
     * Performs variable substitution on the given String[][] by replacing all occurrences of substrings matching
     * "%key%" with the associated value from the given scope; if the key has no value, the given defaultValue (if not
     * null) is used instead.
     *
     * @param table                 A String[][] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value
     *                              of null.
     * @param scope                 An IData document containing the variables being substituted.
     * @return                      The String[][] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[][] substitute(String[][] table, String defaultValue, IData scope) throws ServiceException {
        return substitute(table, defaultValue, scope, SubstitutionType.DEFAULT_SUBSTITUTION_SET);
    }

    /**
     * Performs variable substitution on the given String[][] by replacing all occurrences of substrings matching
     * "%key%" with the associated value from the given scope; if the key has no value, the given defaultValue (if not
     * null) is used instead.
     *
     * @param table                 A String[][] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value of null.
     * @param scope                 An IData document containing the variables being substituted.
     * @param substitutionType      The type of substitution to be performed.
     * @return                      The String[][] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[][] substitute(String[][] table, String defaultValue, IData scope, SubstitutionType substitutionType) throws ServiceException {
        return substitute(table, defaultValue, scope, SubstitutionType.normalize(substitutionType));
    }

    /**
     * Performs variable substitution on the given String[][] by replacing all occurrences of substrings matching
     * "%key%" with the associated value from the given scope; if the key has no value, the given defaultValue (if not
     * null) is used instead.
     *
     * @param table                 A String[][] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value of null.
     * @param scope                 An IData document containing the variables being substituted.
     * @param substitutionTypes     The type of substitutions to be performed.
     * @return                      The String[][] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[][] substitute(String[][] table, String defaultValue, IData scope, EnumSet<SubstitutionType> substitutionTypes) throws ServiceException {
        if (table == null) return null;

        String[][] output = new String[table.length][];

        for (int i = 0; i < table.length; i++) {
            output[i] = substitute(table[i], defaultValue, scope, substitutionTypes);
        }

        return output;
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document  The IData document to perform variable substitution on.
     * @return          The variable substituted IData.
     */
    public static IData substitute(IData document) throws ServiceException {
        return substitute(document, null, null, true);
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document  The IData document to perform variable substitution on.
     * @param recurse   Whether embedded IData and IData[] should have variable substitution recursively performed on
     *                  them.
     * @return          The variable substituted IData.
     */
    public static IData substitute(IData document, boolean recurse) throws ServiceException {
        return substitute(document, null, null, recurse);
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document  The IData document to perform variable substitution on.
     * @param scope     The scope against which variables are are resolved.
     * @param recurse   Whether embedded IData and IData[] should have variable substitution recursively performed on
     *                  them.
     * @return          The variable substituted IData.
     */
    public static IData substitute(IData document, IData scope, boolean recurse) throws ServiceException {
        return substitute(document, null, scope, recurse);
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document      The IData document to perform variable substitution on.
     * @param defaultValue  The value to substitute if a variable cannot be resolved.
     * @param scope         The scope against which variables are are resolved.
     * @param recurse       Whether embedded IData and IData[] should have variable substitution recursively performed on
     *                      them.
     * @return              The variable substituted IData.
     */
    public static IData substitute(IData document, String defaultValue, IData scope, boolean recurse) throws ServiceException {
        return substitute(document, defaultValue, scope, recurse, SubstitutionType.DEFAULT_SUBSTITUTION_SET);
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document              The IData document to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param scope                 The scope against which variables are are resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param substitutionType      The type of substitution to be performed.
     * @return                      The variable substituted IData.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static IData substitute(IData document, String defaultValue, IData scope, boolean recurse, SubstitutionType substitutionType) throws ServiceException {
        return substitute(document, defaultValue, scope, recurse, SubstitutionType.normalize(substitutionType));
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document              The IData document to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param scope                 The scope against which variables are are resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param substitutionTypes     The type of substitutions to be performed.
     * @return                      The variable substituted IData.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static IData substitute(IData document, String defaultValue, IData scope, boolean recurse, EnumSet<SubstitutionType> substitutionTypes) throws ServiceException {
        if (document == null) return null;
        if (scope == null) scope = document;

        IData output = IDataFactory.create();
        IDataCursor inputCursor = document.getCursor();
        IDataCursor outputCursor = output.getCursor();

        while (inputCursor.next()) {
            String key = inputCursor.getKey();
            Object value = inputCursor.getValue();

            if (value != null) {
                if (recurse && (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[])) {
                    value = substitute(IDataHelper.toIDataArray(value), defaultValue, scope, recurse, substitutionTypes);
                } else if (recurse && (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable)) {
                    value = substitute(IDataHelper.toIData(value), defaultValue, scope, recurse, substitutionTypes);
                } else if (value instanceof String) {
                    value = substitute((String)value, defaultValue, scope, substitutionTypes);
                } else if (value instanceof String[]) {
                    value = substitute((String[])value, defaultValue, scope, substitutionTypes);
                } else if (value instanceof String[][]) {
                    value = substitute((String[][])value, defaultValue, scope, substitutionTypes);
                }
            }
            IDataUtil.put(outputCursor, key, value);
        }

        inputCursor.destroy();
        outputCursor.destroy();

        return output;
    }

    /**
     * Performs variable substitution on all elements of the given IData[].
     *
     * @param array         The IData[] to perform variable substitution on.
     * @param defaultValue  The value to substitute if a variable cannot be resolved.
     * @param scope         The scope against which variables are are resolved.
     * @param recurse       Whether embedded IData and IData[] should have variable substitution recursively performed on
     *                      them.
     * @return              The variable substituted IData[].
     */
    public static IData[] substitute(IData[] array, String defaultValue, IData scope, boolean recurse) throws ServiceException {
        return substitute(array, defaultValue, scope, recurse, SubstitutionType.DEFAULT_SUBSTITUTION_SET);
    }

    /**
     * Performs variable substitution on all elements of the given IData[].
     *
     * @param array                 The IData[] to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param scope                 The scope against which variables are are resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param substitutionType      The type of substitution to be performed.
     * @return                      The variable substituted IData[].
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static IData[] substitute(IData[] array, String defaultValue, IData scope, boolean recurse, SubstitutionType substitutionType) throws ServiceException {
        return substitute(array, defaultValue, scope, recurse, SubstitutionType.normalize(substitutionType));
    }

    /**
     * Performs variable substitution on all elements of the given IData[].
     *
     * @param array                 The IData[] to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param scope                 The scope against which variables are are resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param substitutionTypes     The type of substitutions to be performed.
     * @return                      The variable substituted IData[].
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static IData[] substitute(IData[] array, String defaultValue, IData scope, boolean recurse, EnumSet<SubstitutionType> substitutionTypes) throws ServiceException {
        if (array == null) return null;

        IData[] output = new IData[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = substitute(array[i], defaultValue, scope, recurse, substitutionTypes);
        }

        return output;
    }
}
