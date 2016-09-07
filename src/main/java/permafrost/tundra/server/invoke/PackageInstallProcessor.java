package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.PackageReplicator;
import com.wm.app.b2b.server.Server;
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
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Runs any install services found in a package after it is installed.
 */
public class PackageInstallProcessor extends AbstractInvokeChainProcessor {
    /**
     * The service used to install packages, which after invocation is when any detected package install services are invoked by this processor.
     */
    public static final String INTEGRATION_SERVER_PACKAGE_INSTALL_SERVICE = "wm.server.packages:packageInstall";
    /**
     * The default package install service regular expression pattern.
     */
    public static final Pattern DEFAULT_PACKAGE_INSTALL_SERVICE_PATTERN = Pattern.compile("^([^\\.]+\\.)*support(\\.[^\\.]+)*:install$");
    /**
     * A regular expression pattern used to detect package install services.
     */
    protected Pattern pattern;
    
    /**
     * Constructs a new PackageInstallProcessor using the default package install service patterns.
     */
    public PackageInstallProcessor() {
        this(DEFAULT_PACKAGE_INSTALL_SERVICE_PATTERN);
    }

    /**
     * Constructs a new PackageInstallProcessor using the given package install service pattern.
     *
     * @param pattern A regular expression pattern used to find package install services to be invoked on install.
     */
    public PackageInstallProcessor(String pattern) {
        this(pattern == null ? null : Pattern.compile(pattern));
    }

    /**
     * Constructs a new PackageInstallProcessor using the given package install service pattern.
     *
     * @param pattern A regular expression pattern used to find package install services to be invoked on install.
     */
    public PackageInstallProcessor(Pattern pattern) {
        if (pattern == null) throw new NullPointerException("pattern must not be null");
        this.pattern = pattern;
    }

    /**
     * Process the invocation chain; if invocation is a package install, run package install services afterwards.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        if (INTEGRATION_SERVER_PACKAGE_INSTALL_SERVICE.equals(baseService.getNSName().getFullName())) {
            IDataCursor cursor = pipeline.getCursor();
            String packageInstallFileName = IDataUtil.getString(cursor, "file");
            cursor.destroy();

            String packageName = null;

            if (packageInstallFileName != null) {
                File packageInstallFile = new File(Server.getPkgInDir(), packageInstallFileName);
                try {
                    packageName = PackageReplicator.getTargetPackageName(packageInstallFileName, packageInstallFile);
                } catch (IOException ex) {
                    // do nothing
                }
            }

            super.process(iterator, baseService, pipeline, serviceStatus);

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
                                } catch (Exception ex) {
                                    // do nothing
                                }
                            }
                        }

                    }
                }
            }
        } else {
            super.process(iterator, baseService, pipeline, serviceStatus);
        }
    }
}
