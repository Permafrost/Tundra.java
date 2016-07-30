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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.wm.data.DataException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;

/**
 * Adapts an iterator of map entries to an IDataCursor interface.
 */
public class MapIDataCursor<K, V> implements IDataCursor {
    /**
     * Position value used when cursor is unpositioned.
     */
    private static final int UNPOSITIONED = -1;
    /**
     * The wrapped map.
     */
    protected Map<K, V> map;
    /**
     * The key list used for cursor traversal.
     */
    protected List<K> keys;
    /**
     * The current position of the traversal.
     */
    protected int position;
    /**
     * The current key.
     */
    protected K key;
    /**
     * Whether the current position needs to be recalculated.
     */
    protected boolean dirty = false;

    /**
     * Constructs a new MapIDataCursor.
     *
     * @param map   The map the cursor will traverse.
     */
    public MapIDataCursor(Map<K, V> map) {
        this(map, UNPOSITIONED);
    }

    /**
     * Constructs a new MapIDataCursor.
     *
     * @param map       The map the cursor will traverse.
     * @param position  The initial position of the cursor.
     */
    protected MapIDataCursor(Map<K, V> map, int position) {
        if (map == null) throw new NullPointerException("map must not be null");
        this.map = map;
        initialize(position);
    }

    /**
     * Initializes the state of the cursor.
     */
    protected void initialize() {
        initialize(UNPOSITIONED);
    }

    /**
     * Initializes the state of the cursor.
     *
     * @param position  The initial position of the cursor.
     */
    protected void initialize(int position) {
        keys = new ArrayList<K>(map.keySet());
        this.position = Math.max(UNPOSITIONED, position);
    }

    /**
     * Returns the current position of the cursor.
     *
     * @return The current position of the cursor.
     */
    protected int getPosition() {
        if (dirty) {
            dirty = false;
            position = key == null ? UNPOSITIONED : keys.indexOf(key);
        }
        return position;
    }

    /**
     * Sets the current position of the cursor.
     *
     * @param position The new position of the cursor.
     * @return         The new position of the cursor.
     */
    protected int setPosition(int position) {
        this.position = Math.max(position, UNPOSITIONED);
        dirty = false;
        return this.position;
    }

    /**
     * Deletes the element at the cursor's current position.
     *
     * @return True if the element was deleted.
     */
    public boolean delete() {
        int position = getPosition();
        if (position <= UNPOSITIONED) {
            return false;
        } else {
            map.remove(keys.remove(position));
            if (position > keys.size()) {
                setPosition(UNPOSITIONED);
                return false;
            }
            return true;
        }
    }

    /**
     * Destroys the cursor.
     */
    public void destroy() {
        setPosition(UNPOSITIONED);
    }

    /**
     * Positions the cursor at the first element.
     *
     * @return  True if the cursor was repositioned.
     */
    public boolean first() {
        if (keys.size() > 0) {
            setPosition(0);
            return true;
        } else {
            setPosition(UNPOSITIONED);
            return false;
        }
    }

    /**
     * Positions the cursor at the first element with the given key.
     *
     * @param key   The key to position the cursor at.
     * @return      True if the cursor was repositioned.
     */
    @SuppressWarnings("unchecked")
    public final boolean first(String key) {
        if (map.containsKey((K)key)) {
            this.key = (K)key;
            dirty = true;
            return true;
        }
        return false;
    }

    /**
     * Returns a clone of this cursor.
     *
     * @return A clone of this cursor.
     */
    public IDataCursor getCursorClone() {
        return new MapIDataCursor<K, V>(map, position);
    }

    /**
     * Returns the key of the element at the cursor's current position.
     *
     * @return The key of the element at the cursor's current position.
     */
    public String getKey() {
        if (key != null) {
            return key.toString();
        } else {
            int position = getPosition();
            if (position <= UNPOSITIONED) {
                return null;
            } else {
                K key = keys.get(position);
                return key == null ? null : key.toString();
            }
        }
    }

    /**
     * Not implemented; returns null.
     *
     * @return  Null.
     */
    public DataException getLastError() {
        return null;
    }

    /**
     * Returns the value of the element at the cursor's current position.
     *
     * @return The value of the element at the cursor's current position.
     */
    public Object getValue() {
        if (key != null) {
            return map.get(key);
        } else {
            int position = getPosition();
            if (position <= UNPOSITIONED) {
                return null;
            } else {
                return map.get(keys.get(position));
            }
        }
    }

    /**
     * Returns true if the cursor has more elements yet to be traversed.
     *
     * @return True if the cursor has more elements yet to be traversed.
     */
    public boolean hasMoreData() {
        int size = keys.size();
        return size > 0 && (getPosition() + 1) < size;
    }

    /**
     * Not implemented; returns false.
     *
     * @return  False.
     */
    public boolean hasMoreErrors() {
        return false;
    }

    /**
     * Initializes the cursor.
     */
    public void home() {
        initialize();
    }

    /**
     * Inserts the given key value pair after the element at the cursor's current position.
     *
     * @param key   The key to insert.
     * @param value The value to insert.
     */
    @SuppressWarnings("unchecked")
    public void insertAfter(String key, Object value) {
        int position = setPosition(Math.min(getPosition() + 1, keys.size()));
        keys.add(position, (K)key);
        map.put((K)key, (V)value);
    }

    /**
     * Inserts the given key value pair before the element at the cursor's current position.
     *
     * @param key   The key to insert.
     * @param value The value to insert.
     */
    @SuppressWarnings("unchecked")
    public void insertBefore(String key, Object value) {
        int position = setPosition(Math.max(getPosition(), 0));
        keys.add(position, (K)key);
        map.put((K)key, (V)value);
    }

    /**
     * Inserts a new IData document after the element at the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @return      The newly created IData document that was inserted.
     */
    public IData insertDataAfter(String key) {
        IData document = IDataFactory.create();
        insertAfter(key, document);
        return document;
    }

    /**
     * Inserts a new IData document before the element at the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @return      The newly created IData document that was inserted.
     */
    public IData insertDataBefore(String key) {
        IData document = IDataFactory.create();
        insertBefore(key, document);
        return document;
    }

    /**
     * Positions the cursor on the last element in the map.
     *
     * @return True if the cursor was repositioned.
     */
    public boolean last() {
        int size = keys.size();
        if (size > 0) {
            setPosition(size - 1);
            return true;
        } else {
            setPosition(UNPOSITIONED);
            return false;
        }
    }

    /**
     * Positions the cursor on the last occurrence of the given key.
     *
     * @param key   The key to position the cursor at.
     * @return      True if the cursor was repositioned.
     */
    public boolean last(String key) {
        // because keys can only occur once in the map
        return first(key);
    }

    /**
     * Positions the cursor on the next element following the current position.
     *
     * @return  True if the cursor was repositioned.
     */
    public boolean next() {
        boolean hasMoreData = hasMoreData();
        if (hasMoreData) {
            setPosition(getPosition() + 1);
        } else {
            setPosition(UNPOSITIONED);
        }
        return hasMoreData;
    }

    /**
     * Positions the cursor on the next element with the given key.
     *
     * @param key   The key to position the cursor at.
     * @return      True if the cursor was repositioned.
     */
    public boolean next(String key) {
        // because keys can only occur once in the map
        return first(key);
    }

    /**
     * Positions the cursor on the previous element to the current position.
     *
     * @return  True if the cursor was repositioned.
     */
    public boolean previous() {
        int size = keys.size();
        if (size > 0) {
            int position = getPosition();
            if (position <= UNPOSITIONED) {
                setPosition(size - 1);
                return true;
            } else if (position == 0){
                return false;
            } else {
                setPosition(position - 1);
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Positions the cursor on the previous occurrence of the given key.
     *
     * @param key   The key to position the cursor at.
     * @return      True if the cursor was repositioned.
     */
    public boolean previous(String key) {
        // because keys can only occur once in the map
        return first(key);
    }

    /**
     * Not implemented; does nothing.
     *
     * @param mode  Not used.
     */
    public void setErrorMode(int mode) {
        // do nothing
    }

    /**
     * Sets the key of the element at the cursor's current position.
     *
     * @param key   The key to be set.
     */
    @SuppressWarnings("unchecked")
    public void setKey(String key) {
        int position = getPosition();
        if (position > UNPOSITIONED) {
            K oldKey = keys.set(position, (K)key);
            map.put((K)key, map.remove(oldKey));
        }
    }

    /**
     * Sets the value of the element at the cursor's current position.
     *
     * @param value The value to be set.
     */
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        int position = getPosition();
        if (position > UNPOSITIONED) map.put(keys.get(position), (V)value);
    }
}