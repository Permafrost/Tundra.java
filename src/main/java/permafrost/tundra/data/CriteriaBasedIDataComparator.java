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

import permafrost.tundra.math.DecimalHelper;
import permafrost.tundra.math.IntegerHelper;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;

/**
 * Compares two IData objects using the values associated with the given
 * list of keys in precedence order.
 */
public class CriteriaBasedIDataComparator implements IDataComparator {
    protected java.util.List<IDataComparisonCriterion> criteria;

    /**
     * Construct a new IDataComparator with one or more comparison criteria.
     *
     * @param criteria The comparison criteria to be used when comparing IData objects.
     */
    public CriteriaBasedIDataComparator(IDataComparisonCriterion... criteria) {
        this(java.util.Arrays.asList(criteria));
    }

    /**
     * Construct a new IDataComparator with one or more comparison criteria.
     *
     * @param criteria The comparison criteria to be used when comparing IData objects.
     */
    public CriteriaBasedIDataComparator(java.util.List<IDataComparisonCriterion> criteria) {
        if (criteria == null || criteria.size() == 0) throw new IllegalArgumentException("At least one comparison criteria is required to construct an IDataComparator object");
        this.criteria = criteria;
    }

    /**
     * Normalizes the given comparison result for when the comparison should
     * be in descending order.
     *
     * @param result     A comparison result.
     * @param descending Whether the comparison should be in descending order.
     * @return           If descending is true, returns the given comparison
     *                   result negated, otherwise returns the result unchanged.
     */
    protected static int normalize(int result, boolean descending) {
        if (descending) {
            if (result < 0) {
                result = 1;
            } else if (result > 0) {
                result = -1;
            }
        }
        return result;
    }

    /**
     * Compares two IData documents.
     *
     * @param firstDocument  The first IData document to be compared.
     * @param secondDocument The second IData document to be compared.
     * @return               A value less than zero if the first document
     *                       comes before the second document, a value of
     *                       zero if they are equal, or a value of greater
     *                       than zero if the first document comes after the
     *                       second document according to the comparison
     *                       criteria the IDataComparator was constructed with.
     */
    @SuppressWarnings("unchecked")
    public int compare(com.wm.data.IData firstDocument, com.wm.data.IData secondDocument) {
        int result = 0;

        for (IDataComparisonCriterion criterion : criteria) {
            Object firstValue = IDataHelper.get(firstDocument, criterion.getKey());
            Object secondValue = IDataHelper.get(secondDocument, criterion.getKey());

            if (firstValue == null) {
                if (secondValue != null) {
                    result = normalize(-1, criterion.isDescending());
                    break;
                }
            } else if (secondValue == null) {
                result = normalize(1, criterion.isDescending());
                break;
            } else {
                switch(criterion.getType()) {
                    case INTEGER:
                        firstValue = IntegerHelper.parse(firstValue.toString());
                        secondValue = IntegerHelper.parse(secondValue.toString());
                        break;
                    case DECIMAL:
                        firstValue = DecimalHelper.parse(firstValue.toString());
                        secondValue = DecimalHelper.parse(secondValue.toString());
                        break;
                    case DATETIME:
                        firstValue = DateTimeHelper.parse(firstValue.toString(), criterion.getPattern());
                        secondValue = DateTimeHelper.parse(secondValue.toString(), criterion.getPattern());
                        break;
                    case DURATION:
                        firstValue = IntegerHelper.parse(DurationHelper.format(firstValue.toString(), criterion.getPattern(), "milliseconds"));
                        secondValue = IntegerHelper.parse(DurationHelper.format(secondValue.toString(), criterion.getPattern(), "milliseconds"));
                        break;
                    case STRING:
                        firstValue = firstValue.toString();
                        secondValue = secondValue.toString();
                        break;
                }
                if (firstValue instanceof Comparable && secondValue instanceof Comparable) {
                    result = normalize(((Comparable)firstValue).compareTo(secondValue), criterion.isDescending());
                    if (result != 0) break;
                }
            }
        }
        return result;
    }
}
