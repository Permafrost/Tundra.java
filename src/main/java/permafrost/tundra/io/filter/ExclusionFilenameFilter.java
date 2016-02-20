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

package permafrost.tundra.io.filter;

import java.util.Arrays;
import java.util.Collection;

/**
 * A FilenameFilter which accepts files that do not match the patterns in this filter.
 */
public class ExclusionFilenameFilter extends NotFilenameFilter {
    /**
     * Constructs a new InclusionFilenameFilter.
     *
     * @param type      Whether the patterns are regular expressions or wildcards.
     * @param patterns  The patterns to use to include filenames.
     */
    public ExclusionFilenameFilter(FilenameFilterType type, String ...patterns) {
        this(type, patterns == null ? null : Arrays.asList(patterns));
    }

    /**
     * Constructs a new InclusionFilenameFilter.
     *
     * @param type      Whether the patterns are regular expressions or wildcards.
     * @param patterns  The patterns to use to include filenames.
     */
    public ExclusionFilenameFilter(FilenameFilterType type, Collection<String> patterns) {
        super(new InclusionFilenameFilter(type, patterns));
    }
}
