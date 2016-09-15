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

package permafrost.tundra.server;

import com.wm.app.b2b.server.Manifest;
import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.PackageManager;
import com.wm.app.b2b.server.PackageState;
import com.wm.app.b2b.server.PackageStore;
import com.wm.app.b2b.server.ServerAPI;
import com.wm.data.IData;
import com.wm.lang.ns.NSService;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.lang.BooleanHelper;
import permafrost.tundra.lang.IterableEnumeration;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A collection of convenience methods for working with webMethods Integration Server packages.
 */
public final class PackageHelper {
    /**
     * Disallow instantiation of this class.
     */
    private PackageHelper() {}

    /**
     * Returns true if a package with the given name exists on this Integration Server.
     *
     * @param packageName The name of the package to check existence of.
     * @return True if a package with the given name exists.
     */
    public static boolean exists(String packageName) {
        return getPackage(packageName) != null;
    }

    /**
     * Returns the package with the given name if it exists on this Integration Server.
     *
     * @param packageName The name of the package.
     * @return The package with the given name, or null if no package with the given name exists.
     */
    public static Package getPackage(String packageName) {
        if (packageName == null) return null;
        return PackageManager.getPackage(packageName);
    }

    /**
     * Returns the invoking package.
     *
     * @return The invoking package.
     */
    public static Package self() {
        NSService service = ServiceHelper.self();
        if (service != null) {
            return getPackage(service.getPackage().getName());
        }
        return null;
    }

    /**
     * Returns true if the package with the given name is enabled on this Integration Server.
     *
     * @param packageName The name of the package.
     * @return True if the package with the given name is enabled.
     */
    public static boolean isEnabled(String packageName) {
        Package pkg = getPackage(packageName);
        return pkg != null && pkg.isEnabled();
    }

    /**
     * Returns a list of packages on this Integration Server.
     *
     * @return A list of packages on this Integration Server.
     */
    public static Package[] list() {
        return list(false);
    }

    /**
     * Returns a list of packages on this Integration Server.
     *
     * @param enabledOnly If true, only returns enabled packages.
     * @return A list of packages on this Integration Server.
     */
    public static Package[] list(boolean enabledOnly) {
        Package[] packages = PackageManager.getAllPackages();

        SortedSet<Package> packageSet = new TreeSet<Package>(PackageNameComparator.getInstance());
        if (packages != null) {
            for (Package item : packages) {
                if (item != null && (!enabledOnly || item.isEnabled())) packageSet.add(item);
            }
        }

        return packageSet.toArray(new Package[packageSet.size()]);
    }

    /**
     * Reloads the package with the given name.
     *
     * @param packageName The name of the package to be reloaded.
     */
    public static void reload(String packageName) {
        PackageManager.loadPackage(packageName, true);
    }

    /**
     * Converts the given Package to an IData document.
     *
     * @param pkg The package to be converted.
     * @return An IData representation of the given package.
     */
    public static IData toIData(Package pkg) {
        if (pkg == null) return null;

        IDataMap map = new IDataMap();

        map.put("name", pkg.getName());
        map.merge(toIData(pkg.getManifest()));

        IDataMap services = IDataMap.of((IData)map.get("services"));
        services.merge(toIData(pkg.getState()));
        map.put("services", services);

        IDataMap directories = new IDataMap();
        directories.merge(toIData(pkg.getStore()));
        directories.put("config", FileHelper.normalize(ServerAPI.getPackageConfigDir(pkg.getName())));
        map.put("directories", directories);

        return map;
    }

    /**
     * Converts the given list of packages to an IData[].
     *
     * @param packages The list of packages to convert.
     * @return The IData[] representation of the packages.
     */
    public static IData[] toIDataArray(Package... packages) {
        if (packages == null) return new IData[0];

        IData[] output = new IData[packages.length];
        for (int i = 0; i < packages.length; i++) {
            output[i] = toIData(packages[i]);
        }

        return output;
    }

    /**
     * Converts an Package[] to a String[] by calling getName on each package in the list.
     *
     * @param packages The list of packages to convert.
     * @return The String[] representation of the list.
     */
    private static String[] toStringArray(Package... packages) {
        if (packages == null) return new String[0];

        List<String> output = new ArrayList<String>(packages.length);
        for (Package item : packages) {
            if (item != null) output.add(item.getName());
        }

        return output.toArray(new String[output.size()]);
    }

    /**
     * Converts an Iterable object to a String[] by calling toString on each object returned by the iterator.
     *
     * @param iterable The object to convert.
     * @return The String[] representation of the object.
     */
    private static String[] toStringArray(Iterable iterable) {
        if (iterable == null) return new String[0];

        List<String> output = new ArrayList<String>();
        for (Object item : iterable) {
            output.add(item == null ? null : item.toString());
        }

        return output.toArray(new String[output.size()]);
    }

    /**
     * Converts the given Manifest object to an IData document representation.
     *
     * @param manifest The object to be converted.
     * @return An IData representation of the object.
     */
    @SuppressWarnings("unchecked")
    private static IData toIData(Manifest manifest) {
        if (manifest == null) return null;

        IDataMap map = new IDataMap();

        map.put("version", manifest.getVersion());
        map.put("enabled?", BooleanHelper.emit(manifest.isEnabled()));
        map.put("system?", BooleanHelper.emit(manifest.isSystemPkg()));

        IData[] packageDependencies = toIDataArray(manifest.getRequires());
        map.put("dependencies", packageDependencies);
        map.put("dependencies.length", "" + packageDependencies.length);

        IDataMap services = new IDataMap();
        String[] startupServices = toStringArray(manifest.getStartupServices());
        services.put("startup", startupServices);
        services.put("startup.length", "" + startupServices.length);

        String[] shutdownServices = toStringArray(manifest.getShutdownServices());
        services.put("shutdown", shutdownServices);
        services.put("shutdown.length", "" + shutdownServices.length);

        String[] replicationServices = toStringArray(manifest.getReplicationServices());
        services.put("replication", replicationServices);
        services.put("replication.length", "" + replicationServices.length);

        map.put("services", services);

        return map;
    }

    /**
     * Convert the given list of package dependencies to an IData[].
     *
     * @param packageDependencies The object to convert.
     * @return The IData[] representation of the object.
     */
    private static IData[] toIDataArray(Iterable<Manifest.Requires> packageDependencies) {
        if (packageDependencies == null) return new IData[0];

        List<IData> output = new ArrayList<IData>();
        for (Manifest.Requires packageDependency : packageDependencies) {
            IDataMap map = new IDataMap();
            map.put("package", packageDependency.getPackage());
            map.put("version", packageDependency.getVersion());
            output.add(map);
        }

        return output.toArray(new IData[output.size()]);
    }

    /**
     * Converts the given PackageState object to an IData document representation.
     *
     * @param packageState The object to be converted.
     * @return An IData representation of the object.
     */
    private static IData toIData(PackageState packageState) {
        if (packageState == null) return null;

        IDataMap map = new IDataMap();

        String[] loadedServices = toStringArray(IterableEnumeration.of(packageState.getLoaded()));
        map.put("loaded", loadedServices);
        map.put("loaded.length", "" + loadedServices.length);

        return map;
    }

    /**
     * Converts the given PackageStore object to an IData document representation.
     *
     * @param packageStore The object to be converted.
     * @return An IData representation of the object.
     */
    private static IData toIData(PackageStore packageStore) {
        if (packageStore == null) return null;

        IDataMap map = new IDataMap();
        map.put("root", FileHelper.normalize(packageStore.getPackageDir()));
        map.put("ns", FileHelper.normalize(packageStore.getNSDir()));
        map.put("pub", FileHelper.normalize(packageStore.getPubDir()));
        map.put("template", FileHelper.normalize(packageStore.getTemplateDir()));
        map.put("web", FileHelper.normalize(packageStore.getWebDir()));

        return map;
    }
}
