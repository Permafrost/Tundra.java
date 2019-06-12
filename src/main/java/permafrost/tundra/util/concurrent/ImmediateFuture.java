package permafrost.tundra.util.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A future that can immediately return a value.
 *
 * @param <V>   The class of the returned value.
 */
public class ImmediateFuture<V> implements Future<V> {
    /**
     * The value returned by this future.
     */
    protected V value;

    /**
     * Creates a new ImmediateFuture.
     *
     * @param value The value to be returned.
     */
    public ImmediateFuture(V value) {
        this.value = value;
    }

    /**
     * Does nothing as not applicable to this class.
     *
     * @param mayInterruptIfRunning Not used.
     * @return                      false
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    /**
     * Returns the value wrapped by this future.
     *
     * @return the value wrapped by this future.
     */
    @Override
    public V get() {
        return value;
    }

    /**
     * Returns the value wrapped by this future.
     *
     * @param timeout   Not used.
     * @param unit      Not used.
     * @return          The value wrapped by this future.
     */
    @Override
    public V get(long timeout, TimeUnit unit) {
        return get();
    }

    /**
     * Whether the task was cancelled.
     *
     * @return false
     */
    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * Whether the task has completed.
     *
     * @return true
     */
    @Override
    public boolean isDone() {
        return true;
    }
}
