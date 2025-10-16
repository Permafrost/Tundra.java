/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Lachlan Dowding
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

package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.util.ServerException;
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.math.gauss.ServiceEstimator;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * An invocation chain processor which collects service invocation duration statistics.
 */
public class ServiceStatisticsProcessor extends AbstractInvokeChainProcessor implements IDataCodable {
    /**
     * The time this processor was started.
     */
    private long startTime = 0;
    /**
     * The collected statistics per service.
     */
    private ConcurrentNavigableMap<String, ServiceEstimator> statisticsByService = new ConcurrentSkipListMap<String, ServiceEstimator>();

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final ServiceStatisticsProcessor INSTANCE = new ServiceStatisticsProcessor();
    }

    /**
     * Disallow instantiation of this class.
     */
    private ServiceStatisticsProcessor() {}

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static ServiceStatisticsProcessor getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Processes a service invocation.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        long start = System.nanoTime();
        boolean success = false;
        try {
            super.process(iterator, baseService, pipeline, serviceStatus);
            success = true;
        } finally {
            if (isStarted()) {
                long end = System.nanoTime();

                String service = baseService.getNSName().getFullName();

                ServiceEstimator statistics = statisticsByService.get(service);
                if (statistics == null) {
                    ServiceEstimator newStatistics = new ServiceEstimator(service, "seconds");
                    statistics = statisticsByService.putIfAbsent(service, newStatistics);
                    if (statistics == null) {
                        statistics = newStatistics;
                    }
                }

                statistics.add(new ServiceEstimator.Sample(success, (end - start) / 1000000000.0));
            }
        }
    }

    /**
     * Returns the current service statistics as an IData document.
     *
     * @return The current service statistics as an IData document.
     */
    @Override
    public IData getIData() {
        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        List<IData> services = new ArrayList<IData>();
        for (String service : statisticsByService.keySet()) {
            ServiceEstimator statistics = statisticsByService.get(service);
            if (statistics != null) {
                services.add(statistics.getResults().getIData());
            }
        }

        IDataUtil.put(cursor, "sampling.started?", started);
        if (started) {
            long endTime = System.currentTimeMillis();
            IDataUtil.put(cursor, "sampling.start", DateTimeHelper.format(startTime));
            IDataUtil.put(cursor, "sampling.end", DateTimeHelper.format(endTime));
            IDataUtil.put(cursor, "sampling.duration", DurationHelper.format(endTime - startTime, DurationPattern.XML));
        }

        cursor.insertAfter("statistics", services.toArray(new IData[0]));
        cursor.insertAfter("statistics.length", services.size());

        cursor.destroy();

        return output;
    }

    /**
     * Sets values from the given IData. This method has not been implemented.
     *
     * @param input                             Not used.
     * @throws UnsupportedOperationException    This exception is always thrown.
     */
    @Override
    public void setIData(IData input) {
        throw new UnsupportedOperationException("setIData not implemented");
    }

    /**
     * Registers this class as an invocation handler and starts processing.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            startTime = System.currentTimeMillis();
            super.start();
        }
    }

    /**
     * Unregisters this class as an invocation handler and stops processing.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            super.stop();

            startTime = 0;
            statisticsByService.clear();
        }
    }
}
