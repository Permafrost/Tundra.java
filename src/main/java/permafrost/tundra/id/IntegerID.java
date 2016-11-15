/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
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

package permafrost.tundra.id;

import java.util.concurrent.TimeUnit;

/**
 * Provides a time-based algorithm for generating integer IDs. The IDs generated are lexically ordered by time,
 * and fit within the bounds of a positive signed 32-bit integer.
 */
public class IntegerID {
    /**
     * The default time unit used to generate IDs.
     */
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
    /**
     * The previously generated ID's time component, used to ensure only one ID is generated per second.
     */
    private long previous = -1;
    /**
     * The epoch from which time is measured.
     */
    private long epoch;
    /**
     * The divisor used to calculate the time component of the ID (time % divisor).
     */
    private int timeDivisor;
    /**
     * The number of bits used for partitioning the ID space.
     */
    private int partitionBitLength;
    /**
     * The value used to partition the ID space.
     */
    private int partition;
    /**
     * The unit of time used for calculating the time component of the ID.
     */
    private TimeUnit timeUnit;

    /**
     * Constructs a new ID generator using 31 bits of time measured in seconds since the Unix epoch.
     */
    public IntegerID() {
        this(DEFAULT_TIME_UNIT);
    }

    /**
     * Constructs a new ID generator using 31 bits of time measured in the given time unit since the Unix epoch.
     *
     * @param timeUnit  The time unit used for generating IDs.
     */
    public IntegerID(TimeUnit timeUnit) {
        this(0, timeUnit);
    }

    /**
     * Constructs a new ID generator using 31 bits of time since the given epoch in the given time unit.
     *
     * @param epoch     The epoch from which time is measured.
     * @param timeUnit  The time unit used for generating IDs.
     */
    public IntegerID(long epoch, TimeUnit timeUnit) {
        this(epoch, timeUnit, 0, Integer.SIZE - 1);
    }

    /**
     * Constructs a new ID generator using the given number of bits for the partition component, the remaining number
     * of bits from 31 for the time component with time measured from Unix epoch in seconds.
     *
     * @param partitionBitLength    The number of bits used for partitioning the ID space.
     * @param partition             The value to use to partition the ID space.
     */
    public IntegerID(int partitionBitLength, int partition) {
        this(DEFAULT_TIME_UNIT, partitionBitLength, partition);
    }

    /**
     * Constructs a new ID generator using the given number of bits for the partition component, the remaining number
     * of bits from 31 for the time component with time measured from Unix epoch in the given time unit.
     *
     * @param timeUnit              The time unit used for generating IDs.
     * @param partitionBitLength    The number of bits used for partitioning the ID space.
     * @param partition             The value to use to partition the ID space.
     */
    public IntegerID(TimeUnit timeUnit, int partitionBitLength, int partition) {
        this(0, timeUnit, partitionBitLength, partition);
    }

    /**
     * Constructs a new ID generator using the given number of bits for the partition component, the remaining number
     * of bits from 31 for the time component with time measured from the given epoch in the given time unit.
     *
     * @param epoch                 The epoch from which time is measured.
     * @param timeUnit              The time unit used for generating IDs.
     * @param partitionBitLength    The number of bits used for partitioning the ID space.
     * @param partition             The value to use to partition the ID space.
     */
    public IntegerID(long epoch, TimeUnit timeUnit, int partitionBitLength, int partition) {
        if (epoch < 0) throw new IllegalArgumentException("epoch must be greater than or equal to zero");
        if (partitionBitLength <= 0) throw new IllegalArgumentException("partitionBitLength must be greater than or equal to zero");
        if (partition < 0) throw new IllegalArgumentException("partition must be greater than or equal to zero");
        if (partitionBitLength > (Integer.SIZE - 1)) throw new IllegalArgumentException("partitionBitLength must be less than or equal to " + (Integer.SIZE - 1));

        this.epoch = epoch;
        this.partitionBitLength = partitionBitLength;
        int partitionDivisor = (int)Math.pow(2, partitionBitLength);
        this.partition = partitionDivisor == 0 ? 0 : partition % partitionDivisor;
        this.timeDivisor = (int)Math.pow(2, ((Integer.SIZE - 1) - partitionBitLength)) - 1;
        this.timeUnit = timeUnit;
    }

    /**
     * Returns a newly generated ID.
     *
     * @return A newly generated ID.
     */
    public synchronized int generate() {
        long next;

        do {
            next = currentTime();
            next = previous == -1 ? next : Math.min(previous + 1, next);

            if (next == previous) {
                try {
                    timeUnit.sleep(1);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } while (next == previous);

        previous = next;

        return generate(next);
    }

    /**
     * Returns a new ID given a time value.
     *
     * @param time  The time to use to generate the new ID.
     * @return      A new ID.
     */
    private int generate(long time) {
        return (int)((time % timeDivisor) << partitionBitLength) | partition;
    }

    /**
     * Returns the current time since this generator's epoch in this generator's time unit.
     *
     * @return the current time since this generator's epoch in this generator's time unit.
     */
    private long currentTime() {
        return timeUnit.convert(System.currentTimeMillis() - epoch, TimeUnit.MILLISECONDS);
    }
}
