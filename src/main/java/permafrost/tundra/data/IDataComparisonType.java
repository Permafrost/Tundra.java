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

/**
 * The different types of IData value comparison supported by the IDataComparator class.
 */
public enum IDataComparisonType {
    OBJECT(0), STRING(1), INTEGER(2), DECIMAL(3), DATETIME(4), DURATION(5);

    private int value;
    private static java.util.Map<Integer, IDataComparisonType> map = new java.util.HashMap<Integer, IDataComparisonType>();

    private IDataComparisonType(int value) {
        this.value = value;
    }

    static {
        for (IDataComparisonType type : IDataComparisonType.values()) {
            map.put(type.value, type);
        }
    }

    /**
     * Returns an IDataKeyComparisonType for the given integer value.
     * @param value The value to be converted to an IDataKeyComparisonType.
     * @return      The IDataKeyComparisonType representing the given value.
     */
    public static IDataComparisonType valueOf(int value) {
        return map.get(value);
    }

    /**
     * Returns an IDataKeyComparisonType for the given string value.
     * @param value The value to be converted to an IDataKeyComparisonType.
     * @return      The IDataKeyComparisonType representing the given value.
     */
    public static IDataComparisonType normalize(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}