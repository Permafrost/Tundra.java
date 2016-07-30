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
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Wraps a Map object with an IData implementation.
 *
 * @param <K>   The class of keys held by this object.
 * @param <V>   The class of values held by this object.
 */
public class MapIData<K, V> extends MapEnvelope<K, V> implements Serializable {
    /**
     * The serialization identity of this class version.
     */
    private static final long serialVersionUID = 1;

    /**
     * Constructs a new MapIDataAdapter.
     */
    public MapIData() {
        super(new TreeMap<K, V>());
    }

    /**
     * Constructs a new MapIDataAdapter given a Map to be adapted.
     *
     * @param map The map to be adapted.
     */
    public MapIData(Map<K, V> map) {
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
        return new MapIDataCursor<K, V>(this);
    }

    /**
     * Returns a newly created MapIDataAdapter.
     *
     * @return A newly created MapIDataAdapter.
     */
    public static IData create() {
        return new MapIData<String, Object>();
    }
}