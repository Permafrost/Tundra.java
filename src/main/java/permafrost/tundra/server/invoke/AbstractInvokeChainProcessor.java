package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.InvokeManager;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.ServerException;
import java.util.Iterator;

/**
 * An abstract base class for invoke chain processors.
 */
public abstract class AbstractInvokeChainProcessor implements InvokeChainProcessor {
    /**
     * Whether the processor is started or not.
     */
    protected volatile boolean started = false;

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
        if (iterator.hasNext()) ((InvokeChainProcessor)iterator.next()).process(iterator, baseService, pipeline, serviceStatus);
    }

    /**
     * Returns true if this processor is started.
     *
     * @return True if this processor is started.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Registers this class as an invocation handler and starts processing.
     */
    public synchronized void start() {
        if (!started) {
            started = true;
            InvokeManager.getDefault().registerProcessor(this);
        }
    }

    /**
     * Unregisters this class as an invocation handler and stops processing.
     */
    public synchronized void stop() {
        if (started) {
            started = false;
            InvokeManager.getDefault().unregisterProcessor(this);
        }
    }
}
