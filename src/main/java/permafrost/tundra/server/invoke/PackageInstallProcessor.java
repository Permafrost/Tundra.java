package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.PackageReplicator;
import com.wm.app.b2b.server.Server;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.lang.ns.NSName;
import com.wm.util.ServerException;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.lang.IterableEnumeration;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Runs any install services found in a package after it is installed, and any uninstall services found in a packager
 * prior to it being uninstalled.
 */
public class PackageInstallProcessor extends AbstractInvokeChainProcessor {
    /**
     * The service used to install packages, which after invocation is when any detected package install services are invoked by this processor.
     */
    public static final String INTEGRATION_SERVER_PACKAGE_NAMESPACE = "wm.server.packages";
    /**
     * The service used to install packages, which after invocation is when any detected package install services are invoked by this processor.
     */
    public static final String INTEGRATION_SERVER_PACKAGE_INSTALL_SERVICE = INTEGRATION_SERVER_PACKAGE_NAMESPACE + ":packageInstall";
    /**
     * The service used to recover packages, which after invocation is when any detected package install services are invoked by this processor.
     */
    public static final String INTEGRATION_SERVER_PACKAGE_RECOVER_SERVICE = INTEGRATION_SERVER_PACKAGE_NAMESPACE + ":packageRecover";
    /**
     * The service used to uninstall packages, which before invocation is when any detected package uninstall services are invoked by this processor.
     */
    public static final String INTEGRATION_SERVER_PACKAGE_UNINSTALL_SERVICE = INTEGRATION_SERVER_PACKAGE_NAMESPACE + ":packageDelete";
    /**
     * The default package install service regular expression pattern.
     */
    public static final Pattern DEFAULT_PACKAGE_INSTALL_SERVICE_PATTERN = Pattern.compile("^([^\\.]+\\.)*support(\\.[^\\.]+)*:install$");
    /**
     * The default package uninstall service regular expression pattern.
     */
    public static final Pattern DEFAULT_PACKAGE_UNINSTALL_SERVICE_PATTERN = Pattern.compile("^([^\\.]+\\.)*support(\\.[^\\.]+)*:uninstall$");
    /**
     * A regular expression pattern used to detect package install services.
     */
    protected Pattern installServicePattern, uninstallServicePattern;
    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final PackageInstallProcessor INSTANCE = new PackageInstallProcessor();
    }
    
    /**
     * Constructs a new PackageInstallProcessor using the default package install and uninstall service patterns.
     */
    public PackageInstallProcessor() {
        this(DEFAULT_PACKAGE_INSTALL_SERVICE_PATTERN, DEFAULT_PACKAGE_UNINSTALL_SERVICE_PATTERN);
    }

    /**
     * Constructs a new PackageInstallProcessor using the given package install service pattern.
     *
     * @param installServicePattern     A regular expression pattern used to find package install services.
     * @param uninstallServicePattern   A regular expression pattern used to find package uninstall services..
     */
    public PackageInstallProcessor(String installServicePattern, String uninstallServicePattern) {
        this(installServicePattern == null ? null : Pattern.compile(installServicePattern),
             uninstallServicePattern == null ? null : Pattern.compile(uninstallServicePattern));
    }

    /**
     * Constructs a new PackageInstallProcessor using the given package install service pattern.
     *
     * @param installServicePattern     A regular expression pattern used to find package install services.
     * @param uninstallServicePattern   A regular expression pattern used to find package uninstall services..
     *
     */
    public PackageInstallProcessor(Pattern installServicePattern, Pattern uninstallServicePattern) {
        if (installServicePattern == null) throw new NullPointerException("installServicePattern must not be null");
        if (uninstallServicePattern == null) throw new NullPointerException("uninstallServicePattern must not be null");

        this.installServicePattern = installServicePattern;
        this.uninstallServicePattern = uninstallServicePattern;
    }

    /**
     * Returns the default singleton instance of this class.
     *
     * @return The default singleton instance of this class.
     */
    public static PackageInstallProcessor getDefaultInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Process the invocation chain: if invocation is a package install, run package install services afterwards; if
     * the invocation is a package uninstall, run package uninstall services before.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        String serviceName = baseService.getNSName().getFullName();

        if (serviceName.startsWith(INTEGRATION_SERVER_PACKAGE_NAMESPACE)) {
            String packageName = null;

            boolean isPackageInstall = serviceName.equals(INTEGRATION_SERVER_PACKAGE_INSTALL_SERVICE);
            boolean isPackageRecover = serviceName.equals(INTEGRATION_SERVER_PACKAGE_RECOVER_SERVICE);
            boolean isPackageUninstall = serviceName.equals(INTEGRATION_SERVER_PACKAGE_UNINSTALL_SERVICE);

            if (isPackageInstall) {
                String packageInstallFileName = IDataHelper.get(pipeline, "file", String.class);

                if (packageInstallFileName != null) {
                    File packageInstallFile = new File(Server.getPkgInDir(), packageInstallFileName);
                    try {
                        packageName = PackageReplicator.getTargetPackageName(packageInstallFileName, packageInstallFile);
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
            } else if (isPackageRecover || isPackageUninstall) {
                packageName = IDataHelper.get(pipeline, "package", String.class);
            }

            // run any uninstall services found in the package being uninstalled
            if (isPackageUninstall && packageName != null) {
                Package pkg = PackageManager.getPackage(packageName);
                if (pkg != null) {
                    for (Object item : IterableEnumeration.of(pkg.getState().getLoaded())) {
                        if (item != null) {
                            String service = item.toString();
                            if (uninstallServicePattern.matcher(service).matches()) {
                                try {
                                    Service.doInvoke(NSName.create(service), IDataFactory.create());
                                } catch (Exception ex) {
                                    // do nothing
                                }
                            }
                        }
                    }
                }
            }

            super.process(iterator, baseService, pipeline, serviceStatus);

            // run any install services found in the package being installed
            if ((isPackageInstall || isPackageRecover) && packageName != null) {
                Package pkg = PackageManager.getPackage(packageName);
                if (pkg != null) {
                    for (Object item : IterableEnumeration.of(pkg.getState().getLoaded())) {
                        if (item != null) {
                            String service = item.toString();
                            if (installServicePattern.matcher(service).matches()) {
                                try {
                                    Service.doInvoke(NSName.create(service), IDataFactory.create());
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
