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
import com.wm.data.IDataHashCursor;
import com.wm.data.IDataIndexCursor;
import com.wm.data.IDataPortable;
import com.wm.data.IDataSharedCursor;
import com.wm.data.IDataTreeCursor;
import com.wm.txn.ITransaction;
import com.wm.txn.TransactionException;
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
    protected volatile boolean hasWrites = false;

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
    private boolean copyOnWrite() {
        if (hasWrites) return false;

        document = IDataHelper.duplicate(document, false);
        hasWrites = true;
        return hasWrites;
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return new CopyOnWriteIDataCursor(this);
    }

    /**
     * Returns an IDataSharedCursor for this IData object. An IDataSharedCursor contains the basic methods you use to
     * traverse an IData object and get or set elements within it.
     *
     * @return An IDataSharedCursor for this object.
     */
    @Override
    public IDataSharedCursor getSharedCursor() {
        return new CopyOnWriteIDataSharedCursor(this);
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
    private static class CopyOnWriteIDataCursor implements IDataCursor {
        protected CopyOnWriteIDataMap document;
        protected IDataCursor cursor;
        protected Queue<PositionCommand> positioning;

        CopyOnWriteIDataCursor(CopyOnWriteIDataMap document) {
            this(document, null);
        }

        CopyOnWriteIDataCursor(CopyOnWriteIDataMap document, Queue<PositionCommand> positioning) {
            if (document == null) throw new NullPointerException("document must not be null");
            this.document = document;
            if (positioning != null && positioning.size() > 0) {
                this.positioning = new ArrayDeque<PositionCommand>(positioning);
            } else {
                this.positioning = new ArrayDeque<PositionCommand>();
            }
            reposition();
        }

        private void reposition() {
            cursor = document.document.getCursor();

            for (PositionCommand command : positioning) {
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
        }

        private void prepareWrite() {
            if (document.copyOnWrite()) reposition();
        }

        public void setErrorMode(int mode) {
            cursor.setErrorMode(mode);
        }

        public DataException getLastError() {
            return cursor.getLastError();
        }

        public boolean hasMoreErrors() {
            return cursor.hasMoreErrors();
        }

        public void home() {
            positioning.clear();
            cursor.home();
        }

        public String getKey() {
            return cursor.getKey();
        }

        public void setKey(String key) {
            prepareWrite();
            cursor.setKey(key);
        }

        public Object getValue() {
            return normalize(cursor.getValue());
        }

        public void setValue(Object value) {
            prepareWrite();
            cursor.setValue(value);
        }

        public boolean delete() {
            prepareWrite();
            return cursor.delete();
        }

        public void insertBefore(String key, Object value) {
            prepareWrite();
            cursor.insertBefore(key, value);
        }

        public void insertAfter(String key, Object value) {
            prepareWrite();
            cursor.insertAfter(key, value);
        }

        public IData insertDataBefore(String key) {
            prepareWrite();
            return cursor.insertDataBefore(key);
        }

        public IData insertDataAfter(String key) {
            prepareWrite();
            return cursor.insertDataAfter(key);
        }

        public boolean next() {
            boolean success = cursor.next();
            if (success) positioning.add(new PositionCommand(PositionCommandType.NEXT));
            return success;
        }

        public boolean next(String key) {
            boolean success = cursor.next(key);
            if (success) positioning.add(new PositionCommand(PositionCommandType.NEXT_KEY, key));
            return success;
        }

        public boolean previous() {
            boolean success = cursor.previous();
            if (success) positioning.add(new PositionCommand(PositionCommandType.PREVIOUS));
            return success;
        }

        public boolean previous(String key) {
            boolean success = cursor.previous(key);
            if (success) positioning.add(new PositionCommand(PositionCommandType.PREVIOUS_KEY, key));
            return success;
        }

        public boolean first() {
            boolean success = cursor.first();
            if (success) {
                // clear previous positioning commands, as this is an absolute position
                positioning.clear();
                positioning.add(new PositionCommand(PositionCommandType.FIRST));
            }
            return success;
        }

        public boolean first(String key) {
            boolean success = cursor.first(key);
            if (success) positioning.add(new PositionCommand(PositionCommandType.FIRST_KEY, key));
            return success;
        }

        public boolean last() {
            boolean success = cursor.last();
            if (success) {
                // clear previous positioning commands, as this is an absolute position
                positioning.clear();
                positioning.add(new PositionCommand(PositionCommandType.LAST));
            }
            return success;
        }

        public boolean last(String key) {
            boolean success = cursor.last(key);
            if (success) positioning.add(new PositionCommand(PositionCommandType.LAST_KEY, key));
            return success;
        }

        public boolean hasMoreData() {
            return cursor.hasMoreData();
        }

        public void destroy() {
            cursor.destroy();
        }

        public IDataCursor getCursorClone() {
            return new CopyOnWriteIDataCursor(document, positioning);
        }
    }

    /**
     * Copy on write wrapper for an IDataSharedCursor.
     */
    private static class CopyOnWriteIDataSharedCursor implements IDataSharedCursor {
        protected CopyOnWriteIDataMap document;
        protected IDataSharedCursor cursor;
        protected Queue<PositionCommand> positioning;

        public CopyOnWriteIDataSharedCursor(CopyOnWriteIDataMap document) {
            this(document, null);
        }

        public CopyOnWriteIDataSharedCursor(CopyOnWriteIDataMap document, Queue<PositionCommand> positioning) {
            if (document == null) throw new NullPointerException("document must not be null");
            this.document = document;
            if (positioning != null && positioning.size() > 0) {
                this.positioning = new ArrayDeque<PositionCommand>(positioning);
            } else {
                this.positioning = new ArrayDeque<PositionCommand>();
            }
            try {
                reposition();
            } catch(DataException ex) {
                cursor = document.document.getSharedCursor();
            }
        }

        private void reposition() throws DataException {
            cursor = document.document.getSharedCursor();

            for (PositionCommand command : positioning) {
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
        }

        private void prepareWrite() throws DataException {
            if (document.copyOnWrite()) reposition();
        }

        public void home() throws DataException {
            positioning.clear();
            cursor.home();
        }

        public String getKey() throws DataException {
            return cursor.getKey();
        }

        public void setKey(String key) throws DataException {
            prepareWrite();
            cursor.setKey(key);
        }

        public Object getValue() throws DataException {
            return normalize(cursor.getValue());
        }

        public void setValue(Object value) throws DataException {
            prepareWrite();
            cursor.setValue(value);
        }

        public Object getValueReference() throws DataException {
            return normalize(cursor.getValueReference());
        }

        public boolean delete() throws DataException {
            prepareWrite();
            return cursor.delete();
        }

        public void insertBefore(String key, Object value) throws DataException {
            prepareWrite();
            cursor.insertBefore(key, value);
        }

        public void insertAfter(String key, Object value) throws DataException {
            prepareWrite();
            cursor.insertAfter(key, value);
        }

        public IData insertDataBefore(String key) throws DataException {
            prepareWrite();
            return cursor.insertDataBefore(key);
        }

        public IData insertDataAfter(String key) throws DataException {
            prepareWrite();
            return cursor.insertDataAfter(key);
        }

        public boolean next() throws DataException {
            boolean success = cursor.next();
            if (success) positioning.add(new PositionCommand(PositionCommandType.NEXT));
            return success;
        }

        public boolean next(String key) throws DataException {
            boolean success = cursor.next(key);
            if (success) positioning.add(new PositionCommand(PositionCommandType.NEXT_KEY, key));
            return success;
        }

        public boolean previous() throws DataException {
            boolean success = cursor.previous();
            if (success) positioning.add(new PositionCommand(PositionCommandType.PREVIOUS));
            return success;
        }

        public boolean previous(String key) throws DataException {
            boolean success = cursor.previous(key);
            if (success) positioning.add(new PositionCommand(PositionCommandType.PREVIOUS_KEY, key));
            return success;
        }

        public boolean first() throws DataException {
            boolean success = cursor.first();
            if (success) {
                // clear previous positioning commands, as this is an absolute position
                positioning.clear();
                positioning.add(new PositionCommand(PositionCommandType.FIRST));
            }
            return success;
        }

        public boolean first(String key) throws DataException {
            boolean success = cursor.first(key);
            if (success) positioning.add(new PositionCommand(PositionCommandType.FIRST_KEY, key));
            return success;
        }

        public boolean last() throws DataException {
            boolean success = cursor.last();
            if (success) {
                // clear previous positioning commands, as this is an absolute position
                positioning.clear();
                positioning.add(new PositionCommand(PositionCommandType.LAST));
            }
            return success;
        }

        public boolean last(String key) throws DataException {
            boolean success = cursor.last(key);
            if (success) positioning.add(new PositionCommand(PositionCommandType.LAST_KEY, key));
            return success;
        }

        public boolean hasMoreData() throws DataException {
            return cursor.hasMoreData();
        }

        public void destroy() {
            cursor.destroy();
        }

        public IDataSharedCursor getCursorClone() throws DataException {
            return new CopyOnWriteIDataSharedCursor(document, positioning);
        }

        public boolean isTXNSupported() {
            return cursor.isTXNSupported();
        }

        public ITransaction startTXN() throws TransactionException {
            return cursor.startTXN();
        }

        public void txnJoin(ITransaction transaction) throws TransactionException {
            cursor.txnJoin(transaction);
        }

        public void txnAborted() throws TransactionException {
            cursor.txnAborted();
        }

        public void txnCommitted() throws TransactionException {
            cursor.txnCommitted();
        }
    }

    private static enum PositionCommandType {
        FIRST, FIRST_KEY, NEXT, NEXT_KEY, PREVIOUS, PREVIOUS_KEY, LAST, LAST_KEY;
    }

    private static class PositionCommand {
        PositionCommandType type;
        String key;

        public PositionCommand(PositionCommandType type) {
            this(type, null);
        }

        public PositionCommand(PositionCommandType type, String key) {
            this.type = type;
            this.key = key;
        }

        public PositionCommandType getType() {
            return type;
        }

        public String getKey() {
            return key;
        }
    }
}
