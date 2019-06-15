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
import com.wm.data.IDataFactory;
import com.wm.util.Table;
import java.io.Serializable;

/**
 * Wraps an IData in an immutable envelope.
 */
public class ImmutableIData extends IDataEnvelope implements Serializable {
    /**
     * The serialization identity of this class version.
     */
    private static final long serialVersionUID = 1;

    /**
     * Wraps an IData document in an immutable envelope.
     *
     * @param document  The document to be wrapped.
     */
    public ImmutableIData(IData document) {
        super(document);
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return new ImmutableIDataCursor(super.getCursor());
    }

    /**
     * Returns a new ImmutableIData wrapping the given IData document.
     *
     * @param document  The document to be wrapped.
     * @return          A new ImmutableIData wrapping the given IData document.
     */
    public static IData of(IData document) {
        return new ImmutableIData(document);
    }

    /**
     * Returns a new ImmutableIData[] representation of the given IData[] document list.
     *
     * @param array     An IData[] document list.
     * @return          A new ImmutableIData[] representation of the given IData[] document list.
     */
    public static IData[] of(IData[] array) {
        if (array == null) return null;

        IData[] output = new ImmutableIData[array.length];

        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                output[i] = ImmutableIData.of(array[i]);
            }
        }

        return output;
    }

    /**
     * Static factory method used by IData XML deserialization; since there's no point deserializing an immutable IData
     * as it will be a new empty instance that cannot be written to, a mutable IData is returned instead.
     *
     * @return A new IData instance.
     */
    public static IData create() {
        return IDataFactory.create();
    }
}
