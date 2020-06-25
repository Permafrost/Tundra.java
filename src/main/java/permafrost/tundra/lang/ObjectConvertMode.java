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

package permafrost.tundra.lang;

/**
 * The supported return types for the convert method.
 */
public enum ObjectConvertMode {
    STREAM(0), BYTES(1), STRING(2), BASE64(3), HEX(4);

    /**
     * The default convert mode used by Tundra.
     */
    public static final ObjectConvertMode DEFAULT_CONVERT_MODE = STREAM;

    private int value;
    private static java.util.Map<Integer, ObjectConvertMode> map = new java.util.HashMap<Integer, ObjectConvertMode>();

    ObjectConvertMode(int value) {
        this.value = value;
    }

    static {
        for (ObjectConvertMode type : ObjectConvertMode.values()) {
            map.put(type.value, type);
        }
    }

    /**
     * Returns an ConvertMode for the given integer value.
     *
     * @param value The value to be converted to an ConvertMode.
     * @return The ConvertMode representing the given value.
     */
    public static ObjectConvertMode valueOf(int value) {
        return map.get(value);
    }

    /**
     * Returns an ConvertMode for the given string value.
     *
     * @param value The value to be converted to an ConvertMode.
     * @return The ConvertMode representing the given value.
     */
    public static ObjectConvertMode normalize(String value) {
        return normalize(value == null ? (ObjectConvertMode)null : valueOf(value.trim().toUpperCase()));
    }

    /**
     * Normalizes the given ConvertMode.
     *
     * @param mode The ConvertMode to be normalized.
     * @return The default ConvertMode if mode is null, otherwise null.
     */
    public static ObjectConvertMode normalize(ObjectConvertMode mode) {
        return mode == null ? DEFAULT_CONVERT_MODE : mode;
    }
}
