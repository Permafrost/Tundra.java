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

package permafrost.tundra.math;

import java.util.Collection;

/**
 * Class for incrementally calculating the mean and standard deviation.
 */
public class NormalDistributionEstimator {
    protected long count;
    protected double mean, sq, minimum, maximum;
    protected String unit = "";

    /**
     * Constructs a new estimator object.
     */
    public NormalDistributionEstimator() {
        reset();
    }

    /**
     * Constructs a new estimator object.
     *
     * @param unit The unit of measurement related to the measured samples.
     */
    public NormalDistributionEstimator(String unit) {
        this();
        this.unit = unit;
    }

    /**
     * Constructs a new estimator object seeded with the given samples.
     *
     * @param samples One or more initial samples to seed the estimator with.
     */
    public NormalDistributionEstimator(double... samples) {
        this();
        add(samples);
    }

    /**
     * Constructs a new estimator object seeded with the given samples.
     *
     * @param unit    The unit of measurement related to the measured samples.
     * @param samples One or more initial samples to seed the estimator with.
     */
    public NormalDistributionEstimator(String unit, double... samples) {
        this(unit);
        add(samples);
    }

    /**
     * Constructs a new estimator object seeded with the given collection of samples.
     *
     * @param samples An initial collection of samples to seed the estimator with.
     */
    public NormalDistributionEstimator(Collection<Double> samples) {
        this();
        add(samples);
    }

    /**
     * Constructs a new estimator object seeded with the given collection of samples.
     *
     * @param unit    The unit of measurement related to the measured samples.
     * @param samples An initial collection of samples to seed the estimator with.
     */
    public NormalDistributionEstimator(String unit, Collection<Double> samples) {
        this(unit);
        add(samples);
    }

    /**
     * Adds a new sample to the set of samples used for estimating the standard deviation.
     *
     * @param sample The sample to be added.
     * @return The estimator object itself, to support method chaining.
     */
    public synchronized NormalDistributionEstimator add(double sample) {
        count++;
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
        return this;
    }

    /**
     * Adds one or more samples to the set of samples used for estimating the standard deviation.
     *
     * @param samples One or more samples to be added.
     * @return The estimator object itself, to support method chaining.
     */
    public NormalDistributionEstimator add(double... samples) {
        for (double sample : samples) {
            add(sample);
        }
        return this;
    }

    /**
     * Adds a collection of samples to the set of samples used for estimating the standard deviation.
     *
     * @param samples A collection of samples to be added.
     * @return The estimator object itself, to support method chaining.
     */
    public NormalDistributionEstimator add(Collection<Double> samples) {
        for (double sample : samples) {
            add(sample);
        }
        return this;
    }

    /**
     * Resets the estimator back to a set of zero samples.
     *
     * @return The Estimator object itself, to support method chaining.
     */
    public synchronized NormalDistributionEstimator reset() {
        count = 0;
        mean = 0.0;
        sq = 0.0;
        minimum = 0.0;
        maximum = 0.0;
        return this;
    }

    /**
     * Ensure all calculations are up to date.
     */
    protected void quiesce() {}

    /**
     * Returns the number of samples seen by this estimator.
     *
     * @return The number of samples seen by this estimator.
     */
    public synchronized long getCount() {
        quiesce();
        return count;
    }

    /**
     * Returns the mean of the samples.
     *
     * @return The mean of the samples.
     */
    public synchronized double getMean() {
        quiesce();
        return mean;
    }

    /**
     * Returns the minimum of the samples.
     *
     * @return The minimum of the samples.
     */
    public synchronized double getMinimum() {
        quiesce();

        if (count == 0) throw new IllegalStateException("minimum value is not available when sample count is zero");
        return minimum;
    }

    /**
     * Returns the maximum of the samples.
     *
     * @return The maximum of the samples.
     */
    public synchronized double getMaximum() {
        quiesce();

        if (count == 0) throw new IllegalStateException("maximum value is not available when sample count is zero");
        return maximum;
    }

    /**
     * Returns the variance estimate for the current set of samples.
     *
     * @return The variance estimate.
     */
    public synchronized double getVariance() {
        quiesce();
        return count > 1 ? sq / (count - 1) : 0.0;
    }

    /**
     * Returns the standard deviation estimate for the current set of samples.
     *
     * @return The standard deviation estimate.
     */
    public double getStandardDeviation() {
        quiesce();
        return Math.sqrt(getVariance());
    }

    /**
     * Returns the unit of measure related to the measured samples.
     *
     * @return The unit of measure.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns a string-based representation of the mean, standard deviation and number of samples for this estimator.
     *
     * @return String-based representation of this estimator.
     */
    @Override
    public String toString() {
        double mean, stdev, min, max;
        long count;
        String unit = getUnit();

        // thread synchronized to make sure all values are consistent
        synchronized (this) {
            mean = getMean();
            stdev = getStandardDeviation();
            min = getMinimum();
            max = getMaximum();
            count = getCount();
        }

        String output;

        if (unit == null || unit.equals("")) {
            output = String.format("average = %.3f, standard deviation = %.3f, minimum = %.3f, maximum = %.3f, sample count = %d", mean, stdev, min, max, count);
        } else {
            output = String.format("average = %.3f %s, standard deviation = %.3f %s, minimum = %.3f %s, maximum = %.3f %s, sample count = %d", mean, unit, stdev, unit, min, unit, max, unit, count);
        }

        return output;
    }
}
