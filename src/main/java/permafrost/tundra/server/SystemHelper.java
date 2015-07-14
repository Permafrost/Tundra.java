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
import com.wm.data.IData;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.io.FileHelper;

import java.util.Hashtable;
import java.util.Properties;
import java.util.TreeMap;

/**
 * A collection of convenience methods for working with the webMethods Integration Server system.
 */
public class SystemHelper {
    /**
     * Disallow instantiation of this class.
     */
    private SystemHelper() {}

    /**
     * Returns information about Integration Server such as the software version,
     * environment settings, Java properties, well-known directory locations, and
     * memory usage.
     *
     * @return Integration Server properties.
     */
    public static IData getIntegrationServerProperties() {
        IDataMap output = new IDataMap();
        output.put("version", Build.getVersion());
        output.put("environment", getSystemEnvironment());
        output.put("properties", getSystemProperties());
        output.put("directories", getSystemDirectories());
        output.put("memory", getMemoryUsage());

        return output;
    }

    /**
     * Returns the current system environment variables.
     * @return The current system environment variables.
     */
    public static IData getSystemEnvironment() {
        return new IDataMap(new TreeMap(System.getenv()));
    }

    /**
     * Returns the current system properties.
     * @return The current system properties.
     */
    @SuppressWarnings("unchecked")
    public static IData getSystemProperties() {
        Properties properties = System.getProperties();
        if (properties == null) properties = new Properties();

        String mailFrom = properties.getProperty("mail.from");
        if (mailFrom == null || mailFrom.equals("")) {
            properties.setProperty("mail.from", getDefaultFromEmailAddress());
        }

        // protect against concurrent modification exceptions by cloning the hashtable
        // and sort keys in natural ascending order via a TreeMap
        return new IDataMap(new TreeMap((Hashtable)properties.clone()));
    }

    /**
     * Returns the locations of well-known Integration Server directories.
     * @return The locations of well-known Integration Server directories.
     */
    public static IData getSystemDirectories() {
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
     * @return The Java heap memory usage for the currently executing JVM process.
     */
    public static IData getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - freeMemory;

        IDataMap output = new IDataMap();
        output.put("used", "" + usedMemory);
        output.put("free", "" + freeMemory);
        output.put("total", "" + totalMemory);

        return output;
    }

    /**
     * Returns the default from email address of this Integration Server.
     * @return the default from email address of this Integration Server.
     */
    private static String getDefaultFromEmailAddress() {
        String domain = "unknown";
        try {
            java.net.InetAddress address = java.net.InetAddress.getLocalHost();
            domain = address.getCanonicalHostName().toLowerCase();
        } catch (java.net.UnknownHostException ex) { }
        return "Integration-Server@" + domain;
    }
}
