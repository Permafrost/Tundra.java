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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * An abstract FilenameFilter that chains together other filters.
 */
public abstract class ConditionalFilenameFilter implements FilenameFilter {
    /**
     * The list of chained FilenameFilter objects.
     */
    protected List<FilenameFilter> filters = new ArrayList<FilenameFilter>();

    /**
     * Adds a FilenameFilter object to the list of chained filters.
     *
     * @param filter    The FilenameFilter object to be added to the chain.
     */
    public void add(FilenameFilter filter) {
        if (filter != null) this.filters.add(filter);
    }

    /**
     * Adds a collection of FilenameFilter objects to the list of chained filters.
     *
     * @param filters   A collection of FilenameFilter object to be added to the chain.
     */
    public void addAll(Collection<? extends FilenameFilter> filters) {
        if (filters != null) this.filters.addAll(filters);
    }

    /**
     * Returns true if the given parent and child passes all filters.
     *
     * @param parent    The parent directory being filtered.
     * @param child     The child filename being filtered.
     * @return          True if the given parent and child passes all filters.
     */
    public abstract boolean accept(File parent, String child);
}
