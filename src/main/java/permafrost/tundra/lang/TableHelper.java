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

package permafrost.tundra.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A collection of convenience methods for working with two dimensional arrays.
 */
public class TableHelper {
    /**
     * The default separator string between rows of a table when converting it to a string.
     */
    public final static String DEFAULT_ROW_SEPARATOR = ", ";

    /**
     * Disallow instantiation of this class.
     */
    private TableHelper() {}

    /**
     * Returns a new table with all null elements removed.
     *
     * @param table The two dimensional array to be compacted.
     * @param <T>   The class of the items in the array.
     * @return      A copy of the given array with all null items removed.
     */
    public static <T> T[][] compact(T[][] table) {
        if (table == null) return null;

        List<T[]> list = new ArrayList<T[]>(table.length);

        for (T[] row : table) {
            if (row != null) list.add(ArrayHelper.compact(row));
        }

        return list.toArray(Arrays.copyOf(table, list.size()));
    }

    /**
     * Returns a string created by concatenating each element of the given table, separated by the given separator
     * string.
     *
     * @param table             The table whose contents are to be joined.
     * @param rowSeparator      An optional separator string to be used between rows of the table.
     * @param itemSeparator     An optional separator string to be used between items of the table.
     * @param tableDefaultValue An optional value used when the table is null or empty.
     * @param rowDefaultValue   An optional value used when a table row is null or empty.
     * @param includeNulls      If true, null values will be included in the output string, otherwise they are ignored.
     * @param <T>               The class of items stored in the table.
     * @return                  A string representation of the given table created by concatenating together the string
     *                          representation of each item in order, optionally separated by the given separator string.
     */
    public static <T> String join(T[][] table, String rowSeparator, String itemSeparator, String tableDefaultValue, String rowDefaultValue, boolean includeNulls) {
        if (!includeNulls) table = compact(table);
        if (table == null || table.length == 0) return tableDefaultValue;

        StringBuilder builder = new StringBuilder();
        join(table, rowSeparator, itemSeparator, rowDefaultValue, builder);
        return builder.toString();
    }

    /**
     * Returns a string created by concatenating each element of the given table, separated by the given separator
     * string.
     *
     * @param table             The table whose contents are to be joined.
     * @param rowSeparator      An optional separator string to be used between rows of the table.
     * @param itemSeparator     An optional separator string to be used between items of the table.
     * @param rowDefaultValue   An optional value used when a table row is null or empty.
     * @param <T>               The class of items stored in the table.
     */
    private static <T> void join(T[][] table, String rowSeparator, String itemSeparator, String rowDefaultValue, StringBuilder builder) {
        if (table != null && builder != null) {
            for (int i = 0; i < table.length; i++) {
                if (rowSeparator != null && i > 0) builder.append(rowSeparator);
                if (table[i] == null) {
                    builder.append(rowDefaultValue);
                } else {
                    ArrayHelper.join(table[i], itemSeparator, builder);
                }
            }
        }
    }

    /**
     * Returns a string representation of the given table.
     *
     * @param table             The table to be stringified.
     * @param <T>               The class of items stored in the array.
     * @return                  A string representation of the given array.
     */
    public static <T> String stringify(T[][] table) {
        return stringify(table, null, null, true);
    }

    /**
     * Returns a string representation of the given table.
     *
     * @param table             The table to be stringified.
     * @param itemSeparator     The separator string to be used between items of a row.
     * @param includeNulls      Whether null values should be included in the output.
     * @param <T>               The class of items stored in the array.
     * @return                  A string representation of the given array.
     */
    public static <T> String stringify(T[][] table, String itemSeparator, boolean includeNulls) {
        return stringify(table, null, itemSeparator, includeNulls);
    }

    /**
     * Returns a string representation of the given table.
     *
     * @param table             The table to be stringified.
     * @param rowSeparator      The separator string to be used between rows of the table.
     * @param itemSeparator     The separator string to be used between items of a row.
     * @param includeNulls      Whether null values should be included in the output.
     * @param <T>               The class of items stored in the array.
     * @return                  A string representation of the given array.
     */
    public static <T> String stringify(T[][] table, String rowSeparator, String itemSeparator, boolean includeNulls) {
        if (!includeNulls) table = compact(table);
        if (table == null) return null;

        StringBuilder builder = new StringBuilder();
        stringify(table, rowSeparator, itemSeparator, builder);
        return builder.toString();
    }

    /**
     * Returns a string representation of the given table.
     *
     * @param table             The table to be stringified.
     * @param rowSeparator      The separator string to be used between rows of the table.
     * @param itemSeparator     The separator string to be used between items of a row.
     * @param builder           The string builder to use when building the string.
     * @param <T>               The class of items stored in the array.
     */
    private static <T> void stringify(T[][] table, String rowSeparator, String itemSeparator, StringBuilder builder) {
        if (rowSeparator == null) rowSeparator = DEFAULT_ROW_SEPARATOR;
        if (itemSeparator == null) itemSeparator = ArrayHelper.DEFAULT_ITEM_SEPARATOR;

        builder.append("[");
        for (int i = 0; i < table.length; i++) {
            if (i > 0) builder.append(rowSeparator);

            if (table[i] == null) {
                builder.append(table[i]);
            } else {
                ArrayHelper.stringify(table[i], itemSeparator, builder);
            }
        }
        builder.append("]");
    }

    /**
     * Returns a new table with all empty or null elements removed.
     *
     * @param table A table to be squeezed.
     * @param <T>   The type of item in the table.
     * @return A new table that is the given table squeezed.
     */
    private static <T> T[][] squeeze(T[][] table) {
        if (table == null || table.length == 0) return null;

        List<T[]> list = new ArrayList<T[]>(table.length);

        for (T[] row : table) {
            row = ArrayHelper.squeeze(row);
            if (row != null) list.add(row);
        }

        table = list.toArray(Arrays.copyOf(table, list.size()));

        return table.length == 0 ? null : table;
    }

    /**
     * Converts the given two dimensional array of objects to a string table.
     *
     * @param table The two dimensional array to be converted.
     * @param <T>   The type of item in the given array.
     * @return The converted string table.
     */
    public static <T> String[][] toStringTable(T[][] table) {
        if (table == null) return null;

        String[][] stringTable = new String[table.length][];
        for (int i = 0; i < table.length; i++) {
            stringTable[i] = ArrayHelper.toStringArray(table[i]);
        }

        return stringTable;
    }
}
