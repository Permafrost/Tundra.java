/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Lachlan Dowding
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
import com.wm.data.IDataUtil;

import java.text.NumberFormat;

/**
 * Provides service statistics collection and normal distribution estimation.
 */
public class ServiceEstimator extends ConcurrentEstimator {
    /**
     * Represents a single sample.
     */
    public static class Sample extends ConcurrentEstimator.Sample {
        /**
         * Whether this sample was a successful service invocation.
         */
        protected boolean success;

        /**
         * Creates a new Sample.
         *
         * @param success Whether this sample was a successful service invocation.
         * @param value   The duration of the service invocation.
         */
        public Sample(boolean success, double value) {
            super(value);
            this.success = success;
        }

        /**
         * Returns whether this sample was a successful service invocation.
         * @return whether this sample was a successful service invocation.
         */
        public boolean getSuccess() {
            return success;
        }
    }

    /**
     * Represents the results of this estimator.
     */
    public class Results extends Estimator.Results {
        /**
         * The name of the service the collected statistics relate to.
         */
        protected String service;
        /**
         * The count of successful and failed invocations.
         */
        protected long successes = 0, failures = 0;

        /**
         * Constructs a new Results object.
         *
         * @param subject    The subject or description of what was estimated.
         * @param unit       The unit of measurement related to the measured samples.
         * @param count      The number of samples measured.
         * @param successes  The number of successful invocations (those which did not throw an exception).
         * @param failures   The number of failed invocations (those which did throw an exception).
         * @param mean       The measured mean value.
         * @param sq         The measured square value.
         * @param minimum    The measured minimum value.
         * @param maximum    The measured maximum value.
         * @param cumulative The measured cumulative value.
         */
        public Results(String subject, String unit, long count, long successes, long failures, double mean, double sq, double minimum, double maximum, double cumulative) {
            super(subject, unit, count, mean, sq, minimum, maximum, cumulative);
            this.successes = successes;
            this.failures = failures;
        }

        /**
         * Returns the count of successful invocations of the related service since sampling started.
         * @return the count of successful invocations of the related service since sampling started.
         */
        public long getSuccesses() {
            return successes;
        }

        /**
         * Returns the count of failed invocations of the related service since sampling started.
         * @return the count of failed invocations of the related service since sampling started.
         */
        public long getFailures() {
            return failures;
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
            long successes = getSuccesses();
            long failures = getFailures();

            if (unit == null) {
                output = String.format("subject = %s, average = %.9f, standard deviation = %.9f, minimum = %.9f, maximum = %.9f, cumulative = %.9f, successes = %d, failures = %d, total = %d", subject, mean, standardDeviation, minimum, maximum, cumulative, successes, failures, count);
            } else {
                output = String.format("subject = %s, average = %.9f %s, standard deviation = %.9f %s, minimum = %.9f %s, maximum = %.9f %s, cumulative = %.9f %s, successes = %d, failures = %d, total = %d", subject, mean, unit, standardDeviation, unit, minimum, unit, maximum, unit, cumulative, unit, successes, failures, count);
            }

            return output;
        }

        /**
         * Returns an IData representation of this object.
         * @return An IData representation of this object.
         */
        @Override
        public IData getIData() {
            NumberFormat integerFormat = NumberFormat.getIntegerInstance();

            long count = getCount();
            long successes = getSuccesses();
            long failures = getFailures();
            String description = toString();

            IData output = super.getIData();

            IDataCursor cursor = output.getCursor();
            try {
                cursor.insertBefore("description", description);
                cursor.destroy();

                cursor = output.getCursor();
                cursor.insertAfter("count.successes", successes);
                cursor.insertAfter("count.successes.formatted", integerFormat.format(successes));
                cursor.insertAfter("count.failures", failures);
                cursor.insertAfter("count.failures.formatted", integerFormat.format(failures));
                cursor.insertAfter("count.total", count);
                cursor.insertAfter("count.total.formatted", integerFormat.format(count));

                IDataUtil.remove(cursor, "count");
                IDataUtil.remove(cursor, "count.formatted");
            } finally {
                cursor.destroy();
            }

            return output;
        }
    }

    /**
     * The count of successful and failed invocations.
     */
    protected long successes = 0, failures = 0;

    /**
     * Constructs a new estimator object.
     *
     * @param service   The name of the service the collected statistics relate to.
     * @param unit      The unit of measurement related to the measured samples.
     */
    public ServiceEstimator(String service, String unit) {
        super(service, unit);
    }

    /**
     * Resets the estimator back to a set of zero samples.
     */
    public void reset() {
        lock.writeLock().lock();
        try {
            successes = 0;
            failures = 0;
            super.reset();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Adds a new sample to the set of samples used for estimating the standard deviation.
     *
     * @param sample  The sample to be added.
     */
    public void add(Sample sample) {
        super.add(sample);
    }

    /**
     * Ensure all calculations are up to date, must be called once finished collecting samples.
     */
    @Override
    protected void quiesce() {
        int size =  samples.size(); // snapshot size, as it can change due to other threads
        for (int i = 0; i < size; i++) {
            ConcurrentEstimator.Sample sample = samples.poll();
            if (sample == null) {
                break; // exit loop if there are no more samples
            } else {
                lock.writeLock().lock();
                try {
                    if (sample instanceof Sample) {
                        if (((Sample)sample).getSuccess()) {
                            successes++;
                        } else {
                            failures++;
                        }
                    }
                    super.add(sample.getValue());
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * Returns the name of the service the collected statistics relate to.
     *
     * @return the name of the service the collected statistics relate to.
     */
    @Override
    public Results getResults() {
        quiesce();
        lock.readLock().lock();
        try {
            return new Results(subject, unit, count, successes, failures, mean, sq, minimum, maximum, cumulative);
        } finally {
            lock.readLock().unlock();
        }
    }
}
