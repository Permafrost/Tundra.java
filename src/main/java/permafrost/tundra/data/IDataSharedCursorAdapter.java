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

import com.wm.data.DataException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataSharedCursor;
import com.wm.txn.ITransaction;
import com.wm.txn.TransactionException;

/**
 * This class wraps an IDataCursor in an IDataSharedCursor implementation.
 */
class IDataSharedCursorAdapter implements IDataSharedCursor {
    /**
     * The wrapped cursor.
     */
    protected IDataCursor cursor;

    /**
     * Construct a new wrapped cursor.
     *
     * @param cursor The cursor to be wrapped.
     */
    public IDataSharedCursorAdapter(IDataCursor cursor) {
        if (cursor == null) throw new NullPointerException("cursor must not be null");
        this.cursor = cursor;
    }

    /**
     * Resets this cursor's position.
     *
     * @throws DataException    Never thrown.
     */
    public void home() throws DataException {
        cursor.home();
    }

    /**
     * Returns the key at the cursor's current position.
     *
     * @return                  The key at the cursor's current position.
     * @throws DataException    Never thrown.
     */
    public String getKey() throws DataException {
        return cursor.getKey();
    }

    /**
     * Sets the key at the cursor's current position.
     *
     * @param key               The key to be set at the cursor's current position.
     * @throws DataException    Never thrown.
     */
    public void setKey(String key) throws DataException {
        cursor.setKey(key);
    }

    /**
     * Returns the value at the cursor's current position.
     *
     * @return                  The value at the cursor's current position.
     * @throws DataException    Never thrown.
     */
    public Object getValue() throws DataException {
        return cursor.getValue();
    }

    /**
     * Sets the value at the cursor's current position.
     *
     * @param value             The value to be set at the cursor's current position.
     * @throws DataException    Never thrown.
     */
    public void setValue(Object value) throws DataException {
        cursor.setValue(value);
    }

    /**
     * Returns the value at the cursor's current position.
     *
     * @return                  The value at the cursor's current position.
     * @throws DataException    Never thrown.
     */
    public Object getValueReference() throws DataException {
        return cursor.getValue();
    }

    /**
     * Deletes the element at the cursor's current position.
     *
     * @return                  True if the element was deleted.
     * @throws DataException    Never thrown.
     */
    public boolean delete() throws DataException {
        return cursor.delete();
    }

    /**
     * Inserts the key value pair before the cursor's current position.
     *
     * @param key               The key to be inserted.
     * @param value             The value to be inserted.
     * @throws DataException    Never thrown.
     */
    public void insertBefore(String key, Object value) throws DataException {
        cursor.insertBefore(key, value);
    }

    /**
     * Inserts the key value pair after the cursor's current position.
     *
     * @param key               The key to be inserted.
     * @param value             The value to be inserted.
     * @throws DataException    Never thrown.
     */
    public void insertAfter(String key, Object value) throws DataException {
        cursor.insertAfter(key, value);
    }

    /**
     * Inserts the key with a new IData document before the cursor's current position.
     *
     * @param key               The key to be inserted.
     * @return                  The new IData document inserted.
     * @throws DataException    Never thrown.
     */
    public IData insertDataBefore(String key) throws DataException {
        return cursor.insertDataBefore(key);
    }

    /**
     * Inserts the key with a new IData document after the cursor's current position.
     *
     * @param key               The key to be inserted.
     * @return                  The new IData document inserted.
     * @throws DataException    Never thrown.
     */
    public IData insertDataAfter(String key) throws DataException {
        return cursor.insertDataAfter(key);
    }

    /**
     * Moves this cursor's position to the next element.
     *
     * @return                  True if the cursor was repositioned.
     * @throws DataException    Never thrown.
     */
    public boolean next() throws DataException {
        return cursor.next();
    }

    /**
     * Moves this cursor's position to the next occurrence of the given key.
     *
     * @param key               The key to be positioned on.
     * @return                  True if the key exists and the cursor was repositioned.
     * @throws DataException    Never thrown.
     */
    public boolean next(String key) throws DataException {
        return cursor.next(key);
    }

    /**
     * Moves this cursor's position to the previous element.
     *
     * @return                  True if the cursor was repositioned.
     * @throws DataException    Never thrown.
     */
    public boolean previous() throws DataException {
        return cursor.previous();
    }

    /**
     * Moves this cursor's position to the previous occurrence of the given key.
     *
     * @param key               The key to be positioned on.
     * @return                  True if the key exists and the cursor was repositioned.
     * @throws DataException    Never thrown.
     */
    public boolean previous(String key) throws DataException {
        return cursor.previous(key);
    }

    /**
     * Moves this cursor's position to the first element.
     *
     * @return                  True if the cursor was repositioned.
     * @throws DataException    Never thrown.
     */
    public boolean first() throws DataException {
        return cursor.first();
    }

    /**
     * Moves this cursor's position to the first occurrence of the given key.
     *
     * @param key               The key to be positioned on.
     * @return                  True if the key exists and the cursor was repositioned.
     * @throws DataException    Never thrown.
     */
    public boolean first(String key) throws DataException {
        return cursor.first(key);
    }

    /**
     * Moves this cursor's position to the last element.
     *
     * @return                  True if the cursor was repositioned.
     * @throws DataException    Never thrown.
     */
    public boolean last() throws DataException {
        return cursor.last();
    }

    /**
     * Moves this cursor's position to the last occurrence of the given key.
     *
     * @param key               The key to be positioned on.
     * @return                  True if the key exists and the cursor was repositioned.
     * @throws DataException    Never thrown.
     */
    public boolean last(String key) throws DataException {
        return cursor.last(key);
    }

    /**
     * Returns true if this cursor has more data to be iterated over.
     *
     * @return               True if this cursor has more data.
     * @throws DataException Never thrown.
     */
    public boolean hasMoreData() throws DataException {
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
     * @return                  A clone of this cursor.
     * @throws DataException    Never thrown.
     */
    public IDataSharedCursorAdapter getCursorClone() throws DataException {
        return new IDataSharedCursorAdapter(cursor.getCursorClone());
    }

    /**
     * Returns false.
     *
     * @return False.
     */
    public boolean isTXNSupported() {
        return false;
    }

    /**
     * Not implemented, throws UnsupportedOperationException.
     *
     * @return                               Not applicable.
     * @throws TransactionException          Never thrown.
     * @throws UnsupportedOperationException Always thrown.
     */
    public ITransaction startTXN() throws TransactionException {
        throw new UnsupportedOperationException("startTXN not implemented");
    }

    /**
     * Does nothing.
     *
     * @throws TransactionException Never thrown.
     */
    public void txnJoin(ITransaction transaction) throws TransactionException {}

    /**
     * Does nothing.
     *
     * @throws TransactionException Never thrown.
     */
    public void txnAborted() throws TransactionException {}

    /**
     * Does nothing.
     *
     * @throws TransactionException Never thrown.
     */
    public void txnCommitted() throws TransactionException {}
}
