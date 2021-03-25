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
import com.wm.data.DataException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import permafrost.tundra.data.AbstractIData;
import permafrost.tundra.data.CaseInsensitiveIData;
import permafrost.tundra.data.Element;
import permafrost.tundra.data.ElementList;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.data.ImmutableIData;
import permafrost.tundra.data.KeyAliasElement;
import permafrost.tundra.data.MapIData;
import permafrost.tundra.flow.variable.GlobalVariableHelper;
import permafrost.tundra.id.UUIDHelper;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.math.LongHelper;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.TimeZoneHelper;
import java.net.UnknownHostException;
import java.util.Calendar;
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
            output.add(new Element<String, Object>("localhost", getHost()));
            output.add(new Element<String, Object>("environment", getEnvironment()));
            output.add(new KeyAliasElement<String, Object>("property", getProperties(), "properties"));
            if (GlobalVariableHelper.isSupported()) output.add(new Element<String, Object>("global", GlobalVariableHelper.list()));
            output.add(new KeyAliasElement<String, Object>("directory", getDirectories(), "directories"));

            system = new ImmutableIData(output);
        }

        IData duplicate = IDataHelper.duplicate(system);
        IDataCursor cursor = duplicate.getCursor();
        try {
            cursor.insertAfter("memory", getMemoryUsage());
            cursor.insertAfter("reference", getReference());
        } finally {
            cursor.destroy();
        }

        return duplicate;
    }

    /**
     * Returns an IData that can be used as a reference with a random uuid and the current datetime.
     *
     * @return an IData that can be used as a reference with a random uuid and the current datetime.
     */
    private static IData getReference() {
        IData reference = IDataFactory.create();
        IDataCursor cursor = reference.getCursor();

        try {
            cursor.insertAfter("uuid", UUIDHelper.generate());
            cursor.insertAfter("datetime", getDateTime());
        } finally {
            cursor.destroy();
        }

        return reference;
    }

    /**
     * Returns a special IData that uses keys as datetime patterns to format the current datetime dynamically.
     *
     * @return a special IData that uses keys as datetime patterns to format the current datetime dynamically.
     */
    private static IData getDateTime() {
        IData datetime = IDataFactory.create();
        IDataCursor cursor = datetime.getCursor();

        try {
            Calendar nowLocal = Calendar.getInstance();
            Calendar nowUTC = Calendar.getInstance();
            nowUTC.setTimeInMillis(nowLocal.getTimeInMillis());
            nowUTC.setTimeZone(TimeZoneHelper.UTC_TIME_ZONE);
            cursor.insertAfter("local", new DateTimeIData(nowLocal));
            cursor.insertAfter("utc", new DateTimeIData(nowUTC));
        } finally {
            cursor.destroy();
        }

        return datetime;
    }

    /**
     * Returns an IData representation of the localhost.
     *
     * @return an IData representation of the localhost.
     */
    private static IData getHost() {
        IData host = IDataFactory.create();
        IDataCursor cursor = host.getCursor();

        try {
            NameHelper.InternetAddress localhost = NameHelper.InternetAddress.localhost();
            cursor.insertAfter("domain", localhost.getDomain());
            cursor.insertAfter("host", localhost.getHost());
            cursor.insertAfter("ip", localhost.getIPAddress());
        } catch(UnknownHostException ex) {
            // do nothing
        } finally {
            cursor.destroy();
        }

        return host;
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
    private static IData getProperties() {
        // sort keys in natural ascending order via a TreeMap
        Map<String, Object> properties = new TreeMap<String, Object>();

        Properties systemProperties = System.getProperties();
        if (systemProperties != null) {
            // protect against concurrent modification exceptions by cloning
            systemProperties = (Properties)systemProperties.clone();

            // remove empty strings from properties object
            for (Map.Entry<Object ,Object> entry : systemProperties.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();

                if (key instanceof String) {
                    if (value != null && !value.equals("")) {
                        properties.put(key.toString(), value);
                    }
                }
            }
        }

        // default the mail.from property if not set
        if (!properties.containsKey("mail.from")) {
            properties.put("mail.from", getDefaultFromEmailAddress());
        }
        
        return new MapIData<String, Object>(properties);
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

    /**
     * An IData representation of the given Calendar which dynamically formats the datetime using the requested key as
     * the datetime pattern.
     */
    private static class DateTimeIData extends AbstractIData {
        /**
         * The datetime this IData represents.
         */
        private Calendar datetime;

        /**
         * Create a new DateTimeIData object.
         *
         * @param datetime  The datetime this object represents.
         */
        public DateTimeIData(Calendar datetime) {
            if (datetime == null) throw new NullPointerException("datetime must not be null");
            this.datetime = datetime;
        }

        /**
         * Returns a cursor for traversing this object.
         *
         * @return a cursor for traversing this object.
         */
        @Override
        public IDataCursor getCursor() {
            return new DateTimeIDataCursor(datetime);
        }

        /**
         * Implements IDataCursor for the DateTimeIData class, treats requested keys as datetime patterns, dynamically
         * returning the wrapped datetime formatted using this pattern as the key's value.
         */
        private static class DateTimeIDataCursor implements IDataCursor {
            private Calendar datetime;
            private String key;
            private Object value;

            public DateTimeIDataCursor(Calendar datetime) {
                if (datetime == null) throw new NullPointerException("datetime must not be null");
                this.datetime = datetime;
            }

            private DateTimeIDataCursor(Calendar datetime, String key, Object value) {
                this.datetime = datetime;
                this.key = key;
                this.value = value;
            }

            @Override
            public void setErrorMode(int errorMode) {
                // do nothing
            }

            @Override
            public DataException getLastError() {
                return null;
            }

            @Override
            public boolean hasMoreErrors() {
                return false;
            }

            @Override
            public void home() {
                // do nothing
            }

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public void setKey(String key) {
                // do nothing
            }

            @Override
            public void setValue(Object value) {
                // do nothing
            }

            @Override
            public boolean delete() {
                return false;
            }

            @Override
            public void insertBefore(String key, Object value) {
                // do nothing
            }

            @Override
            public void insertAfter(String key, Object value) {
                // do nothing
            }

            @Override
            public IData insertDataBefore(String key) {
                return null;
            }

            @Override
            public IData insertDataAfter(String key) {
                return null;
            }

            @Override
            public boolean next() {
                return false;
            }

            /**
             * Sets the current key to the given key, and the value to the formatted datetime using the key as the datetime
             * pattern.
             *
             * @param key   The requested key.
             * @return      True if the key was a valid datetime pattern and the datetime was able to be formatted using it.
             */
            private boolean get(String key) {
                boolean result = true;
                try {
                    this.key = key;
                    this.value = DateTimeHelper.emit(datetime, key);
                } catch(Exception ex) {
                    result = false;
                }
                return result;
            }

            @Override
            public boolean next(String key) {
                return get(key);
            }

            @Override
            public boolean previous() {
                return false;
            }

            @Override
            public boolean previous(String key) {
                return get(key);
            }

            @Override
            public boolean first() {
                return false;
            }

            @Override
            public boolean first(String key) {
                return get(key);
            }

            @Override
            public boolean last() {
                return false;
            }

            @Override
            public boolean last(String key) {
                return get(key);
            }

            @Override
            public boolean hasMoreData() {
                return false;
            }

            @Override
            public void destroy() {
                // do nothing
            }

            @Override
            public IDataCursor getCursorClone() {
                return new DateTimeIDataCursor(this.datetime, this.key, this.value);
            }
        }
    }
}
