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

package permafrost.tundra.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for working regular expressions.
 */
public class RegularExpressionHelper {
    /**
     * Disallow instantiation of this class.
     */
    private RegularExpressionHelper() {}

    /**
     * Compiles a given regular expression pattern.
     *
     * @param pattern The pattern to compile.
     * @return        The compiled pattern.
     */
    public static Pattern compile(String pattern) {
        if (pattern == null) return null;
        return Pattern.compile(pattern);
    }

    /**
     * Compiles a list of regular expression patterns.
     *
     * @param patterns  The patterns to compile.
     * @return          The compiled patterns.
     */
    public static Collection<Pattern> compile(Collection<String> patterns) {
        if (patterns == null) return null;

        List<Pattern> compiledPatterns = new ArrayList<Pattern>(patterns.size());

        for (String pattern : patterns) {
            if (pattern != null) compiledPatterns.add(compile(pattern));
        }

        return compiledPatterns;
    }

    /**
     * Compiles a list of regular expression patterns.
     *
     * @param patterns  The patterns to compile.
     * @return          The compiled patterns.
     */
    public static Pattern[] compile(String ...patterns) {
        if (patterns == null) return null;

        Collection<Pattern> compiledPatterns = compile(Arrays.asList(patterns));

        return compiledPatterns.toArray(new Pattern[compiledPatterns.size()]);
    }
}
