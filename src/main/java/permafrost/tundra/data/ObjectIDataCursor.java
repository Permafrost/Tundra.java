/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2021 Lachlan Dowding
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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A read-only IDataCursor that introspects over the publicly accessible fields
 * and getter methods on arbitrary objects.
 */
public class ObjectIDataCursor implements IDataCursor {
    /**
     * The object over which this cursor introspects.
     */
    protected Object object;
    /**
     * The list of publicly accessible fields and methods on the object.
     */
    protected List<AccessibleObject> members;
    /**
     * The current position of the cursor.
     */
    protected int position;

    /**
     * Constructs a new ObjectCursor.
     *
     * @param object    The object the cursor introspects.
     */
    public ObjectIDataCursor(Object object) {
        this(object, -1);
    }

    /**
     * Constructs a new ObjectCursor.
     *
     * @param object    The object the cursor introspects.
     * @param position  The initial position of the cursor.
     */
    protected ObjectIDataCursor(Object object, int position) {
        this.object = object;
        this.position = position;

        if (object == null) {
            members = Collections.emptyList();
        } else {
            Field[] fields = object.getClass().getFields();
            Method[] methods = object.getClass().getMethods();

            members = new ArrayList<AccessibleObject>(fields.length + methods.length);

            // add all the fields
            members.addAll(Arrays.asList(fields));

            // add only the getter methods
            for (Method method : methods) {
                String methodName = method.getName();
                if ((methodName.startsWith("get") || methodName.startsWith("is")) && method.getParameterTypes().length == 0) {
                    members.add(method);
                }
            }
        }
    }

    /**
     * Sets the cursor error mode.
     *
     * @param mode The mode to be set.
     */
    @Override
    public void setErrorMode(int mode) {
        // do nothing
    }

    /**
     * Returns the last error encountered.
     *
     * @return The last error encountered.
     */
    @Override
    public DataException getLastError() {
        return null;
    }

    /**
     * Returns whether the cursor has more errors.
     *
     * @return True if the cursor has more errors.
     */
    @Override
    public boolean hasMoreErrors() {
        return false;
    }

    /**
     * Resets the cursor to be unpositioned.
     */
    @Override
    public void home() {
        position = -1;
    }

    /**
     * Returns the key at the cursor's current position.
     *
     * @return The key at the cursor's current position.
     */
    @Override
    public String getKey() {
        String key = null;
        if (position >= 0 && position < members.size()) {
            AccessibleObject accessibleObject = members.get(position);
            if (accessibleObject instanceof Field) {
                Field field = (Field)accessibleObject;
                key = field.getName();
            } else if (accessibleObject instanceof Method) {
                Method method = (Method)accessibleObject;
                key = method.getName();
            }
        }
        return key;
    }

    /**
     * Sets the key at the cursor's current position.
     *
     * @param key The key to be set.
     */
    @Override
    public void setKey(String key) {
        // do nothing
    }

    /**
     * Returns the value at the cursor's current position.
     *
     * @return The value at the cursor's current position.
     */
    @Override
    public Object getValue() {
        Object value = null;
        if (position >= 0 && position < members.size()) {
            try {
                AccessibleObject accessibleObject = members.get(position);
                if (accessibleObject instanceof Field) {
                    Field field = (Field)accessibleObject;
                    value = field.get(object);
                } else if (accessibleObject instanceof Method) {
                    Method method = (Method)accessibleObject;
                    value = method.invoke(object);
                }
            } catch(IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch(InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
        return value;
    }

    /**
     * Sets the value at the cursor's current position.
     *
     * @param value The value to be set.
     */
    @Override
    public void setValue(Object value) {
        // do nothing
    }

    /**
     * Deletes the element at the cursor's current position.
     *
     * @return True if the element was deleted.
     */
    @Override
    public boolean delete() {
        return false;
    }

    /**
     * Inserts the given element before the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @param value The value to be inserted.
     */
    @Override
    public void insertBefore(String key, Object value) {
        // do nothing
    }

    /**
     * Inserts the given element after the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @param value The value to be inserted.
     */
    @Override
    public void insertAfter(String key, Object value) {
        // do nothing
    }

    /**
     * Inserts a new IData document with the given key before the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @return      The newly inserted IData document.
     */
    @Override
    public IData insertDataBefore(String key) {
        return null;
    }

    /**
     * Inserts a new IData document with the given key after the cursor's current position.
     *
     * @param key   The key to be inserted.
     * @return      The newly inserted IData document.
     */
    @Override
    public IData insertDataAfter(String key) {
        return null;
    }

    /**
     * Moves the cursor's position to the next element.
     *
     * @return True if the cursor was repositioned.
     */
    @Override
    public boolean next() {
        return ++position < members.size();
    }

    /**
     * Moves the cursor's position to the next element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     */
    @Override
    public boolean next(String key) {
        if (key == null) throw new NullPointerException("key must not be null");
        while (++position < members.size()) {
            if (key.equals(getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves the cursor's position to the previous element.
     *
     * @return True if the cursor was repositioned.
     */
    @Override
    public boolean previous() {
        return --position >= 0;
    }

    /**
     * Moves the cursor's position to the previous element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     */
    @Override
    public boolean previous(String key) {
        if (key == null) throw new NullPointerException("key must not be null");
        while (--position >= 0) {
            if (key.equals(getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves the cursor's position to the first element.
     *
     * @return True if the cursor was repositioned.
     */
    @Override
    public boolean first() {
        position = 0;
        return members.size() > 0;
    }

    /**
     * Moves the cursor's position to the first element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     */
    @Override
    public boolean first(String key) {
        position = -1;
        return next(key);
    }

    /**
     * Moves the cursor's position to the last element.
     *
     * @return True if the cursor was repositioned.
     */
    @Override
    public boolean last() {
        position = members.size() - 1;
        return position >= 0;
    }

    /**
     * Moves the cursor's position to the last element with the given key.
     *
     * @param key   The key to reposition to.
     * @return      True if the cursor was repositioned.
     */
    @Override
    public boolean last(String key) {
        position = members.size();
        return previous(key);
    }

    /**
     * Returns true if the cursor has more elements to be iterated over.
     *
     * @return True if the cursor has more elements to be iterated over.
     */
    @Override
    public boolean hasMoreData() {
        return position < members.size();
    }

    /**
     * Destroys this cursor.
     */
    @Override
    public void destroy() {
        // do nothing
    }

    /**
     * Returns a clone of this cursor.
     *
     * @return A clone of this cursor.
     */
    @Override
    public ObjectIDataCursor getCursorClone() {
        return new ObjectIDataCursor(object, position);
    }
}
