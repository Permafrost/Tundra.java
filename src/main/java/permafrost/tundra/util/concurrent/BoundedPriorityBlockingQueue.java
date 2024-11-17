package permafrost.tundra.util.concurrent;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * A PriotityBlockingQueue with a bounded maximum capacity.
 *
 * @param <E>   The class of items held on this queue.
 */
public class BoundedPriorityBlockingQueue<E> extends PriorityBlockingQueue<E> {
    /**
     * The initial capacity used to construct the queue.
     */
    protected static final int DEFAULT_INITIAL_CAPACITY = 11;
    /**
     * The maximum capacity of this queue.
     */
    protected final int CAPACITY;

    /**
     * Creates a new queue.
     *
     * @param capacity  The maximum capacity of this queue.
     */
    public BoundedPriorityBlockingQueue(int capacity) {
        super(DEFAULT_INITIAL_CAPACITY);
        CAPACITY = capacity;
    }

    /**
     * Creates a new queue.
     *
     * @param capacity      The maximum capacity of this queue.
     * @param comparator    The comparator used to compare items in this queue.
     */
    public BoundedPriorityBlockingQueue(int capacity, Comparator<? super E> comparator) {
        super(DEFAULT_INITIAL_CAPACITY, comparator);
        CAPACITY = capacity;
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param item  The item to insert.
     * @return      True if the item was inserted, false if queue is at maximum capacity and the item was not inserted.
     */
    @Override
    public boolean offer(E item) {
        if (size() >= CAPACITY) return false;
        return super.offer(item);
    }
}
