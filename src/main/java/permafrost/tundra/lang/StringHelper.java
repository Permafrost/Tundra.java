/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2012 Lachlan Dowding
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

package permafrost.tundra.lang;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.math.BigDecimalHelper;
import permafrost.tundra.math.BigIntegerHelper;
import permafrost.tundra.time.DateTimeHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for working with String objects.
 */
public final class StringHelper {
    /**
     * Disallow instantiation of this class.
     */
    private StringHelper() {}

    /**
     * Normalizes the given byte[] as a string.
     *
     * @param bytes A byte[] to be converted to a string.
     * @return A string representation of the given byte[].
     */
    public static String normalize(byte[] bytes) {
        return normalize(bytes, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Converts the given byte[] as a string.
     *
     * @param bytes       A byte[] to be converted to a string.
     * @param charsetName The character set name to use.
     * @return A string representation of the given byte[].
     */
    public static String normalize(byte[] bytes, String charsetName) {
        return normalize(bytes, CharsetHelper.normalize(charsetName));
    }

    /**
     * Converts the given byte[] as a string.
     *
     * @param bytes   A byte[] to be converted to a string.
     * @param charset The character set to use.
     * @return A string representation of the given byte[].
     */
    public static String normalize(byte[] bytes, Charset charset) {
        if (bytes == null) return null;
        return new String(bytes, CharsetHelper.normalize(charset));
    }

    /**
     * Converts the given java.io.InputStream as a String, and closes the stream.
     *
     * @param inputStream A java.io.InputStream to be converted to a string.
     * @return A string representation of the given java.io.InputStream.
     * @throws IOException If the given encoding is unsupported, or if there is an error reading from the
     *                     java.io.InputStream.
     */
    public static String normalize(InputStream inputStream) throws IOException {
        return normalize(inputStream, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Converts the given java.io.InputStream as a String, and closes the stream.
     *
     * @param inputStream A java.io.InputStream to be converted to a string.
     * @param charsetName The character set to use.
     * @return A string representation of the given java.io.InputStream.
     * @throws IOException If the given encoding is unsupported, or if there is an error reading from the
     *                     java.io.InputStream.
     */
    public static String normalize(InputStream inputStream, String charsetName) throws IOException {
        return normalize(inputStream, CharsetHelper.normalize(charsetName));
    }

    /**
     * Converts the given java.io.InputStream as a String, and closes the stream.
     *
     * @param inputStream A java.io.InputStream to be converted to a string.
     * @param charset     The character set to use.
     * @return A string representation of the given java.io.InputStream.
     * @throws IOException If there is an error reading from the java.io.InputStream.
     */
    public static String normalize(InputStream inputStream, Charset charset) throws IOException {
        if (inputStream == null) return null;

        Writer writer = new StringWriter();
        StreamHelper.copy(new InputStreamReader(StreamHelper.normalize(inputStream), CharsetHelper.normalize(charset)), writer);
        return writer.toString();
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a String.
     *
     * @param object The object to be normalized to a string.
     * @return A string representation of the given object.
     * @throws IOException If the given encoding is unsupported, or if there is an error reading from the
     *                     java.io.InputStream.
     */
    public static String normalize(Object object) throws IOException {
        return normalize(object, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a String.
     *
     * @param object      The object to be normalized to a string.
     * @param charsetName The character set to use.
     * @return A string representation of the given object.
     * @throws IOException If the given encoding is unsupported, or if there is an error reading from the
     *                     java.io.InputStream.
     */
    public static String normalize(Object object, String charsetName) throws IOException {
        return normalize(object, CharsetHelper.normalize(charsetName));
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a String.
     *
     * @param object  The object to be normalized to a string.
     * @param charset The character set to use.
     * @return A string representation of the given object.
     * @throws IOException If there is an error reading from the java.io.InputStream.
     */
    public static String normalize(Object object, Charset charset) throws IOException {
        if (object == null) return null;

        String output;

        if (object instanceof byte[]) {
            output = normalize((byte[])object, charset);
        } else if (object instanceof String) {
            output = (String)object;
        } else if (object instanceof InputStream) {
            output = normalize((InputStream)object, charset);
        } else {
            throw new IllegalArgumentException("object must be a String, byte[], or java.io.InputStream");
        }

        return output;
    }

    /**
     * Normalizes the list of String, byte[], or InputStream to a String list.
     *
     * @param array   The array of objects to be normalized.
     * @param charset The character set to use.
     * @return The resulting String list representing the given array.
     * @throws IOException If there is an error reading from the java.io.InputStream.
     */
    public static String[] normalize(Object[] array, Charset charset) throws IOException {
        if (array == null) return null;

        String[] output = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = normalize(array[i], charset);
        }

        return output;
    }

    /**
     * Normalizes the list of String, byte[], or InputStream to a String list.
     *
     * @param array       The array of objects to be normalized.
     * @param charsetName The character set to use.
     * @return The resulting String list representing the given array.
     * @throws IOException If there is an error reading from the java.io.InputStream.
     */
    public static String[] normalize(Object[] array, String charsetName) throws IOException {
        return normalize(array, CharsetHelper.normalize(charsetName));
    }

    /**
     * Returns a substring starting at the given index for the given length.
     *
     * @param input  The string to be sliced.
     * @param index  The zero-based starting index of the slice.
     * @param length The length in characters of the slice.
     * @return The resulting substring.
     */
    public static String slice(String input, int index, int length) {
        if (input == null || input.equals("")) return input;

        String output = "";
        int inputLength = input.length(), endIndex = 0;

        // support reverse length
        if (length < 0) {
            // support reverse indexing
            if (index < 0) {
                endIndex = index + inputLength + 1;
            } else {
                if (index >= inputLength) index = inputLength - 1;
                endIndex = index + 1;
            }
            index = endIndex + length;
        } else {
            // support reverse indexing
            if (index < 0) index += inputLength;
            endIndex = index + length;
        }

        if (index < inputLength && endIndex > 0) {
            if (index < 0) index = 0;
            if (endIndex > inputLength) endIndex = inputLength;

            output = input.substring(index, endIndex);
        }

        return output;
    }

    /**
     * Converts a null input string to an empty string, or returns the string unmodified if not null.
     *
     * @param input    The string to be converted to an empty string if null.
     * @return         If input is null then empty string, otherwise input string unmodified.
     */
    public static String blankify(String input) {
        return blankify(input, true);
    }

    /**
     * Converts a null input string to an empty string, or returns the string unmodified if not null.
     *
     * @param input    The string to be converted to an empty string if null.
     * @param blankify If true, nulls will be converted to empty strings, else no conversion will occur.
     * @return         If blankify is true and input is null then empty string, otherwise input string unmodified.
     */
    public static String blankify(String input, boolean blankify) {
        if (!blankify) return input;
        return input == null ? "" : input;
    }

    /**
     * Converts any null strings to empty strings, or returns the strings unmodified if not null.
     *
     * @param input    The list of strings to be converted to an empty strings if null.
     * @return         The list of strings converted to empty strings if they were null.
     */
    public static String[] blankify(String[] input) {
        return blankify(input, true);
    }

    /**
     * Converts any null strings to empty strings, or returns the strings unmodified if not null.
     *
     * @param input    The list of strings to be converted to an empty strings if null.
     * @param blankify If true, nulls will be converted to empty strings, else no conversion will occur.
     * @return         The list of strings converted to empty strings if they were null.
     */
    public static String[] blankify(String input[], boolean blankify) {
        if (!blankify || input == null) return input;

        String output[] = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = blankify(input[i], blankify);
        }

        return output;
    }

    /**
     * Capitalizes the first character in either the first word or all words in the given string.
     *
     * @param string        The string to capitalize.
     * @param firstWordOnly Whether only the first word should be capitalized, or all words.
     * @return              The capitalized string.
     */
    public static String capitalize(String string, boolean firstWordOnly) {
        if (string == null) return null;

        char[] characters = string.toCharArray();
        boolean capitalize = true;

        for (int i = 0; i < characters.length; i++) {
            char character = characters[i];
            if (Character.isWhitespace(character)) {
                capitalize = true;
            } else if (capitalize) {
                characters[i] = Character.toTitleCase(character);
                capitalize = false;
                if (firstWordOnly) break;
            }
        }

        return new String(characters);
    }

    /**
     * Capitalizes the first character in either the first word or all words in each of the given
     * strings.
     *
     * @param input         The strings to capitalize.
     * @param firstWordOnly Whether only the first word should be capitalized, or all words.
     * @return              The capitalized strings.
     */
    public static String[] capitalize(String[] input, boolean firstWordOnly) {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = capitalize(input[i], firstWordOnly);
        }

        return output;
    }

    /**
     * Returns the given string as a list of characters.
     *
     * @param string The string.
     * @return The characters in the given string.
     */
    public static Character[] characters(String string) {
        if (string == null) return null;

        char[] chars = string.toCharArray();
        Character[] characters = new Character[chars.length];
        for (int i = 0; i < chars.length; i++) {
            characters[i] = chars[i];
        }

        return characters;
    }

    /**
     * Concatenates all non-null string leaf values in the given IData document.
     *
     * @param operands      An IData document containing strings to be concatenated.
     * @return              All string leaf values in the IData document concatenated together.
     */
    public static String concatenate(IData operands) {
        return concatenate(operands, null, false);
    }

    /**
     * Concatenates all non-null string leaf values in the given IData document, separated by the given separator
     * string.
     *
     * @param operands      An IData document containing strings to be concatenated.
     * @param separator     An optional separator string to be used between items of the array.
     * @return              All string leaf values in the IData document concatenated together.
     */
    public static String concatenate(IData operands, String separator) {
        return concatenate(operands, separator, false);
    }

    /**
     * Concatenates all string leaf values in the given IData document, separated by the given separator string.
     *
     * @param operands      An IData document containing strings to be concatenated.
     * @param separator     An optional separator string to be used between items of the array.
     * @param includeNulls  If true, null values will be included in the output string, otherwise they are ignored.
     * @return              All string leaf values in the IData document concatenated together.
     */
    @SuppressWarnings("unchecked")
    public static String concatenate(IData operands, String separator, boolean includeNulls) {
        return concatenate(separator, includeNulls, (String[])IDataHelper.getLeafValues(operands, String.class));
    }

    /**
     * Concatenates all given non-null strings.
     *
     * @param strings       A list of strings to be concatenated.
     * @return              All given strings concatenated together.
     */
    public static String concatenate(String ...strings) {
        return concatenate(null, false, strings);
    }

    /**
     * Concatenates all given strings, separated by the given separator string.
     *
     * @param separator     An optional separator string to be used between items of the array.
     * @param strings       A list of strings to be concatenated.
     * @return              All given strings concatenated together.
     */
    public static String concatenate(String separator, String ...strings) {
        return concatenate(separator, false, strings);
    }

    /**
     * Concatenates all given strings, separated by the given separator string.
     *
     * @param separator     An optional separator string to be used between items of the array.
     * @param includeNulls  If true, null values will be included in the output string, otherwise they are ignored.
     * @param strings       A list of strings to be concatenated.
     * @return              All given strings concatenated together.
     */
    public static String concatenate(String separator, boolean includeNulls, String ...strings) {
        if (strings == null || strings.length == 0) return includeNulls ? null : "";

        StringBuilder builder = new StringBuilder();
        boolean separatorRequired = false;

        for (String string : strings) {
            boolean includeItem = includeNulls || string != null;

            if (separator != null && separatorRequired && includeItem) builder.append(separator);

            if (includeItem) {
                builder.append(string);
                separatorRequired = true;
            }
        }

        return builder.toString();
    }

    /**
     * Returns the given string with leading and trailing whitespace removed.
     *
     * @param string The string to be trimmed.
     * @return The trimmed string.
     */
    public static String trim(String string) {
        String output = null;
        if (string != null) output = string.trim();
        return output;
    }

    /**
     * Returns the length or number of characters of the string.
     *
     * @param string The string to be measured.
     * @return The length of the given string.
     */
    public static int length(String string) {
        int length = 0;
        if (string != null) length = string.length();
        return length;
    }

    /**
     * Returns all the groups captured by the given regular expression pattern in the given string.
     *
     * @param string  The string to match against the regular expression.
     * @param pattern The regular expression pattern.
     * @return The capture groups from the regular expression pattern match against the string.
     */
    public static IData[] capture(String string, String pattern) {
        if (string == null || pattern == null) return null;

        List<IData> captures = new ArrayList<IData>();
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(string);

        while (matcher.find()) {
            int count = matcher.groupCount();
            List<IData> groups = new ArrayList<IData>(count);
            for (int i = 0; i <= count; i++) {
                int index = matcher.start(i);
                int length = matcher.end(i) - index;
                String content = matcher.group(i);
                boolean captured = index >= 0;

                IData group = IDataFactory.create();
                IDataCursor groupCursor = group.getCursor();
                IDataUtil.put(groupCursor, "captured?", "" + captured);
                if (captured) {
                    IDataUtil.put(groupCursor, "index", "" + index);
                    IDataUtil.put(groupCursor, "length", "" + length);
                    IDataUtil.put(groupCursor, "content", content);
                }
                groupCursor.destroy();
                groups.add(group);
            }

            IData capture = IDataFactory.create();
            IDataCursor captureCursor = capture.getCursor();
            IDataUtil.put(captureCursor, "groups", groups.toArray(new IData[groups.size()]));
            IDataUtil.put(captureCursor, "groups.length", "" + groups.size());
            captureCursor.destroy();
            captures.add(capture);
        }

        return captures.toArray(new IData[captures.size()]);
    }

    /**
     * Returns true if the given regular expression pattern is found anywhere in the given string.
     *
     * @param string  The string to match against the regular expression.
     * @param pattern The regular expression pattern.
     * @return True if the regular expression pattern was found anywhere in the given string.
     */
    public static boolean find(String string, String pattern) {
        return find(string, pattern, false);
    }

    /**
     * /** Returns true if the given pattern is found anywhere in the given string.
     *
     * @param string  The string to match against the regular expression.
     * @param pattern The literal of regular expression pattern.
     * @param literal Whether the pattern is a literal pattern or a regular expression.
     * @return True if the  pattern was found anywhere in the given string.
     */
    public static boolean find(String string, String pattern, boolean literal) {
        boolean found = false;
        if (string != null && pattern != null) {
            if (literal) {
                found = string.contains(pattern);
            } else {
                Pattern regex = Pattern.compile(pattern);
                Matcher matcher = regex.matcher(string);
                found = matcher.find();
            }
        }
        return found;
    }

    /**
     * Returns true if the given regular expression pattern matches the entirety of the given string.
     *
     * @param string  The string to match against the regular expression.
     * @param pattern The regular expression pattern.
     * @return True if the regular expression matches the entirety of the given string.
     */
    public static boolean match(String string, String pattern) {
        return match(string, pattern, false);
    }

    /**
     * Returns true if the pattern matches the entirety of the given string.
     *
     * @param string  The string to match against the regular expression.
     * @param pattern The literal or regular expression pattern.
     * @param literal Whether the pattern is a literal pattern or a regular expression.
     * @return True if the pattern matches the entirety of the given string.
     */
    public static boolean match(String string, String pattern, boolean literal) {
        boolean match = false;
        if (string != null && pattern != null) {
            if (literal) {
                match = string.equals(pattern);
            } else {
                match = string.matches(pattern);
            }
        }
        return match;
    }

    /**
     * Removes all occurrences of the given regular expression in the given string.
     *
     * @param string  The string to remove the pattern from.
     * @param pattern The regular expression pattern to be removed.
     * @return The given string with all occurrences of the given pattern removed.
     */
    public static String remove(String string, String pattern) {
        return remove(string, pattern, false);
    }

    /**
     * Removes either the first or all occurrences of the given regular expression in the given string.
     *
     * @param string    The string to remove the pattern from.
     * @param pattern   The regular expression pattern to be removed.
     * @param firstOnly If true, only the first occurrence is removed, otherwise all occurrences are removed.
     * @return The given string with either the first or all occurrences of the given pattern removed.
     */
    public static String remove(String string, String pattern, boolean firstOnly) {
        return replace(string, pattern, "", true, firstOnly);
    }

    /**
     * Replaces all occurrences of the given regular expression in the given string with the given replacement.
     *
     * @param string      The string to be replaced.
     * @param pattern     The regular expression pattern.
     * @param replacement The replacement string.
     * @param literal     Whether the replacement string is literal and therefore requires quoting.
     * @return The replaced string.
     */
    public static String replace(String string, String pattern, String replacement, boolean literal) {
        return replace(string, pattern, replacement, literal, false);
    }

    /**
     * Replaces either the first or all occurrences of the given regular expression in the given string with the given
     * replacement.
     *
     * @param string      The string to be replaced.
     * @param pattern     The regular expression pattern.
     * @param replacement The replacement string.
     * @param literal     Whether the replacement string is literal and therefore requires quoting.
     * @param firstOnly   If true, only the first occurrence is replaced, otherwise all occurrences are replaced.
     * @return The replaced string.
     */
    public static String replace(String string, String pattern, String replacement, boolean literal, boolean firstOnly) {
        String output = string;
        if (string != null && pattern != null && replacement != null) {
            if (literal) replacement = Matcher.quoteReplacement(replacement);
            Matcher matcher = Pattern.compile(pattern).matcher(string);
            if (firstOnly) {
                output = matcher.replaceFirst(replacement);
            } else {
                output = matcher.replaceAll(replacement);
            }
        }
        return output;
    }

    /**
     * Splits a string around each match of the given regular expression pattern.
     *
     * @param string  The string to be split.
     * @param pattern The regular expression pattern to split around.
     * @return The array of strings computed by splitting the given string around matches of this pattern.
     */
    public static String[] split(String string, String pattern) {
        return split(string, pattern, false);
    }

    /**
     * Splits a string around each match of the given pattern.
     *
     * @param string  The string to be split.
     * @param pattern The literal or regular expression pattern to split around.
     * @param literal Whether the pattern is a literal pattern or a regular expression.
     * @return The array of strings computed by splitting the given string around matches of this pattern.
     */
    public static String[] split(String string, String pattern, boolean literal) {
        String[] output = null;
        if (string != null && pattern != null) {
            if (literal) pattern = quote(pattern);
            output = Pattern.compile(pattern).split(string);
        } else if (string != null) {
            output = new String[1];
            output[0] = string;
        }
        return output;
    }

    /**
     * Returns all the lines in the given string as an array.
     *
     * @param string The string to be split into lines.
     * @return The array of lines from the given string.
     */
    public static String[] lines(String string) {
        return split(string, "\n");
    }

    /**
     * Trims the given string of leading and trailing whitespace, and optionally replaces runs of whitespace characters
     * with a single space character.
     *
     * @param string   The string to be squeezed.
     * @param internal Whether runs of whitespace characters should be replaced with a single space character.
     * @return The squeezed string.
     */
    public static String squeeze(String string, boolean internal) {
        if (string == null) return null;

        string = string.trim();
        if (internal) string = replace(string, "\\s+", " ", false);

        return string.equals("") ? null : string;
    }

    /**
     * Trims the given string of leading and trailing whitespace, and replaces runs of whitespace characters with a
     * single space character.
     *
     * @param string The string to be squeezed.
     * @return The squeezed string.
     */
    public static String squeeze(String string) {
        return squeeze(string, true);
    }

    /**
     * Returns a literal regular expression pattern for the given string.
     *
     * @param string The string to quote.
     * @return A regular expression pattern which literally matches the given string.
     */
    public static String quote(String string) {
        if (string == null) return null;
        return Pattern.quote(string);
    }

    /**
     * Returns a regular expression pattern that matches any of the values in the given string list.
     *
     * @param array The list of strings to be matched.
     * @return A regular expression which literally matches any of the given strings.
     */
    public static String quote(String[] array) {
        if (array == null) return null;

        int last = array.length - 1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i == 0) builder.append("(");
            builder.append(quote(array[i]));
            if (i < last) builder.append("|");
            if (i == last) builder.append(")");
        }

        return builder.toString();
    }

    /**
     * Pads a string with the given character to the given length.
     *
     * @param string    The string to pad.
     * @param length    The desired length of the string. If less than 0 the string is padded right to left, otherwise
     *                  it is padded from left to right.
     * @param character The character to pad the string with.
     * @return          The padded string.
     */
    public static String pad(String string, int length, char character) {
        if (string == null) string = "";

        boolean left = length >= 0;
        if (length < 0) length = length * -1;

        if (string.length() >= length) return string;

        StringBuilder builder = new StringBuilder(length);

        if (!left) builder.append(string);
        for (int i = string.length(); i < length; i++) {
            builder.append(character);
        }
        if (left) builder.append(string);

        return builder.toString();
    }

    /**
     * Pads each string in the given list with the given character to the given length.
     *
     * @param input     The list of strings to be padded.
     * @param length    The desired length of the strings. If less than 0 the strings are padded right to left, otherwise
     *                  they are padded from left to right.
     * @param character The character to pad the strings with.
     * @return          The list of padded strings.
     */
    public static String[] pad(String[] input, int length, char character) {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = pad(input[i], length, character);
        }

        return output;
    }

    /**
     * Compares two strings lexicographically.
     *
     * @param string1 The first string to compare.
     * @param string2 The second string to compare.
     * @return        Less than 0 if the first string is less than the second string, equal to 0 if the two strings are equal,
     *                or greater than 0 if the first string is greater than the second string.
     */
    public static int compare(String string1, String string2) {
        return compare(string1, string2, false, false);
    }

    /**
     * Compares two strings lexicographically.
     *
     * @param string1         The first string to compare.
     * @param string2         The second string to compare.
     * @param caseInsensitive Whether the comparison should be case insensitive.
     * @return                Less than 0 if the first string is less than the second string, equal to 0 if the two strings are equal,
     *                        or greater than 0 if the first string is greater than the second string.
     */
    public static int compare(String string1, String string2, boolean caseInsensitive) {
        return compare(string1, string2, caseInsensitive, false);
    }

    /**
     * Compares two strings lexicographically.
     *
     * @param string1               The first string to compare.
     * @param string2               The second string to compare.
     * @param caseInsensitive       Whether the comparison should be case insensitive.
     * @param whitespaceInsensitive Whether the comparison should be whitespace insensitive.
     * @return                      Less than 0 if the first string is less than the second string, equal to 0 if the two strings are equal,
     *                              or greater than 0 if the first string is greater than the second string.
     */
    public static int compare(String string1, String string2, boolean caseInsensitive, boolean whitespaceInsensitive) {
        if (string1 == null && string2 == null) return 0;
        if (string1 == null) return -1;
        if (string2 == null) return 1;

        if (whitespaceInsensitive) {
            string1 = string1.replaceAll("\\s", "");
            string2 = string2.replaceAll("\\s", "");
        }

        if (caseInsensitive) {
            return string1.compareToIgnoreCase(string2);
        } else {
            return string1.compareTo(string2);
        }
    }

    /**
     * Returns a formatted string using the specified pattern and arguments.
     *
     * @param locale    The locale to apply during formatting. If null then no localization is applied.
     * @param pattern   A format string, as per http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html.
     * @param scope     An IData document which contains the argument values referenced by the given argument keys.
     * @param arguments The list of arguments to be fetched from the record and normalized to the specified types.
     * @return          A formatted string.
     */
    public static String format(Locale locale, String pattern, IData[] arguments, IData scope) {
        return format(locale, pattern, arguments, null, scope);
    }

    /**
     * Returns a formatted string using the specified pattern and arguments.
     *
     * @param locale    The locale to apply during formatting. If null then no localization is applied.
     * @param pattern   A format string, as per http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html.
     * @param scope     An IData document which contains the argument values referenced by the given argument keys.
     * @param arguments The list of arguments to be fetched from the record and normalized to the specified types.
     * @param index     The zero-based array index for this record if it is part of a list that is being formatted.
     * @return          A formatted string.
     */
    public static String format(Locale locale, String pattern, IData[] arguments, IData scope, int index) {
        return format(locale, pattern, arguments, null, scope, 0);
    }

    /**
     * Returns a formatted string using the specified pattern and arguments.
     *
     * @param locale    The locale to apply during formatting. If null then no localization is applied.
     * @param pattern   A format string, as per http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html.
     * @param pipeline  The pipeline against which absolute argument keys are resolved.
     * @param scope     An IData document which contains the argument values referenced by the given argument keys.
     * @param arguments The list of arguments to be fetched from the record and normalized to the specified types.
     * @return          A formatted string.
     */
    public static String format(Locale locale, String pattern, IData[] arguments, IData pipeline, IData scope) {
        return format(locale, pattern, arguments, pipeline, scope, 0);
    }

    /**
     * Returns a formatted string using the specified pattern and arguments.
     *
     * @param locale    The locale to apply during formatting. If null then no localization is applied.
     * @param pattern   A format string, as per http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html.
     * @param pipeline  The pipeline against which absolute argument keys are resolved.
     * @param scope     An IData document which contains the argument values referenced by the given argument keys.
     * @param arguments The list of arguments to be fetched from the record and normalized to the specified types.
     * @param index     The zero-based array index for this record if it is part of a list that is being formatted.
     * @return          A formatted string.
     */
    public static String format(Locale locale, String pattern, IData[] arguments, IData pipeline, IData scope, int index) {
        if (pattern == null || arguments == null || scope == null) return null;

        List<Object> args = new ArrayList<Object>(arguments == null? 0 : arguments.length);

        for (IData argument : arguments) {
            if (argument != null) {
                IDataCursor cursor = argument.getCursor();

                String key = IDataUtil.getString(cursor, "key");
                Object value = IDataUtil.get(cursor, "value");
                String type = IDataUtil.getString(cursor, "type");
                String argPattern = IDataUtil.getString(cursor, "pattern");
                boolean blankify = BooleanHelper.parse(IDataUtil.getString(cursor, "blankify?"));

                cursor.destroy();

                if (key != null && value == null) {
                    value = IDataHelper.get(pipeline, scope, key);
                    if (value == null) {
                        if (key.equals("$index")) {
                            value = index;
                        } else if (key.equals("$iteration")) {
                            value = index + 1;
                        }
                    }
                }

                if (value != null) {
                    if (type == null || type.equalsIgnoreCase("string")) {
                        value = value.toString();
                    } else if (type.equalsIgnoreCase("integer")) {
                        value = BigIntegerHelper.normalize(value);
                    } else if (type.equalsIgnoreCase("decimal")) {
                        value = BigDecimalHelper.normalize(value);
                    } else if (type.equalsIgnoreCase("datetime")) {
                        value = DateTimeHelper.normalize(value, argPattern);
                    }
                } else if (blankify) {
                    if (type == null || type.equalsIgnoreCase("string")) {
                        value = "";
                    } else if (type.equalsIgnoreCase("integer")) {
                        value = BigInteger.ZERO;
                    } else if (type.equalsIgnoreCase("decimal")) {
                        value = BigDecimal.ZERO;
                    }
                }

                args.add(value);
            }
        }

        return String.format(locale, pattern, args.toArray(new Object[args.size()]));
    }

    /**
     * Returns a formatted string produced by formatting each given record using the specified pattern and
     * arguments, separated by the given separator string.
     *
     * @param locale            The locale to apply during formatting. If null then no localization is applied.
     * @param pattern           A format string, as per http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html.
     * @param arguments         The list of arguments to be fetched from each record and normalized to the specified types.
     * @param recordSeparator   An optional string for separating each formatted record in the resulting string.
     * @param records           An IData[] document list where each item contains a set of argument values.
     * @return                  A formatted string.
     */
    public static String format(Locale locale, String pattern, IData[] arguments, String recordSeparator, IData ... records) {
        return format(locale, pattern, arguments, null, recordSeparator, records);
    }

    /**
     * Returns a formatted string produced by formatting each given record using the specified pattern and
     * arguments, separated by the given separator string.
     *
     * @param locale            The locale to apply during formatting. If null then no localization is applied.
     * @param pattern           A format string, as per http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html.
     * @param arguments         The list of arguments to be fetched from each record and normalized to the specified types.
     * @param pipeline          The pipeline against which absolute argument keys are resolved.
     * @param recordSeparator   An optional string for separating each formatted record in the resulting string.
     * @param records           An IData[] document list where each item contains a set of argument values.
     * @return                  A formatted string.
     */
    public static String format(Locale locale, String pattern, IData[] arguments, IData pipeline, String recordSeparator, IData ... records) {
        if (pattern == null || arguments == null || records == null) return null;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < records.length; i++) {
            builder.append(format(locale, pattern, arguments, pipeline, records[i], i));
            if (recordSeparator != null) builder.append(recordSeparator);
        }

        return builder.toString();
    }

    /**
     * Returns null if the given string only contains whitespace characters.
     *
     * @param input   The string to be nullified.
     * @return        Null if the given string only contains whitespace characters, otherwise the given string unmodified.
     */
    public static String nullify(String input) {
        return nullify(input, true);
    }

    /**
     * Returns null if the given string only contains whitespace characters.
     *
     * @param input   The string to be nullified.
     * @param nullify If true, the string will be nullified.
     * @return        Null if the given string only contains whitespace characters, otherwise the given string unmodified.
     */
    public static String nullify(String input, boolean nullify) {
        return (input == null || input.trim().equals("")) ? null : input;
    }

    /**
     * Converts each string in the given list to null if it only contains whitespace characters.
     *
     * @param input   The string list to be nullified.
     * @return        The nullified list of strings.
     */
    public static String[] nullify(String[] input) {
        return nullify(input, true);
    }

    /**
     * Converts each string in the given list to null if it only contains whitespace characters.
     *
     * @param input   The string list to be nullified.
     * @param nullify If true, the list will be nullified.
     * @return        The nullified list of strings.
     */
    public static String[] nullify(String[] input, boolean nullify) {
        if (!nullify || input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = nullify(input[i], nullify);
        }

        return output;
    }
}
