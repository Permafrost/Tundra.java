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

import org.junit.Test;

import static org.junit.Assert.*;

public class NormalDistributionEstimatorTest {
    @Test
    public void testEstimation() throws Exception {
        NormalDistributionEstimator estimator = new NormalDistributionEstimator(30.5, 5, 10.2, 8.6, 4.9);

        assertEquals("count", 5L, estimator.getCount());
        assertEquals("mean", 11.84, estimator.getMean(), 0.1);
        assertEquals("minimum", 4.9, estimator.getMinimum(), 0.1);
        assertEquals("maximum", 30.5, estimator.getMaximum(), 0.1);
        assertEquals("standard deviation", 10.68, estimator.getStandardDeviation(), 0.1);

        estimator.add(6.5);

        assertEquals("count", 6L, estimator.getCount());
        assertEquals("mean", 10.95, estimator.getMean(), 0.1);
        assertEquals("minimum", 4.9, estimator.getMinimum(), 0.1);
        assertEquals("maximum", 30.5, estimator.getMaximum(), 0.1);
        assertEquals("standard deviation", 9.799, estimator.getStandardDeviation(), 0.1);
    }
}