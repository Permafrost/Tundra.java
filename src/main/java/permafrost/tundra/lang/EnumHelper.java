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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A collection of convenience methods for working with Enum objects.
 */
public final class EnumHelper {
    /**
     * Disallow instantiation of this class.
     */
    private EnumHelper() {}

    /**
     * Converts the given object to an Enum value of the given class.
     *
     * @param object    The object to be converted.
     * @param klass     The Enum class to convert to.
     * @param <E>       The Enum class to convert to.
     * @return          The Enum value representing the given string.
     */
    @SuppressWarnings("unchecked")
    public static <E> E normalize(Object object, Class<E> klass) {
        E value = null;

        if (klass != null && klass.isEnum() && object instanceof String) {
            try {
                Method method = klass.getDeclaredMethod("valueOf", String.class);
                value = (E)method.invoke(null, normalize((String)object));
            } catch(NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            } catch(IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch(InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

        return value;
    }

    /**
     * Normalizes the given String value such that it can be used to return an enumeration value using an enum's valueOf
     * method. Trims the value of leading and trailing whitespace, and replaces any other whitespace or hyphen character
     * with underscores, then converts to upper case.
     *
     * @param value The value to be normalized.
     * @return      The normalized value.
     */
    public static String normalize(String value) {
        return value.trim().replace(' ', '_').replace('-', '_').toUpperCase();
    }
}
