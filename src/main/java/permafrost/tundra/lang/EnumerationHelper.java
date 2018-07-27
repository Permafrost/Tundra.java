/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Lachlan Dowding
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
import java.util.Enumeration;
import java.util.List;

/**
 * A collection of convenience methods for working with Enumeration objects.
 */
public final class EnumerationHelper {
    /**
     * Disallow instantiation of this class.
     */
    private EnumerationHelper() {}

    /**
     * Converts an Enumeration object to a String[].
     *
     * @param enumeration   The object to convert.
     * @return              The String[] representation of the object.
     */
    public static String[] stringify(Enumeration enumeration) {
        if (enumeration == null) return new String[0];

        List<String> output = new ArrayList<String>();
        while(enumeration.hasMoreElements()) {
            Object item = enumeration.nextElement();
            output.add(item == null ? null : ObjectHelper.stringify(item));
        }

        return output.toArray(new String[output.size()]);
    }
}
