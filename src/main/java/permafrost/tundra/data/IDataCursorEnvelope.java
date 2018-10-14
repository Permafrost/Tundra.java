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

import com.wm.data.DataException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;

/**
 * Wraps an IDataCursor in an envelope object and provides a skeletal implementation for subclasses.
 */
public abstract class IDataCursorEnvelope implements IDataCursor {
    /**
     * The wrapped cursor.
     */
    protected IDataCursor cursor;

    /**
     * Constructs a new wrapped cursor.
     *
     * @param cursor The cursor to be wrapped.
     */
    public IDataCursorEnvelope(IDataCursor cursor) {
        if (cursor == null) throw new NullPointerException("cursor must not be null");
        this.cursor = cursor;
    }

    /**
     * Sets the cursor error mode.
     *
     * @param mode The mode to be set.
     */
    public void setErrorMode(int mode) {
        cursor.setErrorMode(mode);
    }

    /**
     * Returns the last error encountered.
     *
     * @return The last error encountered.
     */
    public DataException getLastError() {
        return cursor.getLastError();
    }

    /**
     * Returns whether the cursor has more errors.
     *
     * @return True if the cursor has more errors.
     */
    public boolean hasMoreErrors() {
        return cursor.hasMoreErrors();
    }

    /**
     * Resets the cursor to be unpositioned.
     */
    public void home() {
        cursor.home();
    }

    /**
     * Returns the key at the cursor's current position.
     *
     * @return The key at the cursor's current position.
     */
    public String getKey() {
        return cursor.getKey();
    }

    /**
     * Sets the key at the cursor's current position.
     *
     * @param key The key to be set.
     */
    public void setKey(String key) {
        cursor.setKey(key);
    }

    /**
     * Returns the value at the cursor's current position.
     *
     * @return The value at the cursor's current position.
     */
    public Object getValue() {
        return cursor.getValue();
    }

    /**
     * Sets the value at the cursor's current position.
     *
     * @param value The value to be set.
     */
    public void setValue(Object value) {
        cursor.setValue(value);
    }

    /**
     * Deletes the element at the cursor's current position.
     *
     * @return True if the element was deleted.
     */
    public boolean delete() {
        return cursor.delete();
    }

    /**
     * Inserts the given element before the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @param value The value to be inserted.
     */
    public void insertBefore(String key, Object value) {
        cursor.insertBefore(key, value);
    }

    /**
     * Inserts the given element after the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @param value The value to be inserted.
     */
    public void insertAfter(String key, Object value) {
        cursor.insertAfter(key, value);
    }

    /**
     * Inserts a new IData document with the given key before the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @return      The newly inserted IData document.
     */
    public IData insertDataBefore(String key) {
        return cursor.insertDataBefore(key);
    }

    /**
     * Inserts a new IData document with the given key after the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @return      The newly inserted IData document.
     */
    public IData insertDataAfter(String key) {
        return cursor.insertDataAfter(key);
    }

    /**
     * Moves the cursor's position to the next element.
     *
     * @return True if the cursor was repositioned.
     */
    public boolean next() {
        return cursor.next();
    }

    /**
     * Moves the cursor's position to the next element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     */
    public boolean next(String key) {
        return cursor.next(key);
    }

    /**
     * Moves the cursor's position to the previous element.
     *
     * @return True if the cursor was repositioned.
     */
    public boolean previous() {
        return cursor.previous();
    }

    /**
     * Moves the cursor's position to the previous element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     */
    public boolean previous(String key) {
        return cursor.previous(key);
    }

    /**
     * Moves the cursor's position to the first element.
     *
     * @return True if the cursor was repositioned.
     */
    public boolean first() {
        return cursor.first();
    }

    /**
     * Moves the cursor's position to the first element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     */
    public boolean first(String key) {
        return cursor.first(key);
    }

    /**
     * Moves the cursor's position to the last element.
     *
     * @return True if the cursor was repositioned.
     */
    public boolean last() {
        return cursor.last();
    }

    /**
     * Moves the cursor's position to the last element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     */
    public boolean last(String key) {
        return cursor.last(key);
    }

    /**
     * Returns true if the cursor has more elements to be iterated over.
     *
     * @return True if the cursor has more elements to be iterated over.
     */
    public boolean hasMoreData() {
        return cursor.hasMoreData();
    }

    /**
     * Destroys this cursor.
     */
    public void destroy() {
        cursor.destroy();
    }

    /**
     * Returns a clone of this cursor.
     *
     * @return A clone of this cursor.
     */
    public abstract IDataCursor getCursorClone();
}
