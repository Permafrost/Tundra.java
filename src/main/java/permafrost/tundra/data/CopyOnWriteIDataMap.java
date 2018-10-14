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
import com.wm.data.IDataFactory;
import com.wm.data.IDataHashCursor;
import com.wm.data.IDataIndexCursor;
import com.wm.data.IDataPortable;
import com.wm.data.IDataSharedCursor;
import com.wm.data.IDataTreeCursor;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

/**
 * Wraps an IData in an IData and Map compatible wrapper that makes copies of the wrapped IData and its
 * nested IData children when they are written to.
 */
public class CopyOnWriteIDataMap extends IDataMap implements Cloneable, Serializable {
    private static final long serialVersionUID = 1;

    /**
     * Whether this object has been written to.
     */
    protected volatile boolean copied = false;

    /**
     * Construct a new CopyOnWriteIDataMap object.
     *
     * @param document The IData document to wrap in a CopyOnWriteIDataMap representation.
     */
    public CopyOnWriteIDataMap(IData document) {
        super(document);
    }

    /**
     * Construct a new CopyOnWriteIDataMap object.
     *
     * @param document   The IData document to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public CopyOnWriteIDataMap(IData document, IDataComparator comparator) {
        this(document);
        setComparator(comparator);
    }

    /**
     * Constructs a new CopyOnWriteIDataMap wrapping the given IDataCodable object.
     *
     * @param codable The IDataCodable object to be wrapped.
     */
    public CopyOnWriteIDataMap(IDataCodable codable) {
        super(codable);
    }

    /**
     * Constructs a new CopyOnWriteIDataMap wrapping the given IDataCodable object.
     *
     * @param codable    The IDataCodable object to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public CopyOnWriteIDataMap(IDataCodable codable, IDataComparator comparator) {
        this(codable);
        setComparator(comparator);
    }

    /**
     * Constructs a new CopyOnWriteIDataMap wrapping the given IDataPortable object.
     *
     * @param portable The IDataPortable object to be wrapped.
     */
    public CopyOnWriteIDataMap(IDataPortable portable) {
        super(portable);
    }

    /**
     * Constructs a new CopyOnWriteIDataMap wrapping the given IDataPortable object.
     *
     * @param portable   The IDataPortable object to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public CopyOnWriteIDataMap(IDataPortable portable, IDataComparator comparator) {
        this(portable);
        setComparator(comparator);
    }

    /**
     * Constructs a new CopyOnWriteIDataMap wrapping the given ValuesCodable object.
     *
     * @param codable The ValuesCodable object to be wrapped.
     */
    public CopyOnWriteIDataMap(ValuesCodable codable) {
        super(codable);
    }

    /**
     * Constructs a new CopyOnWriteIDataMap wrapping the given ValuesCodable object.
     *
     * @param codable    The ValuesCodable object to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public CopyOnWriteIDataMap(ValuesCodable codable, IDataComparator comparator) {
        this(codable);
        setComparator(comparator);
    }

    /**
     * Constructs a new CopyOnWriteIDataMap seeded with the given Map of key value entries.
     *
     * @param map The map to see this new object with.
     */
    public CopyOnWriteIDataMap(Map<? extends String, ?> map) {
        this(IDataHelper.toIData(map));
    }

    /**
     * Constructs a new CopyOnWriteIDataMap seeded with the given Map of key value entries.
     *
     * @param map        The map to see this new object with.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public CopyOnWriteIDataMap(Map<? extends String, ?> map, IDataComparator comparator) {
        this(map);
        setComparator(comparator);
    }

    /**
     * Returns a new CopyOnWriteIDataMap wrapping the given IData document.
     *
     * @param document  The document to be wrapped.
     * @return          A new CopyOnWriteIDataMap wrapping the given IData document.
     */
    public static CopyOnWriteIDataMap of(IData document) {
        return new CopyOnWriteIDataMap(document);
    }

    /**
     * Returns a new CopyOnWriteIDataMap[] representation of the given IData[] document list.
     *
     * @param array     An IData[] document list.
     * @return          A new CopyOnWriteIDataMap[] representation of the given IData[] document list.
     */
    public static CopyOnWriteIDataMap[] of(IData[] array) {
        if (array == null) return null;

        CopyOnWriteIDataMap[] output = new CopyOnWriteIDataMap[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                output[i] = CopyOnWriteIDataMap.of(array[i]);
            }
        }

        return output;
    }

    /**
     * Converts the given value if it is an IData or IData[] compatible object to a CopyOnWriteIDataMap or
     * CopyOnWriteIDataMap[] respectively.
     *
     * @param value The value to be normalized.
     * @return      If the value is an IData or IData[] compatible object, a new CopyOnWriteIDataMap or
     *              CopyOnWriteIDataMap[] respectively is returned which wraps the given value, otherwise
     *              the value itself is returned unmodified.
     */
    private static Object normalize(Object value) {
        if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
            value = CopyOnWriteIDataMap.of(IDataHelper.toIDataArray(value));
        } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
            value = CopyOnWriteIDataMap.of(IDataHelper.toIData(value));
        }
        return value;
    }

    /**
     * Updates document to be a copy of itself the first time this method is called, subsequent calls do nothing.
     *
     * @return True if a copy was made, false otherwise.
     */
    private synchronized boolean copyOnWrite() {
        if (this.copied) return false;

        IData clone = IDataFactory.create();
        IDataCursor documentCursor = this.document.getCursor();
        IDataCursor cloneCursor = clone.getCursor();

        while(documentCursor.next()) {
            cloneCursor.insertAfter(documentCursor.getKey(), normalize(documentCursor.getValue()));
        }

        documentCursor.destroy();
        cloneCursor.destroy();

        this.document = clone;
        this.copied = true;

        return this.copied;
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return new CopyOnWriteIDataCursor();
    }

    /**
     * Returns an IDataSharedCursor for this IData object. An IDataSharedCursor contains the basic methods you use to
     * traverse an IData object and get or set elements within it.
     *
     * @return An IDataSharedCursor for this object.
     */
    @Override
    public IDataSharedCursor getSharedCursor() {
        return new CopyOnWriteIDataSharedCursor();
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

    /**
     * Returns a newly created IData object.
     *
     * @return A newly created IData object.
     */
    public static IData create() {
        return new CopyOnWriteIDataMap((IData)null);
    }

    /**
     * Returns a clone of this IData object.
     *
     * @return A clone of this IData object.
     */
    @Override
    public IDataMap clone() {
        return new CopyOnWriteIDataMap(document);
    }

    /**
     * Copy on write wrapper for an IDataCursor.
     */
    private class CopyOnWriteIDataCursor extends IDataCursorEnvelope {
        /**
         * The current position of the cursor.
         */
        private Queue<PositionCommand> position;
        /**
         * Whether a copy of the owning IData document has been initiated or not yet.
         */
        protected volatile boolean copied;

        /**
         * Constructs a new cursor.
         */
        CopyOnWriteIDataCursor() {
            this(null);
        }

        /**
         * Constructs a new cursor with the given position.
         *
         * @param position The initial position of the cursor.
         */
        CopyOnWriteIDataCursor(Queue<PositionCommand> position) {
            super(CopyOnWriteIDataMap.this.document.getCursor());
            if (position != null && position.size() > 0) {
                this.position = new ArrayDeque<PositionCommand>(position);
            } else {
                this.position = new ArrayDeque<PositionCommand>();
            }
            initialize();
        }

        /**
         * Initializes this cursor.
         */
        protected void initialize() {
            if (cursor != null) cursor.destroy();
            cursor = CopyOnWriteIDataMap.this.document.getCursor();

            for (PositionCommand command : position) {
                switch(command.getType()) {
                    case FIRST:
                        cursor.first();
                        break;
                    case FIRST_KEY:
                        cursor.first(command.getKey());
                        break;
                    case NEXT:
                        cursor.next();
                        break;
                    case NEXT_KEY:
                        cursor.next(command.getKey());
                        break;
                    case PREVIOUS:
                        cursor.previous();
                        break;
                    case PREVIOUS_KEY:
                        cursor.previous(command.getKey());
                        break;
                    case LAST:
                        cursor.last();
                        break;
                    case LAST_KEY:
                        cursor.last(command.getKey());
                        break;
                }
            }

            position.clear();
            copied = CopyOnWriteIDataMap.this.copied;
        }

        /**
         * Makes a copy of the owning IData document, if required.
         *
         * @return True if a copy was made or the state of the owning IData document changed in the interim.
         */
        private boolean copy() {
            boolean wasCopied = copyOnWrite();
            boolean stateChanged = (wasCopied || (this.copied != CopyOnWriteIDataMap.this.copied));
            if (stateChanged) initialize();
            return stateChanged;
        }

        /**
         * Returns the given value, optionally copied if required.
         *
         * @param value The value to be normalized.
         * @return      The normalized value.
         */
        private Object normalize(Object value) {
            if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[] || value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                if (copy()) {
                    value = cursor.getValue();
                }
            }
            return value;
        }

        /**
         * Resets this cursor to be unpositioned.
         */
        @Override
        public void home() {
            position.clear();
            cursor.home();
        }

        /**
         * Sets the key at the cursor's current position.
         *
         * @param key The key to be set.
         */
        @Override
        public void setKey(String key) {
            copy();
            cursor.setKey(key);
        }

        /**
         * Returns the value at the cursor's current position.
         *
         * @return The value at the cursor's current position.
         */
        @Override
        public Object getValue() {
            return normalize(cursor.getValue());
        }

        /**
         * Sets the value at the cursor's current position.
         *
         * @param value The value to be set.
         */
        @Override
        public void setValue(Object value) {
            copy();
            cursor.setValue(value);
        }

        /**
         * Deletes the element at the cursor's current position.
         *
         * @return True if the element was deleted.
         */
        @Override
        public boolean delete() {
            copy();
            return cursor.delete();
        }

        /**
         * Inserts the given element before the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @param value The value to be inserted.
         */
        @Override
        public void insertBefore(String key, Object value) {
            copy();
            cursor.insertBefore(key, value);
        }

        /**
         * Inserts the given element after the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @param value The value to be inserted.
         */
        @Override
        public void insertAfter(String key, Object value) {
            copy();
            cursor.insertAfter(key, value);
        }

        /**
         * Inserts a new IData document with the given key before the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @return      The newly inserted IData document.
         */
        @Override
        public IData insertDataBefore(String key) {
            copy();
            return cursor.insertDataBefore(key);
        }

        /**
         * Inserts a new IData document with the given key after the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @return      The newly inserted IData document.
         */
        @Override
        public IData insertDataAfter(String key) {
            copy();
            return cursor.insertDataAfter(key);
        }

        /**
         * Moves the cursor's position to the next element.
         *
         * @return True if the cursor was repositioned.
         */
        @Override
        public boolean next() {
            boolean success = cursor.next();
            if (success) position.add(new PositionCommand(PositionCommandType.NEXT));
            return success;
        }

        /**
         * Moves the cursor's position to the next element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         */
        @Override
        public boolean next(String key) {
            boolean success = cursor.next(key);
            if (success) position.add(new PositionCommand(PositionCommandType.NEXT_KEY, key));
            return success;
        }

        /**
         * Moves the cursor's position to the previous element.
         *
         * @return True if the cursor was repositioned.
         */
        @Override
        public boolean previous() {
            boolean success = cursor.previous();
            if (success) position.add(new PositionCommand(PositionCommandType.PREVIOUS));
            return success;
        }

        /**
         * Moves the cursor's position to the previous element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         */
        @Override
        public boolean previous(String key) {
            boolean success = cursor.previous(key);
            if (success) position.add(new PositionCommand(PositionCommandType.PREVIOUS_KEY, key));
            return success;
        }

        /**
         * Moves the cursor's position to the first element.
         *
         * @return True if the cursor was repositioned.
         */
        @Override
        public boolean first() {
            boolean success = cursor.first();
            if (success) {
                // clear previous position commands, as this is an absolute position
                position.clear();
                position.add(new PositionCommand(PositionCommandType.FIRST));
            }
            return success;
        }

        /**
         * Moves the cursor's position to the first element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         */
        @Override
        public boolean first(String key) {
            boolean success = cursor.first(key);
            if (success) position.add(new PositionCommand(PositionCommandType.FIRST_KEY, key));
            return success;
        }

        /**
         * Moves the cursor's position to the last element.
         *
         * @return True if the cursor was repositioned.
         */
        @Override
        public boolean last() {
            boolean success = cursor.last();
            if (success) {
                // clear previous position commands, as this is an absolute position
                position.clear();
                position.add(new PositionCommand(PositionCommandType.LAST));
            }
            return success;
        }

        /**
         * Moves the cursor's position to the last element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         */
        @Override
        public boolean last(String key) {
            boolean success = cursor.last(key);
            if (success) position.add(new PositionCommand(PositionCommandType.LAST_KEY, key));
            return success;
        }

        /**
         * Returns a clone of this cursor.
         *
         * @return A clone of this cursor.
         */
        @Override
        public IDataCursor getCursorClone() {
            return new CopyOnWriteIDataCursor(position);
        }
    }

    /**
     * Copy on write wrapper for an IDataSharedCursor.
     */
    private class CopyOnWriteIDataSharedCursor extends IDataSharedCursorEnvelope {
        /**
         * The current position of the cursor.
         */
        private Queue<PositionCommand> position;
        /**
         * Whether a copy of the owning IData document has been initiated or not yet.
         */
        protected volatile boolean copied;

        /**
         * Constructs a new cursor.
         */
        public CopyOnWriteIDataSharedCursor() {
            this(null);
        }

        /**
         * Constructs a new cursor with the given position.
         *
         * @param position The initial position for the cursor.
         */
        public CopyOnWriteIDataSharedCursor(Queue<PositionCommand> position) {
            super(CopyOnWriteIDataMap.this.document.getSharedCursor());
            if (position != null && position.size() > 0) {
                this.position = new ArrayDeque<PositionCommand>(position);
            } else {
                this.position = new ArrayDeque<PositionCommand>();
            }

            try {
                initialize();
            } catch(DataException ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Initializes the cursor.
         *
         * @throws DataException If an error occurs.
         */
        private void initialize() throws DataException {
            if (cursor != null) cursor.destroy();
            cursor = CopyOnWriteIDataMap.this.document.getSharedCursor();

            for (PositionCommand command : position) {
                switch(command.getType()) {
                    case FIRST:
                        cursor.first();
                        break;
                    case FIRST_KEY:
                        cursor.first(command.getKey());
                        break;
                    case NEXT:
                        cursor.next();
                        break;
                    case NEXT_KEY:
                        cursor.next(command.getKey());
                        break;
                    case PREVIOUS:
                        cursor.previous();
                        break;
                    case PREVIOUS_KEY:
                        cursor.previous(command.getKey());
                        break;
                    case LAST:
                        cursor.last();
                        break;
                    case LAST_KEY:
                        cursor.last(command.getKey());
                        break;
                }
            }

            position.clear();
            copied = CopyOnWriteIDataMap.this.copied;
        }

        /**
         * Makes a copy of the owning IData document, if required.
         *
         * @return True if a copy was made or the state of the owning IData document changed in the interim.
         * @throws DataException If an error occurs.
         */
        private boolean copy() throws DataException {
            boolean wasCopied = copyOnWrite();
            boolean stateChanged = (wasCopied || (this.copied != CopyOnWriteIDataMap.this.copied));
            if (stateChanged) initialize();
            return stateChanged;
        }

        /**
         * Returns the given value, optionally copied if required.
         *
         * @param value The value to be normalized.
         * @return      The normalized value.
         * @throws DataException If an error occurs.
         */
        private Object normalize(Object value) throws DataException {
            if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[] || value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                if (copy()) {
                    value = cursor.getValue();
                }
            }
            return value;
        }

        /**
         * Resets the cursor to be unpositioned.
         *
         * @throws DataException If an error occurs.
         */
        @Override
        public void home() throws DataException {
            position.clear();
            cursor.home();
        }

        /**
         * Sets the key at the cursor's current position.
         *
         * @param key The key to be set.
         * @throws DataException If an error occurs.
         */
        @Override
        public void setKey(String key) throws DataException {
            copy();
            cursor.setKey(key);
        }

        /**
         * Returns the value at the cursor's current position.
         *
         * @return The value at the cursor's current position.
         * @throws DataException If an error occurs.
         */
        @Override
        public Object getValue() throws DataException {
            return normalize(cursor.getValue());
        }

        /**
         * Sets the value at the cursor's current position.
         *
         * @param value The value to be set.
         * @throws DataException If an error occurs.
         */
        @Override
        public void setValue(Object value) throws DataException {
            copy();
            cursor.setValue(value);
        }

        /**
         * Returns the value at the cursor's current position.
         *
         * @return The value at the cursor's current position.
         * @throws DataException If an error occurs.
         */
        @Override
        public Object getValueReference() throws DataException {
            return normalize(cursor.getValueReference());
        }

        /**
         * Deletes the element at the cursor's current position.
         *
         * @return Returns true if the element was deleted.
         * @throws DataException If an error occurs.
         */
        @Override
        public boolean delete() throws DataException {
            copy();
            return cursor.delete();
        }

        /**
         * Inserts a new element before the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @param value The value to be inserted.
         * @throws DataException If an error occurred.
         */
        @Override
        public void insertBefore(String key, Object value) throws DataException {
            copy();
            cursor.insertBefore(key, value);
        }

        /**
         * Inserts a new element after the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @param value The value to be inserted.
         * @throws DataException If an error occurs.
         */
        @Override
        public void insertAfter(String key, Object value) throws DataException {
            copy();
            cursor.insertAfter(key, value);
        }

        /**
         * Inserts a new IData document with the given key before the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @return      The new IData document that was inserted.
         * @throws DataException If an error occurs.
         */
        @Override
        public IData insertDataBefore(String key) throws DataException {
            copy();
            return cursor.insertDataBefore(key);
        }

        /**
         * Inserts a new IData document with the given key after the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @return      The new IData document that was inserted.
         * @throws DataException If an error occurs.
         */
        @Override
        public IData insertDataAfter(String key) throws DataException {
            copy();
            return cursor.insertDataAfter(key);
        }

        /**
         * Moves the cursor's position to the next element.
         *
         * @return True if the cursor was repositioned.
         * @throws DataException If an error occurs.
         */
        @Override
        public boolean next() throws DataException {
            boolean success = cursor.next();
            if (success) position.add(new PositionCommand(PositionCommandType.NEXT));
            return success;
        }

        /**
         * Moves the cursor's position to the next element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         * @throws DataException If an error occurs.
         */
        @Override
        public boolean next(String key) throws DataException {
            boolean success = cursor.next(key);
            if (success) position.add(new PositionCommand(PositionCommandType.NEXT_KEY, key));
            return success;
        }

        /**
         * Moves the cursor's position to the previous element.
         *
         * @return True if the cursor was repositioned.
         * @throws DataException If an error occurs.
         */
        @Override
        public boolean previous() throws DataException {
            boolean success = cursor.previous();
            if (success) position.add(new PositionCommand(PositionCommandType.PREVIOUS));
            return success;
        }

        /**
         * Moves the cursor's position to the previous element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         * @throws DataException If an error occurs.
         */
        @Override
        public boolean previous(String key) throws DataException {
            boolean success = cursor.previous(key);
            if (success) position.add(new PositionCommand(PositionCommandType.PREVIOUS_KEY, key));
            return success;
        }

        /**
         * Moves the cursor's position to the first element.
         *
         * @return True if the cursor was repositioned.
         * @throws DataException If an error occurs.
         */
        @Override
        public boolean first() throws DataException {
            boolean success = cursor.first();
            if (success) {
                // clear previous position commands, as this is an absolute position
                position.clear();
                position.add(new PositionCommand(PositionCommandType.FIRST));
            }
            return success;
        }

        /**
         * Moves the cursor's position to the first element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         * @throws DataException If an error occurs.
         */
        @Override
        public boolean first(String key) throws DataException {
            boolean success = cursor.first(key);
            if (success) position.add(new PositionCommand(PositionCommandType.FIRST_KEY, key));
            return success;
        }

        /**
         * Moves the cursor's position to the last element.
         *
         * @return True if the cursor was repositioned.
         * @throws DataException If an error occurs.
         */
        @Override
        public boolean last() throws DataException {
            boolean success = cursor.last();
            if (success) {
                // clear previous position commands, as this is an absolute position
                position.clear();
                position.add(new PositionCommand(PositionCommandType.LAST));
            }
            return success;
        }

        /**
         * Moves the cursor's position to the last element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         * @throws DataException If an error occurs.
         */
        @Override
        public boolean last(String key) throws DataException {
            boolean success = cursor.last(key);
            if (success) position.add(new PositionCommand(PositionCommandType.LAST_KEY, key));
            return success;
        }

        /**
         * Returns a clone of this cursor.
         *
         * @return A clone of this cursor.
         * @throws DataException If an error occurs.
         */
        @Override
        public IDataSharedCursor getCursorClone() throws DataException {
            return new CopyOnWriteIDataSharedCursor(position);
        }
    }

    /**
     * The different types of position commands possible with an IDataCursor.
     */
    private static enum PositionCommandType {
        FIRST, FIRST_KEY, NEXT, NEXT_KEY, PREVIOUS, PREVIOUS_KEY, LAST, LAST_KEY;
    }

    /**
     * Represents a single position command for an IDataCursor.
     */
    private static class PositionCommand {
        /**
         * The type of position command.
         */
        protected PositionCommandType type;
        /**
         * The optional key used for positioning.
         */
        protected String key;

        /**
         * Constructs a new position command object with the given type.
         *
         * @param type The type of position command.
         */
        public PositionCommand(PositionCommandType type) {
            this(type, null);
        }

        /**
         * Constructs a new position command with the given type and key.
         *
         * @param type  The type of position command.
         * @param key   The key used for applying the command.
         */
        public PositionCommand(PositionCommandType type, String key) {
            this.type = type;
            this.key = key;
        }

        /**
         * Returns the type of position command.
         *
         * @return The type of position command.
         */
        public PositionCommandType getType() {
            return type;
        }

        /**
         * Returns the key used for applying the command.
         *
         * @return The key used for applying the command, or null if not required.
         */
        public String getKey() {
            return key;
        }
    }
}
