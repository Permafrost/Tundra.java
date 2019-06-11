package permafrost.tundra.server;

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ServerThread;
import permafrost.tundra.id.UUIDHelper;
import permafrost.tundra.lang.ThreadHelper;

import java.util.concurrent.ThreadFactory;

public class ServerThreadFactory implements ThreadFactory {
    /**
     * The prefix used on all created thread names.
     */
    protected String threadNamePrefix;
    /**
     * The optional suffix used on all created thread names.
     */
    protected String threadNameSuffix;
    /**
     * The priority threads will be created with.
     */
    protected int threadPriority;
    /**
     * The invoke state threads will be created with.
     */
    protected InvokeState invokeState;
    /**
     * Whether created threads should be daemon threads.
     */
    protected boolean daemon;

    /**
     * Creates a new ServerThreadFactory object.
     *
     * @param threadNamePrefix  The threadNamePrefix of the factory, used to prefix thread names.
     * @param invokeState       The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, InvokeState invokeState) {
        this(threadNamePrefix, null, invokeState, Thread.NORM_PRIORITY, false);
    }

    /**
     * Creates a new ServerThreadFactory object.
     *
     * @param threadNamePrefix The threadNamePrefix of the factory, used to prefix thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param invokeState      The invoke state to clone for each thread created.
     * @param threadPriority   The priority used for each thread created.
     * @param daemon           Whether the created threads should be daemon threads.
     */
    public ServerThreadFactory(String threadNamePrefix, String threadNameSuffix, InvokeState invokeState, int threadPriority, boolean daemon) {
        if (threadNamePrefix == null) throw new NullPointerException("threadNamePrefix must not be null");
        if (invokeState == null) throw new NullPointerException("invokeState must not be null");

        this.threadNamePrefix = threadNamePrefix;
        this.threadNameSuffix = threadNameSuffix;
        this.threadPriority = ThreadHelper.normalizePriority(threadPriority);
        this.invokeState = invokeState;
        this.daemon = daemon;
    }

    /**
     * Returns a newly constructed Thread that will execute the given Runnable.
     *
     * @param runnable  The Runnable to be executed by the thread.
     * @return          The newly constructed thread.
     */
    @Override
    public Thread newThread(Runnable runnable) {
        ServerThread thread = new ServerThread(runnable);
        thread.setName(newThreadName());

        InvokeState state = InvokeStateHelper.clone(invokeState);
        state.setSession(SessionHelper.create(thread.getName(), invokeState.getUser()));
        state.setCheckAccess(false);

        thread.setInvokeState(state);
        thread.setUncaughtExceptionHandler(UncaughtExceptionLogger.getInstance());
        thread.setPriority(threadPriority);
        thread.setDaemon(daemon);

        return thread;
    }

    /**
     * Returns a thread name for a newly generated thread.
     *
     * @return a thread name for a newly generated thread.
     */
    protected String newThreadName() {
        String threadName;

        String threadContext = UUIDHelper.generate();

        if (threadNameSuffix != null) {
            threadName = String.format("%s Thread=%s %s", threadNamePrefix, threadContext, threadNameSuffix);
        } else {
            threadName = String.format("%s Thread=%s", threadNamePrefix, threadContext);
        }

        return threadName;
    }
}
