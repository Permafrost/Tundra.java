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

package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.ISRuntimeException;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.ServerException;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Converts any exceptions thrown by a trigger service to be instances of ISRuntimeException to support being retried.
 */
public class TriggerRetryProcessor extends AbstractInvokeChainProcessor {
    /**
     * Regular expression patterns used to include or exclude services from being affected by this processor.
     */
    protected volatile Pattern includePattern = Pattern.compile(".*"), excludePattern;
    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final TriggerRetryProcessor INSTANCE = new TriggerRetryProcessor();
    }

    /**
     * Default constructor.
     */
    public TriggerRetryProcessor() {}

    /**
     * Construct a new TriggerRetryProcessor using the given include and exclude patterns.
     *
     * @param includePattern    The pattern used to match against service names to be affected by this processor.
     * @param excludePattern    The pattern used to match against service names to be excluded from being affected by
     *                          this processor.
     */
    public TriggerRetryProcessor(Pattern includePattern, Pattern excludePattern) {
        setIncludePattern(includePattern);
        setExcludePattern(excludePattern);
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static TriggerRetryProcessor getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Sets the pattern used to match against service names to be affected by this processor.
     *
     * @param includePattern    The pattern used to match against service names to be affected by this processor.
     */
    public void setIncludePattern(Pattern includePattern) {
        this.includePattern = includePattern;
    }

    /**
     * Sets the pattern used to match against service names to be excluded from being affected by this processor.
     *
     * @param excludePattern    The pattern used to match against service names to be excluded from being affected by
     *                          this processor.
     */
    public void setExcludePattern(Pattern excludePattern) {
        this.excludePattern = excludePattern;
    }

    /**
     * Processes a service invocation: if invoked by a trigger, converts all exceptions thrown to be instances of
     * ISRuntimeException so that the service can be retried.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        boolean isTrigger = Thread.currentThread().getName().startsWith("TriggerTask");
        String serviceName = baseService.getNSName().getFullName();

        if (isTrigger && (excludePattern == null || !excludePattern.matcher(serviceName).matches()) && includePattern != null && includePattern.matcher(serviceName).matches()) {
            try {
                super.process(iterator, baseService, pipeline, serviceStatus);
            } catch(Throwable ex) {
                // convert all exceptions to be instances of ISRuntimeException
                if (ex instanceof ISRuntimeException) {
                    throw (ISRuntimeException)ex;
                } else {
                    throw new ISRuntimeException(ex);
                }
            }
        } else {
            super.process(iterator, baseService, pipeline, serviceStatus);
        }
    }
}
