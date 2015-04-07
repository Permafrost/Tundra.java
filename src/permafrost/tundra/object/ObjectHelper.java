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

public class ObjectHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ObjectHelper() {}

    /**
     * Returns true if the given objects are considered equal.
     *
     * @param firstObject
     * @param secondObject
     * @return
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
     * @param object
     * @param className
     * @return
     * @throws BaseException
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
     * @param object
     * @param klass
     * @return
     */
    public static boolean instance(java.lang.Object object, Class klass) {
        return object != null && klass != null && klass.isInstance(object);
    }

    // returns the nearest ancestor class for the given set of classes
    public static Class<?> nearestAncestor(java.util.Collection<Class<?>> classes) {
        Class<?> nearest = null;

        java.util.Set<Class<?>> ancestors = ancestors(classes);
        if (ancestors.size() > 0) nearest = ancestors.iterator().next();

        return nearest;
    }

    // returns the nearest ancestor class for the given set of classes
    public static Class<?> nearestAncestor(Class<?>... classes) {
        return nearestAncestor(java.util.Arrays.asList(classes));
    }

    // returns the nearest ancestor class for the given set of class names
    public static Class<?> nearestAncestor(java.lang.String... classNames) throws ClassNotFoundException {
        return nearestAncestor(toClassArray(classNames));
    }

    // returns all the common ancestor classes for the given set of classes
    public static java.util.Set<Class<?>> ancestors(java.util.Collection<Class<?>> classes) {
        java.util.Set<Class<?>> intersection = new java.util.LinkedHashSet<Class<?>>();

        for (Class<?> klass : classes) {
            if (intersection.size() == 0) {
                intersection.addAll(ancestors(klass));
            } else {
                intersection.retainAll(ancestors(klass));
            }
        }

        return intersection;
    }

    // returns all the common ancestor classes for the given set of classes
    public static Class<?>[] ancestors(Class<?>... classes) {
        return ancestors(java.util.Arrays.asList(classes)).toArray(new Class<?>[0]);
    }

    // returns all the common ancestor classes for the given set of class names
    public static Class<?>[] ancestors(String... classNames) throws ClassNotFoundException {
        return ancestors(toClassArray(classNames));
    }

    // converts a list of class names to a list of class objects
    protected static Class<?>[] toClassArray(String[] classNames) throws ClassNotFoundException {
        if (classNames == null) return null;

        Class<?>[] classes = new Class<?>[classNames.length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = Class.forName(classNames[i]);
        }
        return classes;
    }

    // returns all ancestors from nearest to furthest for the given class
    protected static java.util.Set<Class<?>> ancestors(Class<?> klass) {
        java.util.Set<Class<?>> ancestors = new java.util.LinkedHashSet<Class<?>>();
        java.util.Set<Class<?>> parents = new java.util.LinkedHashSet<Class<?>>();

        parents.add(klass);

        do {
            ancestors.addAll(parents);

            java.util.Set<Class<?>> children = new java.util.LinkedHashSet<Class<?>>(parents);
            parents.clear();

            for (Class<?> child : children) {
                Class<?> parent = child.getSuperclass();
                if (parent != null) parents.add(parent);
                for (Class<?> parentInterface : child.getInterfaces()) parents.add(parentInterface);
            }

        } while (!parents.isEmpty());

        return ancestors;
    }
}
