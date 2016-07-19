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
import com.wm.data.IDataSharedCursor;
import com.wm.txn.ITransaction;
import com.wm.txn.TransactionException;

/**
 * Wraps an IDataSharedCursor in an envelope object and provides a skeletal implementation for subclasses.
 */
public class IDataSharedCursorEnvelope implements IDataSharedCursor {
    /**
     * The wrapped cursor.
     */
    IDataSharedCursor cursor;

    /**
     * Constructs a new wrapped cursor.
     *
     * @param cursor The cursor to be wrapped.
     */
    public IDataSharedCursorEnvelope(IDataSharedCursor cursor) {
        if (cursor == null) throw new NullPointerException("cursor must not be null");

        if (cursor instanceof IDataSharedCursorEnvelope) {
            try {
                this.cursor = ((IDataSharedCursorEnvelope)cursor).cursor.getCursorClone();
            } catch(DataException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            this.cursor = cursor;
        }
    }

    /**
     * Resets the cursor to be unpositioned.
     *
     * @throws DataException If an error occurs.
     */
    public void home() throws DataException {
        cursor.home();
    }

    /**
     * Returns the key at the cursor's current position.
     *
     * @return The key at the cursor's current position.
     * @throws DataException If an error occurs.
     */
    public String getKey() throws DataException {
        return cursor.getKey();
    }

    /**
     * Sets the key at the cursor's current position.
     *
     * @param key The key to be set.
     * @throws DataException If an error occurs.
     */
    public void setKey(String key) throws DataException {
       cursor.setKey(key);
    }

    /**
     * Returns the value at the cursor's current position.
     *
     * @return The value at the cursor's current position.
     * @throws DataException If an error occurs.
     */
    public Object getValue() throws DataException {
        return cursor.getValue();
    }

    /**
     * Sets the value at the cursor's current position.
     *
     * @param value The value to be set.
     * @throws DataException If an error occurs.
     */
    public void setValue(Object value) throws DataException {
        cursor.setValue(value);
    }

    /**
     * Returns the value at the cursor's current position.
     *
     * @return The value at the cursor's current position.
     * @throws DataException If an error occurs.
     */
    public Object getValueReference() throws DataException {
        return cursor.getValueReference();
    }

    /**
     * Deletes the element at the cursor's current position.
     *
     * @return True if the element was deleted.
     * @throws DataException If an error occurs.
     */
    public boolean delete() throws DataException {
        return cursor.delete();
    }

    /**
     * Inserts the given element before the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @param value The value to be inserted.
     * @throws DataException If an error occurs.
     */
    public void insertBefore(String key, Object value) throws DataException {
        cursor.insertBefore(key, value);
    }

    /**
     * Inserts the given element after the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @param value The value to be inserted.
     * @throws DataException If an error occurs.
     */
    public void insertAfter(String key, Object value) throws DataException {
        cursor.insertAfter(key, value);
    }

    /**
     * Inserts a new IData document with the given key before the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @return      The newly inserted IData document.
     * @throws DataException If an error occurs.
     */
    public IData insertDataBefore(String key) throws DataException {
        return cursor.insertDataBefore(key);
    }

    /**
     * Inserts a new IData document with the given key after the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @return      The newly inserted IData document.
     * @throws DataException If an error occurs.
     */
    public IData insertDataAfter(String key) throws DataException {
        return cursor.insertDataAfter(key);
    }

    /**
     * Moves the cursor's position to the next element.
     *
     * @return True if the cursor was repositioned.
     * @throws DataException If an error occurs.
     */
    public boolean next() throws DataException {
        return cursor.next();
    }

    /**
     * Moves the cursor's position to the next element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     * @throws DataException If an error occurs.
     */
    public boolean next(String key) throws DataException {
        return cursor.next(key);
    }

    /**
     * Moves the cursor's position to the previous element.
     *
     * @return True if the cursor was repositioned.
     * @throws DataException If an error occurs.
     */
    public boolean previous() throws DataException {
        return cursor.previous();
    }

    /**
     * Moves the cursor's position to the previous element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     * @throws DataException If an error occurs.
     */
    public boolean previous(String key) throws DataException {
        return cursor.previous(key);
    }

    /**
     * Moves the cursor's position to the first element.
     *
     * @return True if the cursor was repositioned.
     * @throws DataException If an error occurs.
     */
    public boolean first() throws DataException {
        return cursor.first();
    }

    /**
     * Moves the cursor's position to the first element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     * @throws DataException If an error occurs.
     */
    public boolean first(String key) throws DataException {
        return cursor.first(key);
    }

    /**
     * Moves the cursor's position to the last element.
     *
     * @return True if the cursor was repositioned.
     * @throws DataException If an error occurs.
     */
    public boolean last() throws DataException {
        return cursor.last();
    }

    /**
     * Moves the cursor's position to the last element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     * @throws DataException If an error occurs.
     */
    public boolean last(String key) throws DataException {
        return cursor.last(key);
    }

    /**
     * Returns true if the cursor has more elements to be iterated over.
     *
     * @return True if the cursor has more elements to be iterated over.
     * @throws DataException If an error occurs.
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
     * @return A clone of this cursor.
     * @throws DataException If an error occurs.
     */
    public IDataSharedCursor getCursorClone() throws DataException {
        return new IDataSharedCursorEnvelope(cursor.getCursorClone());
    }

    /**
     * Returns whether this cursor supports transactions.
     *
     * @return Whether this cursor supports transactions.
     */
    public boolean isTXNSupported() {
        return cursor.isTXNSupported();
    }

    /**
     * Starts a new transaction.
     *
     * @return The new transaction.
     * @throws TransactionException If an error occurs.
     */
    public ITransaction startTXN() throws TransactionException {
        return cursor.startTXN();
    }

    /**
     * Joins an existing transaction.
     *
     * @param transaction The transaction to be joined.
     * @throws TransactionException If an error occurs.
     */
    public void txnJoin(ITransaction transaction) throws TransactionException {
        cursor.txnJoin(transaction);
    }

    /**
     * Aborts the current transaction.
     *
     * @throws TransactionException If an error occurs.
     */
    public void txnAborted() throws TransactionException {
        cursor.txnAborted();
    }

    /**
     * Commits the current transaction.
     *
     * @throws TransactionException If an error occurs.
     */
    public void txnCommitted() throws TransactionException {
        cursor.txnCommitted();
    }
}
