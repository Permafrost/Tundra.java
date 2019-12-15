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

import java.util.Set;
import java.util.TreeSet;

/**
 * The different types of variable substitution supported by the SubstitutionHelper methods.
 */
public enum SubstitutionType {
    ALL, LOCAL, GLOBAL;

    /**
     * The default substitution type, if none is specified.
     */
    public static final SubstitutionType DEFAULT_SUBSTITUTION_TYPE = ALL;

    /**
     * Returns an normalized SubstitutionType for the given string values.
     *
     * @param values The values to be converted to an SubstitutionType.
     * @return       The SubstitutionType representing the given values.
     */
    public static SubstitutionType normalize(String ...values) {
        if (values == null || values.length == 0) return DEFAULT_SUBSTITUTION_TYPE;

        Set<SubstitutionType> types = new TreeSet<SubstitutionType>();
        for (String value : values) {
            if (value == null) {
                types.add(DEFAULT_SUBSTITUTION_TYPE);
            } else {
                types.add(valueOf(value.trim().toUpperCase()));
            }
        }

        if (types.contains(ALL) || (types.contains(LOCAL) && types.contains(GLOBAL))) {
            return ALL;
        } else if (types.contains(GLOBAL)) {
            return GLOBAL;
        } else {
            return LOCAL;
        }
    }

    /**
     * Normalizes the given SubstitutionType: if null returns the default SubstitutionType, else returns the given
     * SubstitutionType.
     *
     * @param substitutionType  The SubstitutionType to normalize.
     * @return                  The normalized SubstitutionType.
     */
    public static SubstitutionType normalize(SubstitutionType substitutionType) {
        if (substitutionType == null) {
            return DEFAULT_SUBSTITUTION_TYPE;
        } else {
            return substitutionType;
        }
    }
}
