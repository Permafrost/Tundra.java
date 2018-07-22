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

package permafrost.tundra.measure;

import permafrost.tundra.lang.LocaleHelper;
import java.text.ParseException;
import java.util.Locale;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

/**
 * A collection of convenience methods for working with units of measure.
 */
public final class UnitHelper {
    /**
     * Disallow instantiation of this class.
     */
    private UnitHelper() {}

    /**
     * Returns a Unit object given a unit name.
     *
     * @param unit A unit name, such as km/h.
     * @return     The Unit object representing the unit of measure with the given name.
     */
    public static Unit parse(String unit) {
        return parse(unit, null);
    }

    /**
     * Returns a Unit object given a unit name and locale.
     *
     * @param unit   A unit name, such as km/h.
     * @param locale The locale in which the unit name should be interpreted.
     * @return       The Unit object representing the unit of measure with the given name.
     */
    public static Unit parse(String unit, Locale locale) {
        Unit output = null;

        try {
            output = (Unit)UnitFormat.getInstance(LocaleHelper.normalize(locale)).parseObject(unit);
        } catch(ParseException ex) {
            throw new IllegalArgumentException(ex);
        }

        return output;
    }

    /**
     * Returns a string representation of the given Unit.
     *
     * @param unit The Unit to be converted to a string.
     * @return     A string representation of the given Unit.
     */
    public static String emit(Unit unit) {
        return emit(unit, null);
    }

    /**
     * Returns a string representation of the given Unit for the given locale.
     *
     * @param unit   The Unit to be converted to a string.
     * @param locale The locale to use when converting the Unit to a string.
     * @return       A string representation of the given Unit for the given locale.
     */
    public static String emit(Unit unit, Locale locale) {
        return UnitFormat.getInstance(LocaleHelper.normalize(locale)).format(unit);
    }

    /**
     * Converts the given unit name from one locale to another.
     *
     * @param unit          The unit name to be converted.
     * @param inputLocale   The locale in which to interpret the given name.
     * @param outputLocale  The locale to which the unit is converted.
     * @return              The converted unit name.
     */
    public static String localize(String unit, Locale inputLocale, Locale outputLocale) {
        return emit(parse(unit, inputLocale), outputLocale);
    }
}
