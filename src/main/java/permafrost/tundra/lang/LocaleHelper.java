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

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;

/**
 * A collection of convenience methods for working with Locale objects.
 */
public class LocaleHelper {
    /**
     * Disallow instantiation of this class.
     */
    private LocaleHelper() {}

    /**
     * Returns a new java.util.Locale object for the given language, country and variant
     * @param language An ISO 639 alpha-2 or alpha-3 language code, or a language subtag
     *                 up to 8 characters in length. See the Locale class description
     *                 about valid language values.
     * @param country  An ISO 3166 alpha-2 country code or a UN M.49 numeric-3 area code.
     *                 See the Locale class description about valid country values.
     * @param variant  Any arbitrary value used to indicate a variation of a Locale.
     *                 See the Locale class description for the details.
     * @return A new java.util.Local object.
     */
    public static java.util.Locale locale(String language, String country, String variant) {
        java.util.Locale locale = java.util.Locale.getDefault();

        if (language != null) {
            if (country == null) {
                locale = new java.util.Locale(language);
            } else if (variant == null) {
                locale = new java.util.Locale(language, country);
            } else {
                locale = new java.util.Locale(language, country, variant);
            }
        }

        return locale;
    }

    /**
     * Converts an IData locale object to a java.util.Locale object.
     * @param document The IData locale object to be converted.
     * @return         A java.util.Locale object representing the given locale.
     */
    public static java.util.Locale locale(IData document) {
        String language = null, country = null, variant = null;

        if (document != null) {
            IDataCursor cursor = document.getCursor();
            language = IDataUtil.getString(cursor, "language");
            country = IDataUtil.getString(cursor, "country");
            variant = IDataUtil.getString(cursor, "variant");
            cursor.destroy();
        }

        return locale(language, country, variant);
    }
}
