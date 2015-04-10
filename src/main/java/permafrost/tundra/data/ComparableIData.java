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

package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.util.coder.IDataCodable;

/**
 * Wraps an IData document in an implementation of the Comparable interface,
 * which then allows the use of a standard Java for each loop for iterating
 * over the elements in the document.
 */
public class ComparableIData implements Comparable<IData>, IDataCodable {
    /**
     * The default comparator used when no other comparator or comparison criteria is specified.
     */
    public static final IDataComparator DEFAULT_COMPARATOR = IDataDefaultComparator.INSTANCE;

    protected IData document;
    protected IDataComparator comparator;

    /**
     * Constructs a new ComparableIData using the default comparator.
     * @param document The IData document to be wrapped by this ComparableIData.
     */
    public ComparableIData(IData document) {
        this(document, DEFAULT_COMPARATOR);
    }

    /**
     * Constructs a new ComparableIData using the given comparator.
     * @param document The IData document to be wrapped by this ComparableIData.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public ComparableIData(IData document, IDataComparator comparator) {
        setIData(document);
        setComparator(comparator);
    }

    /**
     * Constructs a new ComparableIData using the given comparison criteria.
     * @param document The IData document to be wrapped by this ComparableIData.
     * @param criteria One or more comparison criteria to be used in comparisons.
     */
    public ComparableIData(IData document, IDataKeyComparisonCriterion ...criteria) {
        this(document, new IDataKeyCriteriaComparator(criteria));
    }

    /**
     * Compares this object with the specified object for order.
     * @param other The object to be compared with this object.
     * @return      A negative integer, zero, or a positive integer as this object is
     *              less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(IData other) {
        return comparator.compare(document, other);
    }

    /**
     * Returns the IDataComparator used to compare IData objects.
     * @return The IDataComparator used to compare IData objects.
     */
    public IDataComparator getComparator() {
        return comparator;
    }

    /**
     * Sets the IDataComparator to be used when comparing IData objects.
     * @param comparator The IDataComparator to be used when comparing IData objects.
     */
    public void setComparator(IDataComparator comparator) {
        if (comparator == null) throw new IllegalArgumentException("comparator must not be null");
        this.comparator = comparator;
    }

    /**
     * Returns the IData document wrapped by this ComparableIData.
     * @return The IData document wrapped by this ComparableIData.
     */
    @Override
    public IData getIData() {
        return document;
    }

    /**
     * Sets the IData document to be wrapped by this ComparableIData.
     */
    @Override
    public void setIData(IData document) {
        if (document == null) throw new IllegalArgumentException("document must not be null");
        this.document = document;
    }
}
