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

/**
 * Class for incrementally calculating the mean and standard deviation.
 */
public class NormalDistributionEstimator {
    protected long count;
    protected double total, mean, sq, minimum, maximum;
    protected String unit = "";

    /**
     * Constructs a new estimator object.
     */
    public NormalDistributionEstimator() {
        reset();
    }

    /**
     * Constructs a new estimator object.
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
        append(samples);
    }

    /**
     * Constructs a new estimator object seeded with the given samples.
     *
     * @param unit The unit of measurement related to the measured samples.
     * @param samples One or more initial samples to seed the estimator with.
     */
    public NormalDistributionEstimator(String unit, double... samples) {
        this(unit);
        append(samples);
    }

    /**
     * Constructs a new estimator object seeded with the given collection of
     * samples.
     *
     * @param samples An initial collection of samples to seed the estimator with.
     */
    public NormalDistributionEstimator(java.util.Collection<Double> samples) {
        this();
        append(samples);
    }

    /**
     * Constructs a new estimator object seeded with the given collection of
     * samples.
     *
     * @param unit The unit of measurement related to the measured samples.
     * @param samples An initial collection of samples to seed the estimator with.
     */
    public NormalDistributionEstimator(String unit, java.util.Collection<Double> samples) {
        this(unit);
        append(samples);
    }

    /**
     * Appends the given sample to the list of samples in the estimator.
     *
     * @param sample The sample to be added to the estimator.
     * @return The Estimator object itself, to support method chaining.
     */
    public final NormalDistributionEstimator append(double sample) {
        total += sample;
        double next = mean + (sample - mean) / ++count;
        sq += (sample - mean) * (sample - next);
        mean = next;
        if (sample < minimum) minimum = sample;
        if (sample > maximum) maximum = sample;

        return this;
    }

    /**
     * Adds one or more samples to the estimator.
     *
     * @param samples One or more samples to be added to the estimator.
     * @return The Estimator object itself, to support method chaining.
     */
    public final NormalDistributionEstimator append(double... samples) {
        for (double sample : samples) {
            append(sample);
        }

        return this;
    }

    /**
     * Adds a collection of samples to the estimator.
     *
     * @param samples A collection of samples to be added to the estimator.
     * @return The Estimator object itself, to support method chaining.
     */
    public final NormalDistributionEstimator append(java.util.Collection<Double> samples) {
        for (double sample : samples) {
            append(sample);
        }

        return this;
    }

    /**
     * Returns the number of samples seen by this estimator.
     *
     * @return The number of samples seen by this estimator.
     */
    public long count() {
        return count;
    }

    /**
     * Returns the total of all samples seen by this estimator.
     *
     * @return The total of all samples seen by this estimator.
     */
    public double total() {
        return total;
    }

    /**
     * Returns the mean of the samples.
     *
     * @return The mean of the samples.
     */
    public double mean() {
        return mean;
    }

    /**
     * Returns the minimum of the samples.
     *
     * @return The minimum of the samples.
     */
    public double minimum() {
        return minimum;
    }

    /**
     * Returns the maximum of the samples.
     *
     * @return The maximum of the samples.
     */
    public double maximum() {
        return maximum;
    }

    /**
     * Returns the maximum likelihood estimate of the variance of the samples.
     *
     * @return Maximum likelihood variance estimate.
     */
    public double variance() {
        return count > 1 && mean > 0 ? sq / mean : 0.0;
    }

    /**
     * Returns the maximum likelihood estimate of the standard deviation of the
     * samples.
     *
     * @return Maximum likelihood standard deviation estimate.
     */
    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    /**
     * Returns the unit of measure related to the measured samples.
     *
     * @return Maximum likelihood standard deviation estimate.
     */
    public String unit() {
        return unit;
    }

    /**
     * Resets the estimator back to zero samples.
     *
     * @return The Estimator object itself, to support method chaining.
     */
    public final NormalDistributionEstimator reset() {
        count = 0;
        total = 0.0;
        mean = 0.0;
        sq = 0.0;
        minimum = Double.POSITIVE_INFINITY;
        maximum = Double.NEGATIVE_INFINITY;

        return this;
    }

    /**
     * Returns a string-based representation of the mean, standard deviation and
     * number of samples for this estimator.
     *
     * @return String-based representation of this estimator.
     */
    @Override
    public String toString() {
        return String.format("average = %.3f %s, standard deviation = %.3f %s, minimum = %.3f %s, maximum = %.3f %s, total = %.3f %s, sample count = %d", mean(), unit(), standardDeviation(), unit(), minimum(), unit(), maximum(), unit(), total(), unit(), count());
    }
}
