/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Lachlan Dowding
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataPortable;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Wraps a ConcurrentMap object with an IData implementation.
 *
 * @param <K>   The class of keys held by this object.
 * @param <V>   The class of values held by this object.
 */
public class ConcurrentMapIData<K, V> extends AbstractConcurrentMap<K, V> implements Serializable {
    /**
     * The serialization identity of this class version.
     */
    private static final long serialVersionUID = 1;

    /**
     * Constructs a new unsorted ConcurrentMapIData.
     */
    public ConcurrentMapIData() {
        this(false);
    }

    /**
     * Constructs a new unsorted ConcurrentMapIData.
     *
     * @param sorted    Whether to sort by keys.
     */
    public ConcurrentMapIData(boolean sorted) {
        this(sorted ? new ConcurrentSkipListMap<K, V>() : new ConcurrentHashMap<K, V>());
    }

    /**
     * Constructs a new unsorted ConcurrentMapIData.
     *
     * @param comparator    The comparator to use when sorting.
     */
    public ConcurrentMapIData(Comparator<? super K> comparator) {
        this(new ConcurrentSkipListMap<K, V>(comparator));
    }

    /**
     * Constructs a new ConcurrentMapIData given a ConcurrentMap to be adapted.
     *
     * @param map The map to be adapted.
     */
    public ConcurrentMapIData(ConcurrentMap<K, V> map) {
        super(map);
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return new MapCursor<K, V>(this);
    }

    /**
     * Returns a newly created ConcurrentMapIData.
     *
     * @return A newly created ConcurrentMapIData.
     */
    public static ConcurrentMapIData<String, Object> create() {
        return new ConcurrentMapIData<String, Object>();
    }

    /**
     * Creates a ConcurrentMapIData which is a recursive clone of the given IData.
     *
     * @param document  The IData to clone.
     * @return          A ConcurrentMapIData clone of the given document.
     */
    public static ConcurrentMapIData<String, Object> of(IData document) {
        return of(document, false);
    }

    /**
     * Creates a ConcurrentMapIData which is a recursive clone of the given IData.
     *
     * @param document  The IData to clone.
     * @param sorted    Whether to sort by keys.
     * @return          A ConcurrentMapIData clone of the given document.
     */
    public static ConcurrentMapIData<String, Object> of(IData document, boolean sorted) {
        if (document == null) return null;

        ConcurrentMapIData<String, Object> map = new ConcurrentMapIData<String, Object>(sorted);

        IDataCursor cursor = document.getCursor();
        try {
            while(cursor.next()) {
                String key = cursor.getKey();
                Object value = cursor.getValue();

                if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                    value = of(IDataHelper.toIDataArray(value), sorted);
                } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                    value = of(IDataHelper.toIData(value), sorted);
                }

                map.put(key, value);
            }
        } finally {
            cursor.destroy();
        }

        return map;
    }

    /**
     * Creates a ConcurrentMapIData[] which is a recursive clone of the given IData[].
     *
     * @param array     The IData[] to clone.
     * @return          A ConcurrentMapIData[] clone of the given array.
     */
    @SuppressWarnings("unchecked")
    public static ConcurrentMapIData<String, Object>[] of(IData[] array) {
        return of(array, false);
    }

    /**
     * Creates a ConcurrentMapIData[] which is a recursive clone of the given IData[].
     *
     * @param array     The IData[] to clone.
     * @param sorted    Whether to sort by keys.
     * @return          A ConcurrentMapIData[] clone of the given array.
     */
    @SuppressWarnings("unchecked")
    public static ConcurrentMapIData<String, Object>[] of(IData[] array, boolean sorted) {
        if (array == null) return null;

        ConcurrentMapIData[] output = new ConcurrentMapIData[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = of(array[i], sorted);
        }

        return output;
    }
}
