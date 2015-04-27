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
 * A collection of convenience methods for working with IDs.
 */
public class IdentityHelper {
    /**
     * Disallow instantiation of this class.
     */
    private IdentityHelper() {}

    /**
     * Returns a new random UUID as a string.
     * @return     The new random UUID.
     */
    public static String generate() {
        return generate((ObjectHelper.ConvertMode)null);
    }

    /**
     * Returns a new random UUID as a string.
     * @param mode Determines how the UUID is represented, either as a
     *             UUID string or as a base64-encoded byte array.
     * @return     The new random UUID.
     */
    public static String generate(String mode) {
        return generate(mode == null ? (ObjectHelper.ConvertMode)null : ObjectHelper.ConvertMode.normalize(mode));
    }

    /**
     * Returns a new random UUID as a string.
     * @param mode Determines how the UUID is represented, either as a
     *             UUID string or as a base64-encoded byte array.
     * @return     The new random UUID.
     */
    public static String generate(ObjectHelper.ConvertMode mode) {
        java.util.UUID uuid = java.util.UUID.randomUUID();
        String id = null;

        if (mode == null) mode = ObjectHelper.ConvertMode.STRING;

        if (mode == ObjectHelper.ConvertMode.STRING) {
            id = uuid.toString();
        } else if (mode == ObjectHelper.ConvertMode.BASE64) {
            long mostSignificantBits = uuid.getMostSignificantBits();
            long leastSignificantBits = uuid.getLeastSignificantBits();
            byte[] bytes = new byte[16];

            for (int i = 0; i < 8; i++) {
                bytes[i] = (byte)(mostSignificantBits >>> 8 * (7 - i));
            }
            for (int i = 8; i < 16; i++) {
                bytes[i] = (byte)(leastSignificantBits >>> 8 * (7 - i));
            }
            id = ByteHelper.base64Encode(bytes);
        } else {
            throw new IllegalArgumentException("Unsupported conversion mode specified: " + mode);
        }
        return id;
    }

    /**
     * Converts the given identifier name to a legal Java identifier by replacing
     * illegal characters with underscores.
     * @param input The string to be converted.
     * @return      The given string converted to a legal Java identifier.
     */
    public static String normalize(String input) {
        if (input == null) return null;

        char[] characters = input.toCharArray();
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < characters.length; i++) {
            char character = characters[i];
            if ((i == 0 && !Character.isJavaIdentifierStart(character))||(i > 0 && !Character.isJavaIdentifierPart(character))) {
                character = '_';
            }
            output.append(character);
        }

        return output.toString();
    }
}
