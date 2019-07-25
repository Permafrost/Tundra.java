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

/**
 * Base class for prioritized objects.
 */
public abstract class AbstractPrioritized implements Prioritized {
    /**
     * The priority of this object, where larger values are higher priority, and with a nominal range of 1..10.
     */
    protected volatile Priority priority;
    /**
     * Returns the priority of this object, where larger values are higher priority, and with a nominal range of 1..10.
     *
     * @return the priority of this object, where larger values are higher priority, and with a nominal range of 1..10.
     */
    @Override
    public Priority getPriority() {
        return priority;
    }

    /**
     * Compares the priority of this object with another object.
     *
     * @param other The other object to be compared to.
     * @return      Whether this object is less than, equal to, or greater than the other object.
     */
    @Override
    public int compareTo(Prioritized other) {
        return AbstractPrioritized.compareTo(this, other);
    }


    /**
     * Compares the priority of this object with another object.
     *
     * @param other The other object to be compared to.
     * @return      Whether this object is less than, equal to, or greater than the other object.
     */
    public static int compareTo(Prioritized self, Prioritized other) {
        Priority selfPriority = self.getPriority();
        Priority otherPriority = other.getPriority();

        int comparison;

        if (selfPriority != null && otherPriority != null) {
            comparison = selfPriority.compareTo(otherPriority);
        } else if (selfPriority == null && otherPriority == null) {
            comparison = 0;
        } else if (selfPriority != null) {
            comparison = -1;
        } else {
            comparison = 1;
        }

        return comparison;
    }
}
