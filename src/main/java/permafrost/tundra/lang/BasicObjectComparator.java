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

/**
 * Compares two Objects.
 */
public class BasicObjectComparator implements ObjectComparator {
    /**
     * The singleton instance of this class.
     */
    private static final BasicObjectComparator INSTANCE = new BasicObjectComparator();

    /**
     * Disallow instantiation of this class.
     */
    private BasicObjectComparator() {}

    /**
     * Returns the singleton instance of this class.
     * @return The singleton instance of this class.
     */
    public static BasicObjectComparator getInstance() {
        return INSTANCE;
    }

    /**
     * Compares two Objects.
     *
     * @param object1        The first Object to be compared.
     * @param object2        The second Object to be compared.
     * @return               A value less than zero if the first object
     *                       comes before the second object, a value of
     *                       zero if they are equal, or a value of greater
     *                       than zero if the first object comes after the
     *                       second object according to the comparison
     *                       of all the keys and values in each document.
     */
    @SuppressWarnings("unchecked")
    public int compare(Object object1, Object object2) {
        int result = 0;

        if (object1 == null || object2 == null) {
            if (object1 != null) {
                result = 1;
            } else if (object2 != null){
                result = -1;
            }
        } else {
            if (object1 instanceof Comparable && object1.getClass().isAssignableFrom(object2.getClass())) {
                result = ((Comparable) object1).compareTo(object2);
            } else if (object2 instanceof Comparable && object2.getClass().isAssignableFrom(object1.getClass())) {
                int comparison = ((Comparable) object2).compareTo(object1);
                result = comparison < 0 ? 1 : comparison > 0 ? -1 : 0;
            } else if (object1 != object2) {
                // last ditch effort: compare two incomparable objects using their hash codes
                result = Integer.valueOf(object1.hashCode()).compareTo(object2.hashCode());
            }
        }

        return result;
    }
}
