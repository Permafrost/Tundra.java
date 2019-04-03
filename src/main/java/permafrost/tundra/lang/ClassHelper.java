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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ClassHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ClassHelper() {}

    /**
     * Converts the given object to a Class.
     *
     * @param object    The object to be converted.
     * @return          The converted object.
     */
    public static Class normalize(Object object) {
        Class value = null;

        if (object instanceof Class) {
            value = (Class)object;
        } else if (object instanceof String) {
            try {
                value = Class.forName((String)object);
            } catch(ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        return value;
    }

    /**
     * Returns an array of Class objects associated with the class or interface with the given names.
     *
     * @param classNames                A list of class or interface names.
     * @return                          A list of Class objects associated with the given names.
     * @throws ClassNotFoundException   If a class with the given name cannot be found.
     */
    public static Class[] forName(String[] classNames) throws ClassNotFoundException {
        if (classNames == null) return null;

        Class[] classes = new Class[classNames.length];

        for (int i = 0; i < classNames.length; i++) {
            if (classNames[i] != null) {
                classes[i] = Class.forName(classNames[i]);
            }
        }

        return classes;
    }

    /**
     * Returns the one dimensional array class associated with the given component class.
     *
     * @param componentClass    The component class to use for the array class.
     * @param <T>               The component class of the array.
     * @return                  The one dimensional array class associated with the given component class.
     */
    public static <T> Class<?> getArrayClass(Class<T> componentClass) {
        return getArrayClass(componentClass, 1);
    }

    /**
     * Returns the array class associated with the given component class and number of dimensions.
     *
     * @param componentClass    The component class to use for the array class.
     * @param dimensions        The array dimensions to use.
     * @param <T>               The component class of the array.
     * @return                  The array class associated with the given component class and number of dimensions.
     */
    public static <T> Class<?> getArrayClass(Class<T> componentClass, int dimensions) {
        if (dimensions < 1) throw new IllegalArgumentException("array dimensions must be >= 1");

        int[] dimensionArray = new int[dimensions];
        Arrays.fill(dimensionArray, 0);
        return Array.newInstance(componentClass, dimensionArray).getClass();
    }

    /**
     * Converts the given list of class names to a list of classes.
     *
     * @param classNames                A list of class names.
     * @return                          A list of classes that correspond to the given names.
     * @throws ClassNotFoundException   If a class name cannot be not found.
     */
    static Class<?>[] toClassArray(String[] classNames) throws ClassNotFoundException {
        if (classNames == null) return null;

        Class<?>[] classes = new Class<?>[classNames.length];

        for (int i = 0; i < classes.length; i++) {
            classes[i] = Class.forName(classNames[i]);
        }

        return classes;
    }

    /**
     * Returns all the ancestor classes from nearest to furthest for the given class.
     *
     * @param klass     A class to fetch the ancestors of.
     * @return          All the ancestor classes from nearest to furthest for the given class.
     */
    static Set<Class<?>> getAncestors(Class<?> klass) {
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
                Collections.addAll(parents, child.getInterfaces());
            }
        } while (!parents.isEmpty());

        return ancestors;
    }

    /**
     * Returns all the common ancestor classes from the given set of class names.
     *
     * @param classNames                A list of class names.
     * @return                          All the common ancestor classes from the given set of class names.
     * @throws ClassNotFoundException   If a class name cannot be found.
     */
    public static Class<?>[] getAncestors(String... classNames) throws ClassNotFoundException {
        return getAncestors(toClassArray(classNames));
    }

    /**
     * Returns all the common ancestor classes from the given set of classes.
     *
     * @param classes   A list of classes.
     * @return          All the common ancestor classes from the given set of class names.
     */
    public static Class<?>[] getAncestors(Class<?>... classes) {
        Set<Class<?>> ancestors = getAncestors(Arrays.asList(classes));
        return ancestors.toArray(new Class<?>[0]);
    }

    /**
     * Returns all the common ancestor classes from the given set of classes.
     *
     * @param classes   A collection of classes.
     * @return          All the common ancestor classes from the given set of class names.
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
     * Returns the nearest class which is an ancestor to all the classes in the given set.
     *
     * @param classNames                A set of class names for which the nearest ancestor will be returned.
     * @return                          The nearest ancestor class which is an ancestor to all the classes in the given
     *                                  set.
     * @throws ClassNotFoundException   If any of the class names cannot be found.
     */
    public static Class<?> getNearestAncestor(String... classNames) throws ClassNotFoundException {
        return getNearestAncestor(toClassArray(classNames));
    }

    /**
     * Returns the nearest class which is an ancestor to all the classes in the given set.
     *
     * @param classes   A set of classes for which the nearest ancestor will be returned.
     * @return          The nearest ancestor class which is an ancestor to all the classes in the given set.
     */
    public static Class<?> getNearestAncestor(Class<?>... classes) {
        return ObjectHelper.getNearestAncestor(Arrays.asList(classes));
    }

    /**
     * Returns the nearest class which is an ancestor to all the classes in the given set.
     *
     * @param classes   A set of classes for which the nearest ancestor will be returned.
     * @return          The nearest ancestor class which is an ancestor to all the classes in the given set.
     */
    public static Class<?> getNearestAncestor(Set<Class<?>> classes) {
        Class<?> nearest = null;

        Set<Class<?>> ancestors = getAncestors(classes);

        if (ancestors.size() > 0) {
            nearest = ancestors.iterator().next();
        }

        if (nearest == null) nearest = Object.class;

        return nearest;
    }
}
