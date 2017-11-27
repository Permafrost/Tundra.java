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

package permafrost.tundra.io.filter;

import permafrost.tundra.io.FileHelper;
import permafrost.tundra.util.regex.PatternHelper;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * A FilenameFilter that only accepts objects whose names match the given regular expression.
 */
public class RegularExpressionFilenameFilter implements FilenameFilter {
    protected Pattern pattern;

    /**
     * Constructs a new RegularExpressionFilenameFilter using the given pattern.
     *
     * @param pattern A regular expression pattern to be used to filter files.
     */
    public RegularExpressionFilenameFilter(String pattern) {
        this(PatternHelper.compile(pattern));
    }

    /**
     * Constructs a new RegularExpressionFilenameFilter using the given pattern.
     *
     * @param pattern A regular expression pattern to be used to filter files.
     */
    public RegularExpressionFilenameFilter(Pattern pattern) {
        if (pattern == null) throw new NullPointerException("pattern must not be null");

        if (FileHelper.isCaseInsensitive()) {
            // the file system is case insensitive so convert the pattern to be case insensitive
            this.pattern = Pattern.compile(pattern.pattern(), pattern.flags() | Pattern.CASE_INSENSITIVE);
        } else {
            this.pattern = pattern;
        }
    }

    /**
     * Returns true if the given child matches the specified regular expression.
     *
     * @param parent The parent directory being filtered.
     * @param child  The child filename being filtered.
     * @return True if the given child matches the specified regular expression.
     */
    public boolean accept(File parent, String child) {
        return pattern.matcher(child).matches();
    }
}
