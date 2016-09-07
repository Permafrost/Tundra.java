package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;
import com.wm.util.ServerException;
import permafrost.tundra.lang.IterableEnumeration;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Runs any uninstall services found in a package before it is uninstalled.
 */
public class PackageUninstallProcessor extends AbstractInvokeChainProcessor {
    /**
     * The service used to uninstall packages, which before invocation is when any detected package uninstall services are invoked by this processor.
     */
    public static final String INTEGRATION_SERVER_PACKAGE_UNINSTALL_SERVICE = "wm.server.packages:packageDelete";
    /**
     * The default package uninstall service regular expression pattern.
     */
    public static final Pattern DEFAULT_PACKAGE_UNINSTALL_SERVICE_PATTERN = Pattern.compile("^([^\\.]+\\.)*support(\\.[^\\.]+)*:uninstall");
    /**
     * A regular expression pattern used to detect package uninstall services.
     */
    protected Pattern pattern;

    /**
     * Constructs a new PackageUninstallProcessor using the default package uninstall service pattern.
     */
    public PackageUninstallProcessor() {
        this(DEFAULT_PACKAGE_UNINSTALL_SERVICE_PATTERN);
    }

    /**
     * Constructs a new PackageUninstallProcessor using the given package uninstall service pattern.
     *
     * @param pattern A regular expression pattern used to find package uninstall services to be invoked on uninstall.
     */
    public PackageUninstallProcessor(String pattern) {
        this(pattern == null ? null : Pattern.compile(pattern));
    }

    /**
     * Constructs a new PackageUninstallProcessor using the given package uninstall service pattern.
     *
     * @param pattern A regular expression pattern used to find package uninstall services to be invoked on uninstall.
     */
    public PackageUninstallProcessor(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Process the invocation chain; if invocation is a package uninstall, run package uninstall services prior.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        if (INTEGRATION_SERVER_PACKAGE_UNINSTALL_SERVICE.equals(baseService.getNSName().getFullName())) {
            IDataCursor cursor = pipeline.getCursor();
            String packageName = IDataUtil.getString(cursor, "package");
            cursor.destroy();

            if (packageName != null) {
                Package pkg = PackageManager.getPackage(packageName);
                if (pkg != null) {
                    for (Object item : IterableEnumeration.of(pkg.getState().getLoaded())) {
                        if (item != null) {
                            String serviceName = item.toString();
                            if (pattern.matcher(serviceName).matches()) {
                                // invoke service
                                try {
                                    Service.doInvoke(NSName.create(serviceName), IDataFactory.create());
                                } catch(Exception ex) {
                                    // do nothing
                                }
                            }
                        }
                    }
                }
            }
        }

        super.process(iterator, baseService, pipeline, serviceStatus);
    }
}
