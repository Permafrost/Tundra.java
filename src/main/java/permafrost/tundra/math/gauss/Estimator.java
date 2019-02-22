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

package permafrost.tundra.math.gauss;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.util.coder.IDataCodable;
import java.text.NumberFormat;

/**
 * Class for incrementally calculating the mean and standard deviation in a single thread.
 */
public class Estimator {
    /**
     * Class for incrementally calculating the mean and standard deviation in a single thread.
     */
    public class Results implements IDataCodable {
        /**
         * The total number of samples in this distribution.
         */
        protected long count;
        /**
         * The calculated mean, variance, minimum, maximum, and cumulative samples.
         */
        protected double mean, sq, minimum, maximum, cumulative;
        /**
         * The sampling subject, and the unit of measure for the samples.
         */
        protected String subject, unit;

        /**
         * Constructs a new Results object.
         */
        public Results(String subject, String unit, long count, double mean, double sq, double minimum, double maximum, double cumulative) {
            this.subject = subject;
            this.unit = unit;
            this.count = count;
            this.mean = mean;
            this.sq = sq;
            this.minimum = minimum;
            this.maximum = maximum;
            this.cumulative = cumulative;
        }

        /**
         * Returns the number of samples seen by this estimator.
         * @return the number of samples seen by this estimator.
         */
        public long getCount() {
            return count;
        }

        /**
         * Returns the mean of the samples.
         * @return the mean of the samples.
         */
        public double getMean() {
            return mean;
        }

        /**
         * Returns the minimum of the samples.
         * @return the minimum of the samples.
         */
        public double getMinimum() {
            return minimum;
        }

        /**
         * Returns the maximum of the samples.
         * @return the maximum of the samples.
         */
        public double getMaximum() {
            return maximum;
        }

        /**
         * Returns the cumulative total of all samples seen by this estimator.
         * @return the cumulative total of all samples seen by this estimator.
         */
        public double getCumulative() {
            return cumulative;
        }

        /**
         * Returns the variance estimate for the current set of samples.
         * @return the variance estimate.
         */
        public double getVariance() {
            return count > 1 ? sq / (count - 1) : 0.0;
        }

        /**
         * Returns the standard deviation estimate for the current set of samples.
         * @return the standard deviation estimate.
         */
        public double getStandardDeviation() {
            return Math.sqrt(getVariance());
        }

        /**
         * Returns the unit of measure related to the measured samples.
         * @return the unit of measure.
         */
        public String getUnit() {
            return unit;
        }

        /**
         * Returns the sampling subject.
         * @return the sampling subject.
         */
        public String getSubject() {
            return subject;
        }

        /**
         * Returns a string-based representation of the mean, standard deviation and number of samples for this estimator.
         * @return a string-based representation of this estimator.
         */
        @Override
        public String toString() {
            String output;

            String subject = getSubject();
            String unit = getUnit();
            double mean = getMean();
            double standardDeviation = getStandardDeviation();
            double minimum = getMinimum();
            double maximum = getMaximum();
            double cumulative = getCumulative();
            long count = getCount();

            if (unit == null || unit.equals("")) {
                output = String.format("subject = %s, average = %.9f, standard deviation = %.9f, minimum = %.9f, maximum = %.9f, cumulative = %.9f, count = %d", subject, mean, standardDeviation, minimum, maximum, cumulative, count);
            } else {
                output = String.format("subject = %s, average = %.9f %s, standard deviation = %.9f %s, minimum = %.9f %s, maximum = %.9f %s, cumulative = %.9f %s, count = %d", subject, mean, unit, standardDeviation, unit, minimum, unit, maximum, unit, cumulative, unit, count);
            }

            return output;
        }

        /**
         * Returns an IData representation of this object.
         * @return an IData representation of this object.
         */
        @Override
        public IData getIData() {
            NumberFormat decimalFormat = NumberFormat.getInstance();
            decimalFormat.setMinimumFractionDigits(9);
            decimalFormat.setMaximumFractionDigits(9);
            NumberFormat integerFormat = NumberFormat.getIntegerInstance();

            IData output = IDataFactory.create();
            IDataCursor cursor = output.getCursor();

            String subject = getSubject();
            String unit = getUnit();
            double mean = getMean();
            double standardDeviation = getStandardDeviation();
            double minimum = getMinimum();
            double maximum = getMaximum();
            double cumulative = getCumulative();
            long count = getCount();

            if (subject != null) cursor.insertAfter("subject", subject);
            if (unit != null) cursor.insertAfter("unit", unit);
            cursor.insertAfter("minimum", minimum);
            cursor.insertAfter("minimum.formatted", decimalFormat.format(minimum));
            cursor.insertAfter("average", mean);
            cursor.insertAfter("average.formatted", decimalFormat.format(mean));
            cursor.insertAfter("deviation.standard", standardDeviation);
            cursor.insertAfter("deviation.standard.formatted", decimalFormat.format(standardDeviation));
            cursor.insertAfter("maximum", maximum);
            cursor.insertAfter("maximum.formatted", decimalFormat.format(maximum));
            cursor.insertAfter("cumulative", cumulative);
            cursor.insertAfter("cumulative.formatted", decimalFormat.format(cumulative));
            cursor.insertAfter("count", count);
            cursor.insertAfter("count.formatted", integerFormat.format(count));

            return output;
        }

        /**
         * Sets values from the given IData. This method has not been implemented.
         *
         * @param input                             Not used.
         * @throws UnsupportedOperationException    This exception is always thrown.
         */
        @Override
        public void setIData(IData input) {
            throw new UnsupportedOperationException("setIData not implemented");
        }
    }

    /**
     * The total number of samples in this distribution.
     */
    protected volatile long count;
    /**
     * The calculated mean, variance, minimum, maximum, and cumulative samples.
     */
    protected volatile double mean, sq, minimum, maximum, cumulative;
    /**
     * The sampling subject, and the unit of measure for the samples.
     */
    protected String subject, unit;

    /**
     * Constructs a new estimator object.
     */
    public Estimator() {
        this(null, null);
    }

    /**
     * Constructs a new estimator object.
     *
     * @param subject The sampling subject
     * @param unit    The unit of measure for the samples.
     */
    public Estimator(String subject, String unit) {
        this.subject = subject;
        this.unit = unit;
    }

    /**
     * Adds a new sample to the set of samples used for estimating the standard deviation.
     *
     * @param sample The sample to be added.
     */
    public void add(double sample) {
        count++;
        cumulative = cumulative + sample;
        if (count == 1) {
            mean = minimum = maximum = sample;
            sq = 0.0;
        } else {
            double previousMean = mean;
            mean = previousMean + ((sample - previousMean) / count);
            sq = sq + (sample - previousMean) * (sample - mean);

            if (minimum > sample) minimum = sample;
            if (maximum < sample) maximum = sample;
        }
    }

    /**
     * Adds one or more samples to the set of samples used for estimating the standard deviation.
     *
     * @param samples One or more samples to be added.
     */
    public void add(double... samples) {
        for (double sample : samples) {
            add(sample);
        }
    }

    /**
     * Adds one or more samples to the set of samples used for estimating the standard deviation.
     *
     * @param samples One or more samples to be added.
     */
    public void add(Iterable<Double> samples) {
        for (Double sample : samples) {
            if (sample != null) add(sample);
        }
    }

    /**
     * Returns the number of samples seen by this estimator.
     * @return the number of samples seen by this estimator.
     */
    public Results getResults() {
        return new Results(subject, unit, count, mean, sq, minimum, maximum, cumulative);
    }

    /**
     * Resets the estimator back to a set of zero samples.
     */
    public void reset() {
        count = 0;
        mean = 0.0;
        sq = 0.0;
        minimum = 0.0;
        maximum = 0.0;
        cumulative = 0.0;
    }
}
