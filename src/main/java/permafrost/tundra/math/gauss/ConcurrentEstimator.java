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

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class for incrementally calculating the mean and standard deviation.
 */
public class ConcurrentEstimator extends Estimator {
    /**
     * Represents a single sample.
     */
    public static class Sample {
        /**
         * The value of the sample.
         */
        protected double value;

        /**
         * Constructs a new sample.
         *
         * @param value the value of the sample.
         */
        public Sample(double value) {
            this.value = value;
        }

        /**
         * Returns the value of the specified number as a double, which may involve rounding.
         * @return the value of the specified number as a double, which may involve rounding.
         */
        public double getValue() {
            return value;
        }
    }

    /**
     * The lock used to synchronize access by multiple read and write threads.
     */
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    /**
     * Use a sample queue to reduce thread synchronization required by estimator.
     */
    protected final Deque<Sample> samples = new LinkedBlockingDeque<Sample>(32);

    /**
     * Constructs a new estimator object.
     *
     * @param unit The unit of measurement related to the measured samples.
     */
    public ConcurrentEstimator(String subject, String unit) {
        super(subject, unit);
    }

    /**
     * Adds a new sample to the set of samples used for estimating the standard deviation.
     *
     * @param sample The sample to be added.
     */
    public void add(Sample sample) {
        while (!samples.offer(sample)) {
            quiesce();
        }
    }

    /**
     * Resets the estimator back to a set of zero samples.
     */
    @Override
    public void reset() {
        lock.writeLock().lock();
        try {
            super.reset();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Ensure all calculations are up to date, must be called once finished collecting samples.
     */
    protected void quiesce() {
        int size =  samples.size(); // snapshot size, as it can change due to other threads
        for (int i = 0; i < size; i++) {
            Sample sample = samples.poll();
            if (sample == null) {
                break; // exit loop if there are no more samples
            } else {
                lock.writeLock().lock();
                try {
                    super.add(sample.getValue());
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * Returns the estimator's results.
     *
     * @return the estimator's results.
     */
    @Override
    public Results getResults() {
        quiesce();
        lock.readLock().lock();
        try {
            return super.getResults();
        } finally {
            lock.readLock().unlock();
        }
    }
}
