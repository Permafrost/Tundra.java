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
import permafrost.tundra.math.NormalDistributionEstimator;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingDeque;

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
    private ConcurrentNavigableMap<String, ServiceStatistics> statisticsByService = new ConcurrentSkipListMap<String, ServiceStatistics>();

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
        try {
            super.process(iterator, baseService, pipeline, serviceStatus);
        } finally {
            if (isStarted()) {
                long end = System.nanoTime();

                String service = baseService.getNSName().getFullName();

                ServiceStatistics statistics = statisticsByService.get(service);
                if (statistics == null) {
                    ServiceStatistics newStatistics = new ServiceStatistics(service);
                    statistics = statisticsByService.putIfAbsent(service, newStatistics);
                    if (statistics == null) {
                        statistics = newStatistics;
                    }
                }

                statistics.add((end - start) / 1000000000.0);
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
            ServiceStatistics statistics = statisticsByService.get(service);
            if (statistics != null) {
                services.add(statistics.getIData());
            }
        }

        IDataUtil.put(cursor, "sampling.started?", started);
        if (started) {
            IDataUtil.put(cursor, "sampling.start", DateTimeHelper.format(startTime));
            IDataUtil.put(cursor, "sampling.duration", DurationHelper.format(System.currentTimeMillis() - startTime, DurationPattern.XML));
        }

        cursor.insertAfter("statistics", services.toArray(new IData[services.size()]));
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

    /**
     * Service statistics collection class.
     */
    private static class ServiceStatistics extends NormalDistributionEstimator implements IDataCodable {
        /**
         * The pattern used to format decimals for display.
         */
        private static final String DECIMAL_PATTERN = "###,##0.000000";
        /**
         * The pattern used to format integers for display.
         */
        private static final String INTEGER_PATTERN = "###,##0";
        /**
         * Use a sample queue to reduce thread synchronization required by estimator.
         */
        private Deque<Double> samples = new LinkedBlockingDeque<Double>(64);
        /**
         * The name of the service the collected statistics relate to.
         */
        private String service;

        /**
         * Constructs a new estimator object.
         *
         * @param service   The name of the service the collected statistics relate to.
         */
        public ServiceStatistics(String service) {
            this(service, null);
        }

        /**
         * Constructs a new estimator object.
         *
         * @param service   The name of the service the collected statistics relate to.
         * @param unit      The unit of measurement related to the measured samples.
         */
        public ServiceStatistics(String service, String unit) {
            super(unit);
            this.service = service;
        }

        @Override
        public NormalDistributionEstimator add(double sample) {
            while (!samples.offer(sample)) {
                quiesce();
            }

            return this;
        }

        /**
         * Returns the name of the service the collected statistics relate to.
         *
         * @return the name of the service the collected statistics relate to.
         */
        public String getService() {
            return service;
        }

        /**
         * Ensure all calculations are up to date.
         */
        @Override
        protected void quiesce() {
            Double previousSample;
            while((previousSample = samples.poll()) != null) {
                super.add(previousSample);
            }
        }

        /**
         * Returns an IData representation of this object.
         *
         * @return An IData representation of this object.
         */
        @Override
        public IData getIData() {
            DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_PATTERN);
            DecimalFormat integerFormat = new DecimalFormat(INTEGER_PATTERN);

            IData output = IDataFactory.create();
            IDataCursor cursor = output.getCursor();

            double mean = getMean();
            double standardDeviation = getStandardDeviation();
            double minimum = getMinimum();
            double maximum = getMaximum();
            double cumulative = getCumulative();
            long count = getCount();

            cursor.insertAfter("service", getService());
            cursor.insertAfter("minimum", minimum);
            cursor.insertAfter("minimum.formatted", decimalFormat.format(minimum));
            cursor.insertAfter("average", mean);
            cursor.insertAfter("average.formatted", decimalFormat.format(mean));
            cursor.insertAfter("deviation.standard", standardDeviation);
            cursor.insertAfter("deviation.standard.formatted", decimalFormat.format(standardDeviation));
            cursor.insertAfter("maximum", maximum);
            cursor.insertAfter("maximum.formatted", decimalFormat.format(maximum));
            cursor.insertAfter("cumulative", cumulative);
            cursor.insertAfter("cumulative.formatted", decimalFormat.format(cumulative));
            cursor.insertAfter("count", count);
            cursor.insertAfter("count.formatted", integerFormat.format(count));

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
    }
}
