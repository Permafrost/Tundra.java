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

package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataHashCursor;
import com.wm.data.IDataIndexCursor;
import com.wm.data.IDataSharedCursor;
import com.wm.data.IDataTreeCursor;

/**
 * Provides a skeleton implementation of the IData interface.
 */
public abstract class AbstractIData implements IData {
    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    public abstract IDataCursor getCursor();

    /**
     * Returns an IDataSharedCursor for this IData object. An IDataSharedCursor contains the basic methods you use to
     * traverse an IData object and get or set elements within it.
     *
     * @return An IDataSharedCursor for this object.
     */
    @Override
    public IDataSharedCursor getSharedCursor() {
        return new IDataSharedCursorAdapter(getCursor());
    }

    /**
     * Returns an IDataIndexCursor for traversing this IData.
     *
     * @return An IDataIndexCursor for traversing this IData.
     * @throws UnsupportedOperationException As this method is not implemented.
     * @deprecated
     */
    @Override
    public IDataIndexCursor getIndexCursor() {
        throw new UnsupportedOperationException("getIndexCursor not implemented");
    }

    /**
     * Returns an IDataTreeCursor for traversing this IData.
     *
     * @return An IDataTreeCursor for traversing this IData.
     * @throws UnsupportedOperationException As this method is not implemented.
     * @deprecated
     */
    @Override
    public IDataTreeCursor getTreeCursor() {
        throw new UnsupportedOperationException("getTreeCursor not implemented");
    }

    /**
     * Returns an IDataHashCursor for traversing this IData.
     *
     * @return An IDataHashCursor for traversing this IData.
     * @throws UnsupportedOperationException As this method is not implemented.
     * @deprecated
     */
    @Override
    public IDataHashCursor getHashCursor() {
        throw new UnsupportedOperationException("getHashCursor not implemented");
    }
}
