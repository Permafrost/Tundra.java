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
import permafrost.tundra.collection.ListHelper;
import permafrost.tundra.math.BigDecimalHelper;
import permafrost.tundra.math.BigIntegerHelper;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Compares two IData objects using the values associated with the given list of keys in precedence order.
 */
public class CriteriaBasedIDataComparator implements IDataComparator {
    /**
     * The criteria used for a comparison.
     */
    protected List<IDataComparisonCriterion> criteria;

    /**
     * Construct a new IDataComparator with one or more comparison criteria.
     *
     * @param criteria      The comparison criteria to be used when comparing IData objects.
     */
    public CriteriaBasedIDataComparator(IDataComparisonCriterion... criteria) {
        this(ListHelper.of(true, criteria));
    }

    /**
     * Construct a new IDataComparator with one or more comparison criteria.
     *
     * @param criteria      The comparison criteria to be used when comparing IData objects.
     */
    public CriteriaBasedIDataComparator(List<IDataComparisonCriterion> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalArgumentException("At least one comparison criteria is required to construct an CriteriaBasedIDataComparator object");
        }
        this.criteria = criteria;
    }

    /**
     * Returns the criteria used for comparisons by this comparator.
     *
     * @return              The criteria used for comparisons by this comparator.
     */
    public List<IDataComparisonCriterion> getCriteria() {
        return this.criteria;
    }

    /**
     * Normalizes the given comparison result for when the comparison should be in descending order.
     *
     * @param result        A comparison result.
     * @param descending    Whether the comparison should be in descending order.
     * @return              If descending is true, returns the given comparison result negated, otherwise returns the
     *                      result unchanged.
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
     * @param firstDocument     The first IData document to be compared.
     * @param secondDocument    The second IData document to be compared.
     * @return                  A value less than zero if the first document comes before the second document, a value
     *                          of zero if they are equal, or a value of greater than zero if the first document comes
     *                          after the second document according to the comparison criteria the IDataComparator was
     *                          constructed with.
     */
    @SuppressWarnings("unchecked")
    public int compare(IData firstDocument, IData secondDocument) {
        int result = 0;

        for (IDataComparisonCriterion criterion : criteria) {
            Object firstValue = IDataHelper.get(firstDocument, criterion.getKey());
            Object secondValue = IDataHelper.get(secondDocument, criterion.getKey());

            if (firstValue == null) {
                if (secondValue != null) {
                    result = normalize(-1, criterion.isDescending());
                }
            } else if (secondValue == null) {
                result = normalize(1, criterion.isDescending());
            } else {
                boolean firstParse = true, secondParse = true;

                switch (criterion.getType()) {
                    case INTEGER:
                        try {
                            firstValue = BigIntegerHelper.parse(firstValue.toString());
                        } catch(NumberFormatException ex) {
                            firstParse = false;
                        }
                        try {
                            secondValue = BigIntegerHelper.parse(secondValue.toString());
                        } catch(NumberFormatException ex) {
                            secondParse = false;
                        }

                        // handle failed parses
                        if (firstParse && !secondParse) {
                            secondValue = BigInteger.valueOf(Long.MAX_VALUE);
                        } else if (!firstParse && secondParse) {
                            firstValue = BigInteger.valueOf(Long.MAX_VALUE);
                        }
                        break;
                    case DECIMAL:
                        try {
                            firstValue = BigDecimalHelper.parse(firstValue.toString());
                        } catch(NumberFormatException ex) {
                            firstParse = false;
                        }
                        try {
                            secondValue = BigDecimalHelper.parse(secondValue.toString());
                        } catch(NumberFormatException ex) {
                            secondParse = false;
                        }

                        // handle failed parses
                        if (firstParse && !secondParse) {
                            secondValue = BigDecimal.valueOf(Double.MAX_VALUE);
                        } else if (!firstParse && secondParse) {
                            firstValue = BigDecimal.valueOf(Double.MAX_VALUE);
                        }
                        break;
                    case DATETIME:
                        firstValue = DateTimeHelper.parse(firstValue.toString(), criterion.getPattern());
                        secondValue = DateTimeHelper.parse(secondValue.toString(), criterion.getPattern());
                        break;
                    case DURATION:
                        firstValue = BigIntegerHelper.parse(DurationHelper.format(firstValue.toString(), criterion.getPattern(), "milliseconds"));
                        secondValue = BigIntegerHelper.parse(DurationHelper.format(secondValue.toString(), criterion.getPattern(), "milliseconds"));
                        break;
                    case STRING:
                        firstValue = firstValue.toString();
                        secondValue = secondValue.toString();
                        break;
                }
                if (firstValue instanceof Comparable && secondValue instanceof Comparable) {
                    try {
                        result = normalize(((Comparable)firstValue).compareTo(secondValue), criterion.isDescending());
                    } catch (Exception ex) {
                        result = normalize(compareObjectIdentity(firstValue, secondValue), criterion.isDescending());
                    }
                } else {
                    result = normalize(compareObjectIdentity(firstValue, secondValue), criterion.isDescending());
                }
            }
            if (result != 0) break;
        }
        return result;
    }

    /**
     * Fallback comparison for incomparable objects using the Java object identity.
     *
     * @param firstValue    The first object to be compared.
     * @param secondValue   The second object to be compared.
     * @return              The result of the comparison.
     */
    private int compareObjectIdentity(Object firstValue, Object secondValue) {
        return firstValue == secondValue ? 0 : Integer.valueOf(System.identityHashCode(firstValue)).compareTo(System.identityHashCode(secondValue));
    }
}
