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

import permafrost.tundra.collection.ListHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

/**
 * The different types of variable substitution supported by the SubstitutionHelper methods.
 */
public enum SubstitutionType {
    LOCAL, GLOBAL;

    /**
     * The default substitution type, if none is specified.
     */
    public static final SubstitutionType DEFAULT_SUBSTITUTION_TYPE = LOCAL;

    /**
     * The default substitution set, if none is specified.
     */
    public static final EnumSet<SubstitutionType> DEFAULT_SUBSTITUTION_SET = EnumSet.of(DEFAULT_SUBSTITUTION_TYPE);

    /**
     * Returns an EnumSet of SubstitutionType for the given string values.
     *
     * @param values The values to be converted to an EnumSet of SubstitutionType.
     * @return       The EnumSet of SubstitutionType representing the given values.
     */
    public static EnumSet<SubstitutionType> normalize(String ...values) {
        if (values == null || values.length == 0) return DEFAULT_SUBSTITUTION_SET;

        List<SubstitutionType> types = new ArrayList<SubstitutionType>(values.length);
        for (String value : values) {
            if (value == null) {
                types.add(DEFAULT_SUBSTITUTION_TYPE);
            } else if (value.equalsIgnoreCase("ALL")) {
                types.add(LOCAL);
                types.add(GLOBAL);
            } else {
                types.add(valueOf(value.trim().toUpperCase()));
            }
        }

        return normalize(types);
    }

    /**
     * Normalizes a list of SubstitutionTypes.
     *
     * @param types The SubstitutionTypes to be normalized.
     * @return      If the given type is null the default type set, otherwise the given types in a set.
     */
    public static EnumSet<SubstitutionType> normalize(SubstitutionType ...types) {
        return normalize(ListHelper.of(types));
    }

    /**
     * Normalizes a list of SubstitutionTypes.
     *
     * @param types The SubstitutionTypes to be normalized.
     * @return      If the given type is null the default type set, otherwise the given types in a set.
     */
    public static EnumSet<SubstitutionType> normalize(Collection<SubstitutionType> types) {
        return normalize(types == null ? null : EnumSet.copyOf(types));
    }

    /**
     * Normalizes an EnumSet of SubstitutionType.
     *
     * @param types The EnumSet to be normalized.
     * @return      If the given EnumSet is null then an EnumSet containing the default type, otherwise the given
     *              EnumSet.
     */
    public static EnumSet<SubstitutionType> normalize(EnumSet<SubstitutionType> types) {
        return types == null || types.isEmpty() ? DEFAULT_SUBSTITUTION_SET : types;
    }
}
