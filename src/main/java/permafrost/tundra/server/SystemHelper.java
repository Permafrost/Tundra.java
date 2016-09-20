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

import com.wm.app.b2b.server.Build;
import com.wm.app.b2b.server.Resources;
import com.wm.app.b2b.server.Server;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import permafrost.tundra.data.CaseInsensitiveElementList;
import permafrost.tundra.data.CaseInsensitiveIData;
import permafrost.tundra.data.Element;
import permafrost.tundra.data.ElementList;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.data.ImmutableIData;
import permafrost.tundra.data.KeyAliasElement;
import permafrost.tundra.data.MapIData;
import permafrost.tundra.flow.variable.GlobalVariableHelper;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.math.LongHelper;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * A collection of convenience methods for working with the webMethods Integration Server system.
 */
public final class SystemHelper {
    /**
     * The cached structure for system environment variables.
     */
    private static volatile IData system;
    /**
     * Whether the operating system is Microsoft Windows based.
     */
    private static volatile boolean OPERATING_SYSTEM_IS_WINDOWS = isWindows();

    /**
     * Disallow instantiation of this class.
     */
    private SystemHelper() {}

    /**
     * Returns information about Integration Server such as the software version, environment settings, Java properties,
     * well-known directory locations, and memory usage.
     *
     * @return                  Integration Server properties.
     * @throws ServiceException If an error occurs.
     */
    public static IData reflect() throws ServiceException {
        return reflect(false);
    }

    /**
     * Returns information about Integration Server such as the software version, environment settings, Java properties,
     * well-known directory locations, and memory usage.
     *
     * @param refresh           If true, the returned IData will be rebuilt from the system environment and properties
     *                          to reflect any changes that have occurred.
     * @return                  Integration Server properties.
     * @throws ServiceException If an error occurs.
     */
    public static IData reflect(boolean refresh) throws ServiceException {
        if (refresh || system == null) {
            ElementList<String, Object> output = new ElementList<String, Object>();

            output.add(new Element<String, Object>("version", Build.getVersion()));
            output.add(new Element<String, Object>("environment", getEnvironment()));
            output.add(new KeyAliasElement<String, Object>("property", getProperties(), "properties"));
            if (GlobalVariableHelper.isSupported()) output.add(new Element<String, Object>("global", GlobalVariableHelper.list()));
            output.add(new KeyAliasElement<String, Object>("directory", getDirectories(), "directories"));
            output.add(new Element<String, Object>("memory", getMemoryUsage()));

            system = new ImmutableIData(output);
        }

        return system;
    }

    /**
     * Returns true if the operating system is Microsoft Windows based.
     *
     * @return True if the operating system is Microsoft Windows based.
     */
    private static boolean isWindows() {
        boolean result = false;
        String operatingSystemName = System.getProperty("os.name");
        if (operatingSystemName != null) {
            result = operatingSystemName.toLowerCase().contains("windows");
        }
        return result;
    }

    /**
     * Returns the current system environment variables.
     *
     * @return          The current system environment variables.
     */
    private static IData getEnvironment() {
        IData environment = new MapIData<String, String>(new TreeMap<String, String>(System.getenv()));
        if (OPERATING_SYSTEM_IS_WINDOWS) environment = new CaseInsensitiveIData(environment);
        return environment;
    }

    /**
     * Returns the current system properties.
     *
     * @return          The current system properties.
     */
    @SuppressWarnings("unchecked")
    private static IData getProperties() {
        Properties systemProperties = System.getProperties();
        if (systemProperties == null) systemProperties = new Properties();

        String mailFrom = systemProperties.getProperty("mail.from");
        if (mailFrom == null || mailFrom.equals("")) {
            systemProperties.setProperty("mail.from", getDefaultFromEmailAddress());
        }

        // protect against concurrent modification exceptions by cloning the hashtable
        // and sort keys in natural ascending order via a TreeMap
        return new MapIData<String, String>(new TreeMap<String, String>((Hashtable)systemProperties.clone()));
    }

    /**
     * Returns the locations of well-known Integration Server directories.
     *
     * @return          The locations of well-known Integration Server directories.
     */
    private static IData getDirectories() {
        Resources resources = Server.getResources();

        IDataMap output = new IDataMap();
        output.put("root", FileHelper.normalize(resources.getRootDir()));
        output.put("config", FileHelper.normalize(resources.getConfigDir()));
        output.put("datastore", FileHelper.normalize(resources.getDatastoreDir()));
        output.put("jobs", FileHelper.normalize(resources.getJobsDir()));
        output.put("lib", FileHelper.normalize(resources.getLibDir()));
        output.put("logs", FileHelper.normalize(resources.getLogDir()));
        output.put("packages", FileHelper.normalize(resources.getPackagesDir()));
        output.put("recycle", FileHelper.normalize(resources.getRecycleDir()));
        output.put("replicate", FileHelper.normalize(resources.getReplicateDir()));
        output.put("replicate.inbound", FileHelper.normalize(resources.getReplicateInDir()));
        output.put("replicate.outbound", FileHelper.normalize(resources.getReplicateOutDir()));
        output.put("replicate.salvage", FileHelper.normalize(resources.getReplicateSaveDir()));

        return output;
    }

    /**
     * Returns the Java heap memory usage for the currently executing JVM process.
     *
     * @return The Java heap memory usage for the currently executing JVM process.
     */
    private static IData getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - freeMemory;

        IDataMap output = new IDataMap();
        output.put("used", LongHelper.emit(usedMemory));
        output.put("free", LongHelper.emit(freeMemory));
        output.put("total", LongHelper.emit(totalMemory));

        return output;
    }

    /**
     * Returns the default from email address of this Integration Server.
     *
     * @return the default from email address of this Integration Server.
     */
    private static String getDefaultFromEmailAddress() {
        String domain;
        try {
            domain = java.net.InetAddress.getLocalHost().getCanonicalHostName().toLowerCase();
        } catch (java.net.UnknownHostException ex) {
            domain = "unknown";
        }

        return "Integration-Server@" + domain;
    }
}
