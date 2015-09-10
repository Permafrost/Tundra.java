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

package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.InvokeManager;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.data.IDataUtil;
import com.wm.util.ServerException;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.IDataXMLCoder;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.lang.BooleanHelper;
import permafrost.tundra.time.DateTimeHelper;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * A service invocation processor that saves input and output pipelines to disk.
 */
public class PipelineCaptureProcessor implements InvokeChainProcessor, IDataCodable {
    public static Pattern DEFAULT_SERVICE_PATTERN = Pattern.compile(".*");
    public static File DEFAULT_DIRECTORY = new File("./pipeline");

    /**
     * A thread-pool for asynchronously saving pipelines to disk, so that invocation performance is minimally affected.
     */
    protected ExecutorService executor;

    /**
     * A regular expression which if matching the invoked service will save the pipeline to disk.
     */
    protected volatile Pattern servicePattern;

    /**
     * The directory in which the pipelines are saved.
     */
    protected volatile File directory;

    /**
     * Whether the processor is started or not.
     */
    protected volatile boolean started = false;

    /**
     * When the capture was started.
     */
    protected volatile long startTime;

    /**
     * The local host name, used when naming pipeline files.
     */
    protected volatile String localhost;

    /**
     * Atomic counter incremented for each saved pipeline.
     */
    protected AtomicLong count = new AtomicLong(0);

    /**
     * The datetime format used in the saved pipeline file names.
     */
    protected static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    /**
     * Creates a new pipeline capture processor using default settings.
     */
    public PipelineCaptureProcessor() {
        this(DEFAULT_SERVICE_PATTERN, DEFAULT_DIRECTORY);
    }

    /**
     * Creates a new pipeline capture processor.
     * @param servicePattern The regular expression that if matching an invoked service will save that service's pipeline.
     * @param directory      The directory to which to save the pipelines.
     */
    public PipelineCaptureProcessor(Pattern servicePattern, File directory) {
        setServicePattern(servicePattern);
        setDirectory(directory);
    }

    /**
     * Returns the regular expression used to match service names.
     * @return the regular expression used to match service names.
     */
    public Pattern getServicePattern() {
        return servicePattern;
    }

    /**
     * Sets the regular expression used to match service names.
     * @param servicePattern The regular expression used to match service names.
     */
    public synchronized void setServicePattern(Pattern servicePattern) {
        if (servicePattern == null) throw new NullPointerException("servicePattern must not be null");
        this.servicePattern = servicePattern;
    }

    /**
     * Returns the directory the pipeline files are saved to.
     * @return the directory the pipeline files are saved to.
     */
    public File getDirectory() {
        return new File(directory.getAbsolutePath());
    }

    /**
     * Sets the directory the pipeline files are saved to.
     * @param directory The directory the pipeline files are saved to.
     */
    public synchronized void setDirectory(File directory) {
        if (directory == null) throw new NullPointerException("directory must not be null");
        this.directory = directory;
        this.directory.mkdirs();
    }

    /**
     * Registers this class as an invocation handler and starts saving pipelines.
     */
    public synchronized void start() {
        if (!started) {
            resolveLocalHost();
            executor = Executors.newSingleThreadExecutor(new NamedThreadFactory());
            startTime = System.currentTimeMillis();
            started = true;

            InvokeManager.getDefault().registerProcessor(this);
        }
    }

    /**
     * Sets the local host name member variable using a DNS lookup.
     */
    private void resolveLocalHost() {
        try {
            localhost = sanitize(InetAddress.getLocalHost().getHostName().toLowerCase());
        } catch(UnknownHostException ex) {
            localhost = "unknown";
        }
    }

    /**
     * Unregisters this class as an invocation handler and stops saving pipelines.
     */
    public synchronized void stop() {
        if (started) {
            started = false;

            InvokeManager.getDefault().unregisterProcessor(this);

            // disable new tasks from being submitted
            executor.shutdown();

            try {
                // wait a while for existing tasks to terminate
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    // cancel currently executing tasks
                    executor.shutdownNow();
                }
            } catch(InterruptedException ex) {
                // cancel if current thread also interrupted
                executor.shutdownNow();
                // preserve interrupt status
                Thread.currentThread().interrupt();
            }

            count.set(0);
        }
    }

    /**
     * Processes a service invocation by saving the input and output pipeline to disk.
     *
     * @param iterator      Invocation chain.
     * @param baseService   The invoked service.
     * @param pipeline      The input pipeline for the service.
     * @param serviceStatus The status of the service invocation.
     * @throws ServerException If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        File directory = this.directory; // cache this locally, so that input and output pipelines are always written together to same directory
        String serviceName = baseService.getNSName().getFullName();
        String sanitizedServiceName = null;
        String startDateTime = null;
        long id = 0;

        boolean matches = servicePattern.matcher(serviceName).matches();

        if (matches) {
            sanitizedServiceName = sanitize(serviceName);
            startDateTime = DATE_FORMATTER.format(new Date());
            id = count.incrementAndGet();
        }

        try {
            if (started && matches) {
                try {
                    executor.execute(new SavePipelineToFileRunnable(pipeline, generatePipelineFilename(directory, sanitizedServiceName, startDateTime, id, "input")));
                } catch(RejectedExecutionException ex) {
                    // do nothing, executor has been shutdown
                }
            }

            if (iterator.hasNext()) {
                ((InvokeChainProcessor)iterator.next()).process(iterator, baseService, pipeline, serviceStatus);
            }
        } finally {
            if (started && matches) {
                try {
                    executor.execute(new SavePipelineToFileRunnable(pipeline, generatePipelineFilename(directory, sanitizedServiceName, startDateTime, id, "output")));
                } catch(RejectedExecutionException ex) {
                    // do nothing, executor has been shutdown
                }
            }
        }
    }

    /**
     * Returns a string sanitized for use in a file name.
     *
     * @param string The string to sanitize.
     * @return       The sanitized string.
     */
    protected String sanitize(String string) {
        return string == null ? "" : string.replaceAll("\\W+", "-");
    }

    /**
     * Returns a new file name for saving a pipeline to.
     *
     * @param directory     The parent directory for the pipeline files.
     * @param serviceName   The name of the service the pipeline belongs to.
     * @param startDateTime The formatted start datetime of the service.
     * @param id            The ID of the invocation.
     * @param suffix        A suffix for the file name, such as "input" or "output".
     * @return              A file name suitable for saving the pipeline to.
     */
    protected File generatePipelineFilename(File directory, String serviceName, String startDateTime, long id, String suffix) {
        return new File(directory, String.format("%s_%019d_%019d_%s_%s_%s.%s", localhost, startTime, id, startDateTime, serviceName, suffix, "xml"));
    }

    /**
     * Sets the regular expression pattern used for matching service name and the directory in which
     * pipeline files are saved from the given IData document.
     *
     * @param document An IData document containing the keys: pattern, directory.
     */
    @Override
    public void setIData(IData document) {
        IDataMap map = new IDataMap(document);
        String pattern = (String)map.get("pattern");
        String directory = (String)map.get("directory");

        setServicePattern(Pattern.compile(pattern));
        setDirectory(FileHelper.construct(directory));
    }

    /**
     * Returns an IData representation of this object.
     * @return An IData representation of this object.
     */
    @Override
    public IData getIData() {
        IDataMap map = new IDataMap();
        map.put("pattern", getServicePattern().toString());
        map.put("directory", FileHelper.normalize(getDirectory()));
        map.put("started?", BooleanHelper.emit(started));
        if (started) {
            map.put("start", DateTimeHelper.format(startTime));
            map.put("count", "" + count.get());
        }
        return map;
    }

    /**
     * Thread factory that names the returned threads.
     */
    private class NamedThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("Tundra/PipelineCaptureProcessor#" + thread.getId());
            return thread;
        }
    }

    /**
     * A runnable which saves a pipeline to a file.
     */
    private static class SavePipelineToFileRunnable implements Runnable {
        /**
         * The IData pipeline to be saved.
         */
        protected IData pipeline;
        /**
         * The file to save the pipeline to.
         */
        protected File target;

        /**
         * Creates a new runnable that saves the given pipeline to the given file.
         * @param pipeline The pipeline to be saved.
         * @param target   The file to save the pipeline to.
         */
        public SavePipelineToFileRunnable(IData pipeline, File target) {
            if (pipeline == null) throw new NullPointerException("pipeline must not be null");
            if (target == null) throw new NullPointerException("target must not be null");

            try {
                this.pipeline = IDataUtil.deepClone(pipeline);
                this.target = target;
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Saves the IData pipeline to the target file.
         */
        @Override
        public void run() {
            try {
                IDataXMLCoder coder = new IDataXMLCoder();
                coder.writeToFile(target, pipeline);
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
