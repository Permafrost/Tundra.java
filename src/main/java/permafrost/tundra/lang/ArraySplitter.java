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

package permafrost.tundra.lang;

import java.util.Arrays;

/**
 * Encapsulates splitting a given array into a head and tail.
 *
 * @param <T> The component type of the array.
 */
public class ArraySplitter<T> {
    /**
     * The array to take the head and tail from.
     */
    T[] array;
    /**
     * The number of items to take as the head of the array.
     */
    int count;

    /**
     * Constructs a new ArrayHeadTail object.
     *
     * @param array The array to take the head and tail from.
     * @param count The number of items to take as the head of the array.
     */
    public ArraySplitter(T[] array, int count) {
        setArray(array);
        setCount(count);
    }

    /**
     * Returns the array that the head and tail are taken from.
     *
     * @return The array that the head and tail are taken from.
     */
    public T[] getArray() {
        return array;
    }

    /**
     * Sets the array to take the head and tail from.
     *
     * @param array The array to take the head and tail from.
     */
    public void setArray(T[] array) {
        if (array == null) throw new NullPointerException("array must not be null");
        this.array = array;
    }

    /**
     * Returns the number of items taken as the head of the array.
     *
     * @return The number of items taken as the head of the array.
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the number of items to take as the head of the array.
     *
     * @param count The number of items to take as the head of the array.
     */
    public void setCount(int count) {
        if (count < 0) throw new IllegalArgumentException("count must be greater than zero");
        this.count = count;
        if (count > array.length) this.count = array.length;
    }

    /**
     * Returns the head of the array.
     *
     * @return The head of the array.
     */
    public T[] getHead() {
        return Arrays.copyOfRange(array, 0, count);
    }

    /**
     * Returns the tail of the array.
     *
     * @return The tail of the array.
     */
    public T[] getTail() {
        T[] tail;

        if (count >= array.length) {
            tail = Arrays.copyOf(array, 0);
        } else {
            tail = Arrays.copyOfRange(array, count, array.length);
        }

        return tail;
    }
}