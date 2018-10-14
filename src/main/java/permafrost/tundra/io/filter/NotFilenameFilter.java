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

import java.io.File;
import java.io.FilenameFilter;

/**
 * A FilenameFilter that negates another FilenameFilter.
 */
public class NotFilenameFilter implements FilenameFilter {
    /**
     * The FilenameFilter that is negated by this object.
     */
    protected FilenameFilter filter;

    /**
     * Constructs a new NotFilenameFilter to negate the given FilenameFilter.
     *
     * @param filter    The FilenameFilter object to be negated.
     */
    public NotFilenameFilter(FilenameFilter filter) {
        if (filter == null) throw new NullPointerException("filter must not be null");
        this.filter = filter;
    }

    /**
     * Returns true if the given parent and child passes all chained filters.
     *
     * @param parent    The parent directory being filtered.
     * @param child     The child filename being filtered.
     * @return          True if the given parent and child passes all chained filters.
     */
    public boolean accept(File parent, String child) {
        return !filter.accept(parent, child);
    }
}
