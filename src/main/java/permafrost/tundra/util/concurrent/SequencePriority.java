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

package permafrost.tundra.util.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Prioritizes based on object creation sequence.
 */
public class SequencePriority implements Priority {
    /**
     * The sequencer used to stamp objects with their creation sequence.
     */
    protected static AtomicLong SEQUENCER = new AtomicLong(Long.MIN_VALUE);
    /**
     * The object creation sequence.
     */
    protected long sequence = SEQUENCER.incrementAndGet();

    /**
     * Compares this CallableRoutePriority with another CallableRoutePriority, in order to prioritize routes.
     *
     * @param other The other CallableRoutePriority to compare to.
     * @return      The result of the comparison.
     */
    @Override
    public int compareTo(Priority other) {
        int comparison;
        if (other instanceof SequencePriority) {
            long otherSequence = ((SequencePriority)other).sequence;
            if (sequence < otherSequence) {
                comparison = -1;
            } else if (sequence > otherSequence) {
                comparison = 1;
            } else {
                comparison = 0;
            }
        } else {
            // cannot compare with different priority implementation, so default to equal
            comparison = 0;
        }
        return comparison;
    }
}
