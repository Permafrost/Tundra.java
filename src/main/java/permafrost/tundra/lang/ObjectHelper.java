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
import org.apache.log4j.Level;
import permafrost.tundra.collection.ListHelper;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.math.BigDecimalHelper;
import permafrost.tundra.math.BigIntegerHelper;
import permafrost.tundra.math.ByteHelper;
import permafrost.tundra.math.DoubleHelper;
import permafrost.tundra.math.FloatHelper;
import permafrost.tundra.math.IntegerHelper;
import permafrost.tundra.math.LongHelper;
import permafrost.tundra.math.ShortHelper;
import permafrost.tundra.mime.MIMETypeHelper;
import permafrost.tundra.security.MessageDigestHelper;
import permafrost.tundra.server.NodePermission;
import permafrost.tundra.server.ServerLogger;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.TimeZoneHelper;
import permafrost.tundra.xml.namespace.IDataNamespaceContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.activation.MimeType;
import javax.xml.datatype.Duration;

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
     * @param firstObject  The first object in the comparison.
     * @param secondObject The seconds object in the comparison.
     * @return True if the two objects are equal, otherwise false.
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
     * @param objects   A list of arguments to be coalesced.
     * @param <T>       The type of the arguments.
     * @return          The first non-null argument, or null.
     */
    public static <T> T coalesce(T... objects) {
        if (objects != null) {
            for (T object : objects) {
                if (object != null) return object;
            }
        }
        return null;
    }

    /**
     * Returns the first non-null argument.
     *
     * @param objects   A list of arguments to be coalesced.
     * @param <T>       The type of the arguments.
     * @return          The first non-null argument, or null.
     */
    public static <T> T coalesce(Iterable<T> objects) {
        if (objects != null) {
            for (T object : objects) {
                if (object != null) return object;
            }
        }
        return null;
    }

    /**
     * Compares two Objects.
     *
     * @param object1   The first Object to be compared.
     * @param object2   The second Object to be compared.
     * @return          A value less than zero if the first object comes before the second object, a value of zero if
     *                  they are equal, or a value of greater than zero if the first object comes after the second
     *                  object according to the comparison of all the keys and values in each document.
     */
    @SuppressWarnings("unchecked")
    public static int compare(Object object1, Object object2) {
        int result = 0;

        if (object1 == null || object2 == null) {
            if (object1 != null) {
                result = 1;
            } else if (object2 != null) {
                result = -1;
            }
        } else {
            if (object1 instanceof Comparable && object1.getClass().isAssignableFrom(object2.getClass())) {
                result = ((Comparable)object1).compareTo(object2);
            } else if (object2 instanceof Comparable && object2.getClass().isAssignableFrom(object1.getClass())) {
                result = ((Comparable)object2).compareTo(object1) * -1;
            } else if (object1 != object2) {
                // last ditch effort: compare two incomparable objects using their hash codes
                result = Integer.valueOf(object1.hashCode()).compareTo(object2.hashCode());
            }
        }

        return result;
    }

    /**
     * Returns true if the given object is considered empty.
     *
     * @param object The object to check the emptiness of.
     * @return True if the object is considered empty.
     */
    public static boolean isEmpty(Object object) {
        return object == null ||
                (object instanceof String && object.equals("")) ||
                (object instanceof Object[] && ((Object[])object).length == 0) ||
                (object instanceof Collection && ((Collection)object).size() == 0) ||
                (object instanceof IData && IDataHelper.size((IData)object) == 0);
    }

    /**
     * Returns true if the given object is an instance of the given class.
     *
     * @param object                    The object to be checked against the given class.
     * @param className                 The name of the class the given object will be tested against.
     * @return                          True if the given object is an instance of the given class.
     */
    public static boolean instance(Object object, String className) {
        boolean isInstance;

        try {
            isInstance = instance(object, className, false);
        } catch(ClassNotFoundException ex) {
            isInstance = false;
        }

        return isInstance;
    }

    /**
     * Returns true if the given object is an instance of the given class.
     *
     * @param object                    The object to be checked against the given class.
     * @param className                 The name of the class the given object will be tested against.
     * @param raise                     If true, throws an exception if the class with the given name does not exist.
     * @return                          True if the given object is an instance of the given class.
     * @throws ClassNotFoundException   If the given class name is not found.
     */
    public static boolean instance(Object object, String className, boolean raise) throws ClassNotFoundException {
        try {
            return className != null && instance(object, Class.forName(className));
        } catch(ClassNotFoundException ex) {
            if (raise) {
                throw ex;
            } else {
                return false;
            }
        }
    }

    /**
     * Returns true if the given object is an instance of the given class.
     *
     * @param object The object to be checked against the given class.
     * @param klass  The class the given object will be tested against.
     * @return True if the given object is an instance of the given class.
     */
    public static boolean instance(Object object, Class klass) {
        return klass != null && klass.isInstance(object);
    }

    /**
     * Returns true if the given object is a primitive or an array of primitives.
     *
     * @param object    An object to check.
     * @return          True if the given object is a primitive or an array of primitives.
     */
    public static boolean isPrimitive(Object object) {
        if (object == null) return false;
        Class klass = object.getClass();
        return klass.isPrimitive() || (klass.isArray() && !(object instanceof Object[]));
    }

    /**
     * Returns a string representation of the given object.
     *
     * @param object The object to stringify.
     * @return A string representation of the given object.
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
     * @param item The item to be converted to an array.
     * @return Either the item itself if it is already an array or a new array containing the item as its only element.
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
     * @param item The item to be converted to a List.
     * @return Either a List containing all elements in the given item if it is an array, or a List containing the item
     * itself if it is not an array.
     */
    public static List<Object> listify(Object item) {
        List<Object> list = new ArrayList<Object>();

        if (item instanceof Object[]) {
            list.addAll(ListHelper.of((Object[])item));
        } else if (item != null) {
            list.add(item);
        }

        return list;
    }

    /**
     * Returns the nearest class which is an ancestor to the classes of the given objects.
     *
     * @param objects One or more objects to return the nearest ancestor for.
     * @return The nearest ancestor class which is an ancestor to the classes of the given objects.
     */
    public static Class<?> getNearestAncestor(Object... objects) {
        return ClassHelper.getNearestAncestor(toClassSet(objects));
    }

    /**
     * Returns the nearest class which is an ancestor to the classes of the given objects.
     *
     * @param objects One or more objects to return the nearest ancestor for.
     * @return The nearest ancestor class which is an ancestor to the classes of the given objects.
     */
    public static Class<?> getNearestAncestor(Collection<?> objects) {
        return ClassHelper.getNearestAncestor(toClassSet(objects));
    }

    /**
     * Returns all the ancestor classes from nearest to furthest for the given class.
     *
     * @param objects One or more objects to fetch the ancestors classes of.
     * @return All the ancestor classes from nearest to furthest for the class of the given object.
     */
    public static Set<Class<?>> getAncestors(Object... objects) {
        return ClassHelper.getAncestors(toClassSet(objects));
    }

    /**
     * Returns all the ancestor classes from nearest to furthest for the given class.
     *
     * @param object An object to fetch the ancestors classes of.
     * @return All the ancestor classes from nearest to furthest for the class of the given object.
     */
    public static Set<Class<?>> getAncestors(Object object) {
        return object == null ? new LinkedHashSet<Class<?>>() : ClassHelper.getAncestors(object.getClass());
    }

    /**
     * Converts the given list of objects to a set of classes.
     *
     * @param objects One or more objects to return a set of classes for.
     * @return The set of classes for the given list of objects.
     */
    private static Set<Class<?>> toClassSet(Object... objects) {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

        if (objects != null) {
            for (Object object : objects) {
                if (object != null) classes.add(object.getClass());
            }
        } else {
            classes.add(Object.class);
        }

        return classes;
    }

    /**
     * Converts the given list of objects to a set of classes.
     *
     * @param objects One or more objects to return a set of classes for.
     * @return The set of classes for the given list of objects.
     */
    private static Set<Class<?>> toClassSet(Collection<?> objects) {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();

        if (objects != null) {
            for (Object object : objects) {
                if (object != null) classes.add(object.getClass());
            }
        } else {
            classes.add(Object.class);
        }

        return classes;
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object      The object to be converted.
     * @param charsetName The character set to use.
     * @param mode        The desired return type of the object.
     * @return The converted object.
     * @throws IOException If an I/O problem occurs.
     */
    public static Object convert(Object object, String charsetName, String mode) throws IOException {
        return convert(object, CharsetHelper.normalize(charsetName), mode);
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object  The object to be converted.
     * @param charset The character set to use.
     * @param mode    The desired return type of the object.
     * @return The converted object.
     * @throws IOException If an I/O problem occurs.
     */
    public static Object convert(Object object, Charset charset, String mode) throws IOException {
        return convert(object, charset, ObjectConvertMode.normalize(mode));
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object The object to be converted.
     * @param mode   The desired return type of the object.
     * @return The converted object.
     * @throws IOException If an I/O problem occurs.
     */
    public static Object convert(Object object, String mode) throws IOException {
        return convert(object, ObjectConvertMode.normalize(mode));
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object The object to be converted.
     * @param mode   The desired return type of the object.
     * @return The converted object.
     * @throws IOException If an I/O problem occurs.
     */
    public static Object convert(Object object, ObjectConvertMode mode) throws IOException {
        return convert(object, CharsetHelper.DEFAULT_CHARSET, mode);
    }

    /**
     * Converts a string, byte array or stream to a string, byte array or stream.
     *
     * @param object  The object to be converted.
     * @param charset The character set to use.
     * @param mode    The desired return type of the object.
     * @return The converted object.
     * @throws IOException If an I/O problem occurs.
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

    /**
     * Converts the given object to the given class.
     *
     * @param object    The object to be converted.
     * @param klass     The class to convert the object to.
     * @param <T>       The class to convert the object to.
     * @return          The given object converted to the given class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object object, Class<T> klass) {
        return convert(object, null, klass);
    }

    /**
     * Converts the given object to the given class.
     *
     * @param object    The object to be converted.
     * @param charset   The charset to use for string related conversions.
     * @param klass     The class to convert the object to.
     * @param <T>       The class to convert the object to.
     * @return          The given object converted to the given class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object object, Charset charset, Class<T> klass) {
        return convert(object, charset, klass, false);
    }

    /**
     * Converts the given object to the given class.
     *
     * @param object    The object to be converted.
     * @param klass     The class to convert the object to.
     * @param required  If true and the object could not be converted, throws an exception.
     * @param <T>       The class to convert the object to.
     * @return          The given object converted to the given class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object object, Class<T> klass, boolean required) {
        return convert(object, null, klass, required);
    }

    /**
     * Converts the given object to the given class.
     *
     * @param object    The object to be converted.
     * @param charset   The charset to use for string related conversions.
     * @param klass     The class to convert the object to.
     * @param required  If true and the object could not be converted, throws an exception.
     * @param <T>       The class to convert the object to.
     * @return          The given object converted to the given class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object object, Charset charset, Class<T> klass, boolean required) {
        if (required) {
            if (object == null) {
                throw new NullPointerException("object must not be null");
            } else if (klass == null) {
                throw new NullPointerException("klass must not be null");
            }
        } else if (object == null || klass == null) {
            return null;
        }

        T value = null;

        try {
            if (klass.isInstance(object)) {
                value = (T)object;
            } else if (klass.isAssignableFrom(byte[].class)) {
                value = (T)BytesHelper.normalize(object, charset);
            } else if (klass.isAssignableFrom(InputStream.class)) {
                value = (T)InputStreamHelper.normalize(object, charset);
            } else if (klass.isAssignableFrom(String.class)) {
                value = (T)StringHelper.normalize(object, charset);
            } else if (klass.isAssignableFrom(Boolean.class)) {
                value = (T)BooleanHelper.normalize(object);
            } else if (klass.isAssignableFrom(BigDecimal.class)) {
                value = (T)BigDecimalHelper.normalize(object);
            } else if (klass.isAssignableFrom(BigInteger.class)) {
                value = (T)BigIntegerHelper.normalize(object);
            } else if (klass.isAssignableFrom(Double.class)) {
                value = (T)DoubleHelper.normalize(object);
            } else if (klass.isAssignableFrom(Float.class)) {
                value = (T)FloatHelper.normalize(object);
            } else if (klass.isAssignableFrom(Long.class)) {
                value = (T)LongHelper.normalize(object);
            } else if (klass.isAssignableFrom(Integer.class)) {
                value = (T)IntegerHelper.normalize(object);
            } else if (klass.isAssignableFrom(Short.class)) {
                value = (T)ShortHelper.normalize(object);
            } else if (klass.isAssignableFrom(Byte.class)) {
                value = (T)ByteHelper.normalize(object);
            } else if (klass.isAssignableFrom(Class.class)) {
                value = (T)ClassHelper.normalize(object);
            } else if (klass.isEnum()) {
                value = EnumHelper.normalize(object, klass);
            } else if (klass.isAssignableFrom(IData.class) && (object instanceof IDataCodable || object instanceof IDataPortable || object instanceof ValuesCodable || object instanceof Map)) {
                value = (T)IDataHelper.toIData(object);
            } else if (klass.isAssignableFrom(Character.class) && object instanceof String && ((String)object).length() > 0) {
                value = (T)Character.valueOf(((String)object).charAt(0));
            } else if (klass.isAssignableFrom(Calendar.class) && object instanceof String) {
                value = (T)DateTimeHelper.parse((String)object);
            } else if (klass.isAssignableFrom(Date.class) && object instanceof String) {
                value = (T)DateTimeHelper.parse((String)object).getTime();
            } else if (klass.isAssignableFrom(Duration.class) && object instanceof String) {
                value = (T)DurationHelper.parse((String)object);
            } else if (klass.isAssignableFrom(Charset.class) && object instanceof String) {
                value = (T)CharsetHelper.of((String)object);
            } else if (klass.isAssignableFrom(MimeType.class) && object instanceof String) {
                value = (T)MIMETypeHelper.of((String)object);
            } else if (klass.isAssignableFrom(NodePermission.class) && object instanceof String) {
                value = (T)NodePermission.normalize((String)object);
            } else if (klass.isAssignableFrom(IDataNamespaceContext.class) && object instanceof IData) {
                value = (T)IDataNamespaceContext.of((IData)object);
            } else if (klass.isAssignableFrom(Locale.class) && object instanceof IData) {
                value = (T)LocaleHelper.toLocale((IData)object);
            } else if (klass.isAssignableFrom(MessageDigest.class) && object instanceof String) {
                value = (T)MessageDigestHelper.normalize((String)object);
            } else if (klass.isAssignableFrom(TimeZone.class) && object instanceof String) {
                value = (T)TimeZoneHelper.get((String)object);
            } else if (klass.isAssignableFrom(File.class) && object instanceof String) {
                value = (T)FileHelper.construct((String)object);
            } else if (klass.isAssignableFrom(Level.class) && (object instanceof String || object instanceof Number)) {
                if (object instanceof String) {
                    value = (T)ServerLogger.toLevel((String)object);
                } else {
                    value = (T)ServerLogger.toLevel(((Number)object).intValue());
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }

        if (required && value == null) {
            throw new UnsupportedOperationException(MessageFormat.format("Unsupported class conversion from {0} to {1}", object.getClass().getName(), klass.getName()));
        }

        return value;
    }
}