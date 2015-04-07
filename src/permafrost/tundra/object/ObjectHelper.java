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

package permafrost.tundra.object;

import com.wm.data.IData;
import com.wm.data.IDataUtil;
import permafrost.tundra.exception.BaseException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ObjectHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ObjectHelper() {}

    /**
     * Returns true if the given objects are considered equal; correctly supports comparing com.wm.data.IData objects.
     *
     * @param firstObject  The first object in the comparison.
     * @param secondObject The seconds object in the comparison.
     * @return             True if the two objects are equal, otherwise false.
     */
    public static boolean equal(java.lang.Object firstObject, java.lang.Object secondObject) {
        boolean result = true;
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
     * Returns true if the given object is an instance of the given class.
     *
     * @param object    The object to be checked against the given class.
     * @param className The name of the class the given object will be tested against.
     * @return          True if the given object is an instance of the given class.
     * @throws BaseException If the given class name is not found.
     */
    public static boolean instance(java.lang.Object object, java.lang.String className) throws BaseException {
        boolean instance = false;
        try {
            instance = className != null && instance(object, Class.forName(className));
        } catch(ClassNotFoundException ex) {
            throw new BaseException(ex);
        }
        return instance;
    }

    /**
     * Returns true if the given object is an instance of the given class.
     *
     * @param object    The object to be checked against the given class.
     * @param klass     The class the given object will be tested against.
     * @return          True if the given object is an instance of the given class.
     */
    public static boolean instance(java.lang.Object object, Class klass) {
        return object != null && klass != null && klass.isInstance(object);
    }

    /**
     * Returns the nearest class which is an ancestor to all the classes in the given set.
     * @param classes A set of classes for which the nearest ancestor will be returned.
     * @return        The nearest ancestor class which is an ancestor to all the classes in the given set.
     */
    public static Class<?> getNearestAncestor(Collection<Class<?>> classes) {
        Class<?> nearest = null;

        Set<Class<?>> ancestors = getAncestors(classes);
        if (ancestors.size() > 0) nearest = ancestors.iterator().next();

        return nearest;
    }

    /**
     * Returns the nearest class which is an ancestor to all the classes in the given set.
     * @param classes A set of classes for which the nearest ancestor will be returned.
     * @return        The nearest ancestor class which is an ancestor to all the classes in the given set.
     */
    public static Class<?> getNearestAncestor(Class<?>... classes) {
        return getNearestAncestor(java.util.Arrays.asList(classes));
    }

    /**
     * Returns the nearest class which is an ancestor to all the classes in the given set.
     * @param classNames A set of class names for which the nearest ancestor will be returned.
     * @return           The nearest ancestor class which is an ancestor to all the classes in the given set.
     * @throws BaseException If any of the class names cannot be found.
     */
    public static Class<?> getNearestAncestor(java.lang.String... classNames) throws BaseException {
        return getNearestAncestor(toClassArray(classNames));
    }

    /**
     * Returns all the common ancestor classes from the given set of classes.
     * @param classes    A collection of classes.
     * @return           All the common ancestor classes from the given set of class names.
     */
    public static Set<Class<?>> getAncestors(Collection<Class<?>> classes) {
        Set<Class<?>> intersection = new LinkedHashSet<Class<?>>();

        for (Class<?> klass : classes) {
            if (intersection.size() == 0) {
                intersection.addAll(getAncestors(klass));
            } else {
                intersection.retainAll(getAncestors(klass));
            }
        }

        return intersection;
    }

    /**
     * Returns all the common ancestor classes from the given set of classes.
     * @param classes    A list of classes.
     * @return           All the common ancestor classes from the given set of class names.
     */
    public static Class<?>[] getAncestors(Class<?>... classes) {
        return getAncestors(java.util.Arrays.asList(classes)).toArray(new Class<?>[0]);
    }

    /**
     * Returns all the common ancestor classes from the given set of class names.
     * @param classNames A list of class names.
     * @return           All the common ancestor classes from the given set of class names.
     * @throws BaseException If a class name cannot be found.
     */
    public static Class<?>[] getAncestors(String... classNames) throws BaseException {
        return getAncestors(toClassArray(classNames));
    }

    /**
     * Returns all the ancestor classes from nearest to furthest for the given class.
     * @param klass A class to fetch the ancestors of.
     * @return      All the ancestor classes from nearest to furthest for the given class.
     */
    public static Set<Class<?>> getAncestors(Class<?> klass) {
        Set<Class<?>> ancestors = new LinkedHashSet<Class<?>>();
        Set<Class<?>> parents = new LinkedHashSet<Class<?>>();

        parents.add(klass);

        do {
            ancestors.addAll(parents);

            Set<Class<?>> children = new LinkedHashSet<Class<?>>(parents);
            parents.clear();

            for (Class<?> child : children) {
                Class<?> parent = child.getSuperclass();
                if (parent != null) parents.add(parent);
                for (Class<?> parentInterface : child.getInterfaces()) {
                    parents.add(parentInterface);
                }
            }
        } while (!parents.isEmpty());

        return ancestors;
    }

    /**
     * Converts the given list of class names to a list of classes.
     * @param classNames A list of class names.
     * @return           A list of classes that correspond to the given names.
     * @throws BaseException If a class name cannot be not found.
     */
    private static Class<?>[] toClassArray(String[] classNames) throws BaseException {
        if (classNames == null) return null;

        Class<?>[] classes = new Class<?>[classNames.length];

        for (int i = 0; i < classes.length; i++) {
            try {
                classes[i] = Class.forName(classNames[i]);
            } catch (ClassNotFoundException ex) {
                throw new BaseException(ex);
            }
        }

        return classes;
    }
}
