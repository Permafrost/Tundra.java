/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lachlan Dowding
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

package permafrost.tundra.lang;

import com.wm.data.IData;
import com.wm.data.IDataPortable;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSNode;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.io.InputStreamHelper;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A collection of convenience methods for working with Objects.
 */
public final class ObjectHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ObjectHelper() {}

    /**
     * Returns true if the given objects are considered equal; correctly supports comparing com.wm.data.IData objects.
     *
     * @param firstObject   The first object in the comparison.
     * @param secondObject  The seconds object in the comparison.
     * @return              True if the two objects are equal, otherwise false.
     */
    public static boolean equal(Object firstObject, Object secondObject) {
        boolean result;
        if (firstObject != null && secondObject != null) {
            if (firstObject instanceof IData && secondObject instanceof IData) {
                result = IDataUtil.equals((IData)firstObject, (IData)secondObject);
            } else {
                result = firstObject.equals(secondObject);
            }
        } else {
            result = (firstObject == null && secondObject == null);
        }

        return result;
    }

    /**
     * Returns the first non-null argument.
     *
     * @param objects A list of arguments to be coalesced.
     * @param <T>     The type of the arguments.
     * @return        The first non-null argument, or null.
     */
    public static <T> T coalesce(T ... objects) {
        if (objects == null) return null;

        T output = null;

        for (T item : objects) {
            if (item != null) {
                output = item;
                break;
            }
        }

        return output;
    }

    /**
     * Returns true if the given object is considered empty.
     *
     * @param object    The object to check the emptiness of.
     * @return          True if the object is considered empty.
     */
    public static boolean isEmpty(Object object) {
        return object == null ||
               (object instanceof String && object.equals("")) ||
               (object instanceof Object[] && ((Object[])object).length == 0) ||
               (object instanceof Collection && ((Collection)object).size() == 0);
    }

    /**
     * Returns true if the given object is an instance of the given class.
     *
     * @param object                    The object to be checked against the given class.
     * @param className                 The name of the class the given object will be tested against.
     * @return                          True if the given object is an instance of the given class.
     * @throws ClassNotFoundException   If the given class name is not found.
     */
    public static boolean instance(Object object, String className) throws ClassNotFoundException {
        return className != null && instance(object, Class.forName(className));
    }

    /**
     * Returns true if the given object is an instance of the given class.
     *
     * @param object    The object to be checked against the given class.
     * @param klass     The class the given object will be tested against.
     * @return          True if the given object is an instance of the given class.
     */
    public static boolean instance(Object object, Class klass) {
        return object != null && klass != null && klass.isInstance(object);
    }

    /**
     * Returns a string representation of the given object.
     *
     * @param object    The object to stringify.
     * @return          A string representation of the given object.
     */
    public static String stringify(Object object) {
        if (object == null) return null;

        String output;

        if (object instanceof NSNode) {
            output = ((NSNode)object).getNSName().toString();
        } else if (object instanceof IData[] || object instanceof Table || object instanceof IDataCodable[] || object instanceof IDataPortable[] || object instanceof ValuesCodable[]) {
            output = ArrayHelper.stringify(IDataHelper.toIDataArray(object));
        } else if (object instanceof IData || object instanceof IDataCodable || object instanceof IDataPortable || object instanceof ValuesCodable) {
            output = IDataHelper.toIData(object).toString();
        } else if (object instanceof Object[][]) {
            output = TableHelper.stringify((Object[][])object);
        } else if (object instanceof Object[]) {
            output = ArrayHelper.stringify((Object[])object);
        } else {
            output = object.toString();
        }

        return output;
    }

    /**
     * Returns the given item if it is already an array, or a new array with the given type whose first element is the
     * given item.
     *
     * @param item  The item to be converted to an array.
     * @return      Either the item itself if it is already an array or a new array containing the item as its only
     *              element.
     */
    public static Object[] arrayify(Object item) {
        Object[] array = null;

        if (item instanceof Object[]) {
            array = (Object[])item;
        } else if (item != null) {
            array = (Object[])Array.newInstance(item.getClass(), 1);
            array[0] = item;
        }

        return array;
    }

    /**
     * Returns a List containing all elements in the given item if it is an array, or a List containing the item itself
     * if it is not an array.
     *
     * @param item  The item to be converted to a List.
     * @return      Either a List containing all elements in the given item if it is an array, or a List containing the
     *              item itself if it is not an array.
     */
    public static List<Object> listify(Object item) {
        List<Object> list = new ArrayList<Object>();

        if (item instanceof Object[]) {
            list.addAll(ArrayHelper.toList((Object[])item));
        } else if (item != null) {
            list.add(item);
        }

        return list;
    }

    /**
     * Returns the nearest class which is an ancestor to the classes of the given objects.
     *
     * @param objects   One or more objects to return the nearest ancestor for.
     * @return          The nearest ancestor class which is an ancestor to the classes of the given objects.
     */
    public static Class<?> getNearestAncestor(Object... objects) {
        return ClassHelper.getNearestAncestor(toClassSet(objects));
    }

    /**
     * Returns the nearest class which is an ancestor to the classes of the given objects.
     *
     * @param objects   One or more objects to return the nearest ancestor for.
     * @return          The nearest ancestor class which is an ancestor to the classes of the given objects.
     */
    public static Class<?> getNearestAncestor(Collection<?> objects) {
        return ClassHelper.getNearestAncestor(toClassSet(objects));
    }

    /**
     * Returns all the ancestor classes from nearest to furthest for the given class.
     *
     * @param objects   One or more objects to fetch the ancestors classes of.
     * @return          All the ancestor classes from nearest to furthest for the class of the given object.
     */
    public static Set<Class<?>> getAncestors(Object... objects) {
        return ClassHelper.getAncestors(toClassSet(objects));
    }

    /**
     * Returns all the ancestor classes from nearest to furthest for the given class.
     *
     * @param object    An object to fetch the ancestors classes of.
     * @return          All the ancestor classes from nearest to furthest for the class of the given object.
     */
    public static Set<Class<?>> getAncestors(Object object) {
        return object == null ? new TreeSet<Class<?>>() : ClassHelper.getAncestors(object.getClass());
    }

    /**
     * Converts the given list of objects to a set of classes.
     *
     * @param objects   One or more objects to return a set of classes for.
     * @return          The set of classes for the given list of objects.
     */
    private static Set<Class<?>> toClassSet(Object... objects) {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

        for (Object object : objects) {
            if (object != null) classes.add(object.getClass());
        }

        return classes;
    }

    /**
     * Converts the given list of objects to a set of classes.
     *
     * @param objects   One or more objects to return a set of classes for.
     * @return          The set of classes for the given list of objects.
     */
    private static Set<Class<?>> toClassSet(Collection<?> objects) {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

        for (Object object : objects) {
            if (object != null) classes.add(object.getClass());
        }

        return classes;
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object        The object to be converted.
     * @param charsetName   The character set to use.
     * @param mode          The desired return type of the object.
     * @return              The converted object.
     * @throws IOException  If an I/O problem occurs.
     */
    public static Object convert(Object object, String charsetName, String mode) throws IOException {
        return convert(object, CharsetHelper.normalize(charsetName), mode);
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object        The object to be converted.
     * @param charset       The character set to use.
     * @param mode          The desired return type of the object.
     * @return              The converted object.
     * @throws IOException  If an I/O problem occurs.
     */
    public static Object convert(Object object, Charset charset, String mode) throws IOException {
        return convert(object, charset, ObjectConvertMode.normalize(mode));
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object        The object to be converted.
     * @param mode          The desired return type of the object.
     * @return              The converted object.
     * @throws IOException  If an I/O problem occurs.
     */
    public static Object convert(Object object, String mode) throws IOException {
        return convert(object, ObjectConvertMode.normalize(mode));
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object        The object to be converted.
     * @param mode          The desired return type of the object.
     * @return              The converted object.
     * @throws IOException  If an I/O problem occurs.
     */
    public static Object convert(Object object, ObjectConvertMode mode) throws IOException {
        return convert(object, CharsetHelper.DEFAULT_CHARSET, mode);
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object        The object to be converted.
     * @param charset       The character set to use.
     * @param mode          The desired return type of the object.
     * @return              The converted object.
     * @throws IOException  If an I/O problem occurs.
     */
    public static Object convert(Object object, Charset charset, ObjectConvertMode mode) throws IOException {
        if (object == null) return null;

        mode = ObjectConvertMode.normalize(mode);

        if (mode == ObjectConvertMode.BYTES) {
            object = BytesHelper.normalize(object, charset);
        } else if (mode == ObjectConvertMode.STRING) {
            object = StringHelper.normalize(object, charset);
        } else if (mode == ObjectConvertMode.BASE64) {
            object = BytesHelper.base64Encode(BytesHelper.normalize(object, charset));
        } else if (mode == ObjectConvertMode.STREAM) {
            object = InputStreamHelper.normalize(object, charset);
        } else {
            throw new IllegalArgumentException("Unsupported conversion mode specified: " + mode);
        }

        return object;
    }
}
