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
     * with the associated value from the given scope.
     *
     * @param substitutionString A string to perform variable substitution on.
     * @param scopes             One or more IData documents containing the variables being substituted.
     * @return                   The string after variable substitution has been performed.
     * @throws ServiceException  If an error occurs retrieving a global variable.
     */
    public static String substitute(String substitutionString, IData... scopes) throws ServiceException {
        return substitute(substitutionString, null, scopes);
    }

    /**
     * Performs variable substitution on the given string by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param substitutionString A string to perform variable substitution on.
     * @param defaultValue       A default value to be substituted when the variable being substituted has a value of
     *                           null.
     * @param scopes             One or more IData documents containing the variables being substituted.
     * @return                   The string after variable substitution has been performed.
     * @throws ServiceException  If an error occurs retrieving a global variable.
     */
    public static String substitute(String substitutionString, String defaultValue, IData... scopes) throws ServiceException {
        return substitute(substitutionString, defaultValue, SubstitutionType.DEFAULT_SUBSTITUTION_SET, scopes);
    }

    /**
     * Performs variable substitution on the given string by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param substitutionString A string to perform variable substitution on.
     * @param defaultValue       A default value to be substituted when the variable being substituted has a value of
     *                           null.
     * @param substitutionType   The type of substitution to perform.
     * @param scopes             One or more IData documents containing the variables being substituted.
     * @return                   The string after variable substitution has been performed.
     * @throws ServiceException  If an error occurs retrieving a global variable.
     */
    public static String substitute(String substitutionString, String defaultValue, SubstitutionType substitutionType, IData... scopes) throws ServiceException {
        return substitute(substitutionString, defaultValue, SubstitutionType.normalize(substitutionType), scopes);
    }

    /**
     * Performs variable substitution on the given string by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param substitutionString A string to perform variable substitution on.
     * @param defaultValue       A default value to be substituted when the variable being substituted has a value of
     *                           null.
     * @param substitutionTypes  The type of substitutions to perform.
     * @param scopes             One or more IData documents containing the variables being substituted.
     * @return                   The string after variable substitution has been performed.
     * @throws ServiceException  If an error occurs retrieving a global variable.
     */
    public static String substitute(String substitutionString, String defaultValue, EnumSet<SubstitutionType> substitutionTypes, IData... scopes) throws ServiceException {
        if (substitutionString == null || scopes == null || scopes.length == 0) return substitutionString;
        substitutionTypes = SubstitutionType.normalize(substitutionTypes);

        Matcher matcher = matcher(substitutionString);
        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = null;

            if (substitutionTypes.contains(SubstitutionType.GLOBAL)) {
                String globalValue = GlobalVariableHelper.get(key);
                if (globalValue != null) value = globalValue;
            }

            if (substitutionTypes.contains(SubstitutionType.LOCAL)) {
                for (IData scope : scopes) {
                    String localValue = IDataHelper.get(scope, key, String.class);
                    if (localValue != null) {
                        value = localValue;
                        break;
                    }
                }
            }

            if (value != null) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(value));
            } else if (defaultValue != null) {
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
     * @param scopes             One or more IData documents containing the variables being substituted.
     * @return                      The String[] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[] substitute(String[] array, String defaultValue, IData... scopes) throws ServiceException {
        return substitute(array, defaultValue, SubstitutionType.DEFAULT_SUBSTITUTION_SET, scopes);
    }

    /**
     * Performs variable substitution on the given String[] by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param array                 A String[] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value
     *                              of null.
     * @param substitutionType      The type of substitution to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The String[] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[] substitute(String[] array, String defaultValue, SubstitutionType substitutionType, IData... scopes) throws ServiceException {
        return substitute(array, defaultValue, SubstitutionType.normalize(substitutionType), scopes);
    }

    /**
     * Performs variable substitution on the given String[] by replacing all occurrences of substrings matching "%key%"
     * with the associated value from the given scope; if the key has no value, the given defaultValue (if not null) is
     * used instead.
     *
     * @param array                 A String[] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value
     *                              of null.
     * @param substitutionTypes     The type of substitutions to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The String[] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[] substitute(String[] array, String defaultValue, EnumSet<SubstitutionType> substitutionTypes, IData... scopes) throws ServiceException {
        if (array == null) return null;

        String[] output = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = substitute(array[i], defaultValue, substitutionTypes, scopes);
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
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The String[][] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[][] substitute(String[][] table, String defaultValue, IData... scopes) throws ServiceException {
        return substitute(table, defaultValue, SubstitutionType.DEFAULT_SUBSTITUTION_SET, scopes);
    }

    /**
     * Performs variable substitution on the given String[][] by replacing all occurrences of substrings matching
     * "%key%" with the associated value from the given scope; if the key has no value, the given defaultValue (if not
     * null) is used instead.
     *
     * @param table                 A String[][] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value of null.
     * @param substitutionType      The type of substitution to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The String[][] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[][] substitute(String[][] table, String defaultValue, SubstitutionType substitutionType, IData... scopes) throws ServiceException {
        return substitute(table, defaultValue, SubstitutionType.normalize(substitutionType), scopes);
    }

    /**
     * Performs variable substitution on the given String[][] by replacing all occurrences of substrings matching
     * "%key%" with the associated value from the given scope; if the key has no value, the given defaultValue (if not
     * null) is used instead.
     *
     * @param table                 A String[][] to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted has a value of null.
     * @param substitutionTypes     The type of substitutions to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The String[][] after variable substitution has been performed.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static String[][] substitute(String[][] table, String defaultValue, EnumSet<SubstitutionType> substitutionTypes, IData... scopes) throws ServiceException {
        if (table == null) return null;

        String[][] output = new String[table.length][];

        for (int i = 0; i < table.length; i++) {
            output[i] = substitute(table[i], defaultValue, substitutionTypes, scopes);
        }

        return output;
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document          The IData document to perform variable substitution on.
     * @return                  The variable substituted IData.
     * @throws ServiceException If an error occurs.
     */
    public static IData substitute(IData document) throws ServiceException {
        return substitute(document, true);
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document          The IData document to perform variable substitution on.
     * @param recurse           Whether embedded IData and IData[] should have variable substitution recursively
     *                          performed on them.
     * @param scopes            One or more IData documents containing the variables being substituted.
     * @return                  The variable substituted IData.
     * @throws ServiceException If an error occurs.
     */
    public static IData substitute(IData document, boolean recurse, IData... scopes) throws ServiceException {
        return substitute(document, null, recurse, scopes);
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document          The IData document to perform variable substitution on.
     * @param defaultValue      The value to substitute if a variable cannot be resolved.
     * @param recurse           Whether embedded IData and IData[] should have variable substitution recursively
     *                          performed on them.
     * @param scopes            One or more IData documents containing the variables being substituted.
     * @return                  The variable substituted IData.
     * @throws ServiceException If an error occurs.
     */
    public static IData substitute(IData document, String defaultValue, boolean recurse, IData... scopes) throws ServiceException {
        return substitute(document, defaultValue, recurse, SubstitutionType.DEFAULT_SUBSTITUTION_SET, scopes);
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document              The IData document to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param substitutionType      The type of substitution to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The variable substituted IData.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static IData substitute(IData document, String defaultValue, boolean recurse, SubstitutionType substitutionType, IData... scopes) throws ServiceException {
        return substitute(document, defaultValue, recurse, SubstitutionType.normalize(substitutionType), scopes);
    }

    /**
     * Performs variable substitution on all elements of the given IData input document.
     *
     * @param document              The IData document to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param substitutionTypes     The type of substitutions to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The variable substituted IData.
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static IData substitute(IData document, String defaultValue, boolean recurse, EnumSet<SubstitutionType> substitutionTypes, IData... scopes) throws ServiceException {
        if (document == null) return null;
        if (scopes == null || scopes.length == 0) {
            scopes = new IData[1];
            scopes[0] = document;
        }

        IData output = IDataFactory.create();
        IDataCursor inputCursor = document.getCursor();
        IDataCursor outputCursor = output.getCursor();

        while (inputCursor.next()) {
            String key = inputCursor.getKey();
            Object value = inputCursor.getValue();

            if (value != null) {
                if (recurse && (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[])) {
                    value = substitute(IDataHelper.toIDataArray(value), defaultValue, recurse, substitutionTypes, scopes);
                } else if (recurse && (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable)) {
                    value = substitute(IDataHelper.toIData(value), defaultValue, recurse, substitutionTypes, scopes);
                } else if (value instanceof String) {
                    value = substitute((String)value, defaultValue, substitutionTypes, scopes);
                } else if (value instanceof String[]) {
                    value = substitute((String[])value, defaultValue, substitutionTypes, scopes);
                } else if (value instanceof String[][]) {
                    value = substitute((String[][])value, defaultValue, substitutionTypes, scopes);
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
     * @param array             The IData[] to perform variable substitution on.
     * @param defaultValue      The value to substitute if a variable cannot be resolved.
     * @param recurse           Whether embedded IData and IData[] should have variable substitution recursively
     *                          performed on them.
     * @param scopes            One or more IData documents containing the variables being substituted.
     * @return                  The variable substituted IData[].
     * @throws ServiceException If an error occurs.
     */
    public static IData[] substitute(IData[] array, String defaultValue, boolean recurse, IData... scopes) throws ServiceException {
        return substitute(array, defaultValue, recurse, SubstitutionType.DEFAULT_SUBSTITUTION_SET, scopes);
    }

    /**
     * Performs variable substitution on all elements of the given IData[].
     *
     * @param array                 The IData[] to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param substitutionType      The type of substitution to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The variable substituted IData[].
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static IData[] substitute(IData[] array, String defaultValue, boolean recurse, SubstitutionType substitutionType, IData... scopes) throws ServiceException {
        return substitute(array, defaultValue, recurse, SubstitutionType.normalize(substitutionType), scopes);
    }

    /**
     * Performs variable substitution on all elements of the given IData[].
     *
     * @param array                 The IData[] to perform variable substitution on.
     * @param defaultValue          The value to substitute if a variable cannot be resolved.
     * @param recurse               Whether embedded IData and IData[] should have variable substitution recursively
     *                              performed on them.
     * @param substitutionTypes     The type of substitutions to be performed.
     * @param scopes                One or more IData documents containing the variables being substituted.
     * @return                      The variable substituted IData[].
     * @throws ServiceException     If an error occurs retrieving a global variable.
     */
    public static IData[] substitute(IData[] array, String defaultValue, boolean recurse, EnumSet<SubstitutionType> substitutionTypes, IData... scopes) throws ServiceException {
        if (array == null) return null;

        IData[] output = new IData[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = substitute(array[i], defaultValue, recurse, substitutionTypes, scopes);
        }

        return output;
    }
}
