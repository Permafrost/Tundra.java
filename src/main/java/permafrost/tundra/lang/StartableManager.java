/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
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

package permafrost.tundra.lang;

import com.wm.data.IData;
import com.wm.data.IDataPortable;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages a collection of Startable objects.
 *
 * @param <K> The component type of the keys associated with managed worker objects.
 * @param <V> The component type of the managed worker objects.
 */
public class StartableManager<K, V extends Startable> implements Startable, IDataCodable {
    /**
     * Registry of Startable objects managed by this manager object.
     */
    protected final ConcurrentMap<K, V> REGISTRY = new ConcurrentHashMap<K, V>();
    /**
     * Whether this manager is started
     */
    protected volatile boolean started = false;
    /**
     * Whether workers should be started when registered if the manager is started.
     */
    protected final boolean startWorkerOnRegistration;

    /**
     * Create a new StartableManager.
     */
    public StartableManager() {
        this(false);
    }

    /**
     * Create a new StartableManager.
     *
     * @param startWorkerOnRegistration Whether workers should be started when registered if the manager is started.
     */
    public StartableManager(boolean startWorkerOnRegistration) {
        this.startWorkerOnRegistration = startWorkerOnRegistration;
    }

    /**
     * Registers the given Startable against the given registry key. If this manager is started, the given Startable
     * is started immediately after registration.
     *
     * @param key       The key to register.
     * @param worker    The worker to associated with the key.
     * @return          True if the value was associated with the key. False if another associated value already exists.
     */
    protected boolean register(K key, V worker) {
        boolean didRegister = REGISTRY.putIfAbsent(key, worker) == null;
        if (didRegister && started && startWorkerOnRegistration) {
            worker.start();
        }
        return didRegister;
    }

    /**
     * Unregisters the given Startable associated with the given key.
     *
     * @param key       The key to unregister.
     * @param worker    The worker to associated with the key.
     * @return          True if the key and worker was unregistered. False if the worker is not associated with the key
     *                  or the key does not exist.
     */
    protected boolean unregister(K key, V worker) {
        return REGISTRY.remove(key, worker);
    }

    /**
     * Returns the Startable associated with the given key.
     *
     * @param key   The key.
     * @return      The Startable associated with the given key, or null if the key does not exist.
     */
    protected V get(K key) {
        return REGISTRY.get(key);
    }

    /**
     * Starts all objects managed by this manager.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            started = true;
            start(REGISTRY.values());
        }
    }

    /**
     * Stops all objects managed by this manager.
     */
    @Override
    public synchronized void stop() {
        stop(false);
    }

    /**
     * Stops all objects managed by this manager.
     *
     * @param clear If true, all registrations are removed.
     */
    public synchronized void stop(boolean clear) {
        if (started) {
            started = false;
            if (clear) {
                clear();
            } else {
                stop(REGISTRY.values());
            }
        }
    }

    /**
     * Stops then unregisters all workers.
     */
    public synchronized void clear() {
        try {
            stop(REGISTRY.values());
        } finally {
            REGISTRY.clear();
        }
    }

    /**
     * Restarts all objects managed by this manager.
     */
    @Override
    public synchronized void restart() {
        List<Throwable> exceptions = new ArrayList<Throwable>();

        try {
            stop();
        } catch(Throwable exception) {
            exceptions.add(exception);
        }

        try {
            start();
        } catch(Throwable exception) {
            exceptions.add(exception);
        }

        if (exceptions.size() > 0) {
            ExceptionHelper.raiseUnchecked(exceptions);
        }
    }

    /**
     * Returns true if the manager is started.
     *
     * @return true if the manager is started.
     */
    @Override
    public boolean isStarted() {
        return started;
    }

    /**
     * Returns an IData representation of this object.
     *
     * @return an IData representation of this object.
     */
    @Override
    public IData getIData() {
        IDataMap document = new IDataMap();
        document.put("supervisor.started?", isStarted());
        List<IData> workers = new ArrayList<IData>(REGISTRY.size());
        for (V value : REGISTRY.values()) {
            if (value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable || value instanceof IData) {
                workers.add(IDataHelper.toIData(value));
            }
        }
        document.put("workers", workers.toArray(new IData[0]));
        document.put("workers.length", workers.size());
        return document;
    }

    /**
     * Method not implemented.
     *
     * @param document                          The IData document.
     */
    @Override
    public void setIData(IData document) {
        // ignore, not implemented
    }

    /**
     * Starts all given Startable objects.
     *
     * @param workers   The workers to be stopped.
     * @param <V>       The type of worker.
     */
    public static <V extends Startable> void start(Iterable<V> workers) {
        List<Throwable> exceptions = new ArrayList<Throwable>();

        for (V worker : workers) {
            try {
                worker.start();
            } catch(Throwable exception) {
                exceptions.add(exception);
            }
        }

        if (exceptions.size() > 0) {
            ExceptionHelper.raiseUnchecked(exceptions);
        }
    }

    /**
     * Stops all given Startable objects.
     *
     * @param workers   The workers to be stopped.
     * @param <V>       The type of worker.
     */
    public static <V extends Startable> void stop(Iterable<V> workers) {
        List<Throwable> exceptions = new ArrayList<Throwable>();

        for (V worker : workers) {
            try {
                worker.stop();
            } catch(Throwable exception) {
                exceptions.add(exception);
            }
        }

        if (exceptions.size() > 0) {
            ExceptionHelper.raiseUnchecked(exceptions);
        }
    }
}
