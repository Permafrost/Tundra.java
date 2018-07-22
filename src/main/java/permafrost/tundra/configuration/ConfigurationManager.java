/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
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

package permafrost.tundra.configuration;

import com.wm.app.b2b.server.Package;
import com.wm.app.b2b.server.ServerAPI;
import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataHjsonParser;
import permafrost.tundra.data.IDataJSONParser;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.data.IDataParser;
import permafrost.tundra.data.IDataPropertiesParser;
import permafrost.tundra.data.IDataXMLParser;
import permafrost.tundra.data.IDataYAMLParser;
import permafrost.tundra.flow.variable.SubstitutionHelper;
import permafrost.tundra.flow.variable.SubstitutionType;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.io.filter.AndFilenameFilter;
import permafrost.tundra.io.filter.FileFilenameFilter;
import permafrost.tundra.io.filter.RegularExpressionFilenameFilter;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.server.PackageHelper;
import permafrost.tundra.server.SystemHelper;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Integration Server package configuration manager.
 */
public class ConfigurationManager {
    /**
     * Directory containing server-specific package configuration files.
     */
    private static final File SERVER_PACKAGE_CONFIGURATION_DIRECTORY = new File(ServerAPI.getServerConfigDir(), "packages");

    /**
     * Regular expression pattern to match the supported configuration filename extensions.
     */
    private static final String PACKAGE_CONFIGURATION_EXTENSIONS_PATTERN = "(hjson|json|properties|values|xml|yaml)";

    /**
     * Regular expression pattern to match package level configuration files.
     */
    private static final Pattern PACKAGE_CONFIGURATION_PATTERN = Pattern.compile("package\\." + PACKAGE_CONFIGURATION_EXTENSIONS_PATTERN);

    /**
     * Cache of package configurations.
     */
    private static final ConcurrentMap<String, IData> CONFIGURATIONS = new ConcurrentHashMap<String, IData>();

    /**
     * Disallow instantiation of this class.
     */
    private ConfigurationManager() {}

    /**
     * Returns the configuration settings for the package containing the invoking service.
     *
     * @return                  The configuration settings for the package containing the invoking service.
     * @throws IOException      If an error occurs reading the configuration files.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData get() throws IOException, ServiceException {
        return get(null, false);
    }

    /**
     * Returns the the configuration settings for the package with the given name.
     *
     * @param packageName       The name of the package whose configuration is to be returned.
     * @return                  The configuration settings associated with the named package.
     * @throws IOException      If an error occurs reading the configuration files.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData get(String packageName) throws IOException, ServiceException {
        return get(packageName, false);
    }

    /**
     * Returns the the configuration settings for the package with the given name.
     *
     * @param packageName       The name of the package whose configuration is to be returned.
     * @param refresh           If true, the package configuration is refreshed from disk.
     * @return                  The configuration settings associated with the named package.
     * @throws IOException      If an error occurs reading the configuration files.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData get(String packageName, boolean refresh) throws IOException, ServiceException {
        return get(packageName, refresh, true);
    }

    /**
     * Returns the the configuration settings for the package with the given name.
     *
     * @param packageName       The name of the package whose configuration is to be returned.
     * @param refresh           If true, the package configuration is refreshed from disk.
     * @param updateCache       If true, and package configuration is refreshed from disk, the cache is updated with
     *                          the refreshed configuration.
     * @return                  The configuration settings associated with the named package.
     * @throws IOException      If an error occurs reading the configuration files.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData get(String packageName, boolean refresh, boolean updateCache) throws IOException, ServiceException {
        IData configuration;

        if (PackageHelper.isEnabled(packageName)) {
            configuration = CONFIGURATIONS.get(packageName);

            if (refresh || configuration == null) {
                File[] packageConfigFiles = ServerAPI.getPackageConfigDir(packageName).listFiles(new AndFilenameFilter(new RegularExpressionFilenameFilter(PACKAGE_CONFIGURATION_PATTERN), FileFilenameFilter.getInstance()));

                // sanitize packageName for file system access
                String sanitizedPackageName = packageName.replaceAll("\\W", "_");
                Pattern serverPackagePattern = Pattern.compile(Pattern.quote(sanitizedPackageName) + "\\." + PACKAGE_CONFIGURATION_EXTENSIONS_PATTERN);
                File[] serverConfigFiles = SERVER_PACKAGE_CONFIGURATION_DIRECTORY.listFiles(new AndFilenameFilter(new RegularExpressionFilenameFilter(serverPackagePattern), FileFilenameFilter.getInstance()));

                configuration = merge(ArrayHelper.concatenate(packageConfigFiles, serverConfigFiles));

                if (IDataHelper.size(configuration) > 0) {
                    // support substituting system environment and java properties using the $system structure
                    IDataMap scope = new IDataMap();
                    scope.put("$system", SystemHelper.reflect(refresh));

                    // substitute %key% strings with the associated global variable or other configuration values if they exist
                    configuration = SubstitutionHelper.substitute(configuration, null, true, EnumSet.of(SubstitutionType.LOCAL), scope, configuration);

                    // cache the configuration in-memory to optimise performance
                    if (updateCache) CONFIGURATIONS.put(packageName, configuration);
                } else {
                    // remove old configuration from cache as it has been replaced with an empty configuration
                    CONFIGURATIONS.remove(packageName);
                }
            }
        } else {
            // clean up cached configuration for disabled packages
            CONFIGURATIONS.remove(packageName);
            throw new ServiceException("package does not exist or is disabled: " + packageName);
        }

        return IDataUtil.deepClone(configuration);
    }

    /**
     * Returns the given package's configuration stored on disk, without updating the configuration cache.
     *
     * @param packageName       The name of the package whose configuration is to be returned.
     * @return                  The configuration settings associated with the named package.
     * @throws IOException      If an error occurs reading the configuration files.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData peek(String packageName) throws IOException, ServiceException {
        return get(packageName, true, false);
    }

    /**
     * Returns all package configurations.
     *
     * @return                  The configuration settings associated with all configured packages.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData all() throws ServiceException {
        return all(false);
    }

    /**
     * Returns all package configurations.
     *
     * @param refresh           If true, the package configurations are refreshed from disk.
     * @return                  The configuration settings associated with all configured packages.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData all(boolean refresh) throws ServiceException {
        return all(refresh, true);
    }

    /**
     * Returns all package configurations.
     *
     * @param refresh           If true, the package configurations are refreshed from disk.
     * @param updateCache       If true, and package configuration is refreshed from disk, the cache is updated with
     *                          the refreshed configuration.
     * @return                  The configuration settings associated with all configured packages.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData all(boolean refresh, boolean updateCache) throws ServiceException {
        Package[] packages = PackageHelper.list(true);

        IDataMap output = new IDataMap();

        for (Package pkg : packages) {
            try {
                String packageName = pkg.getName();
                output.put(packageName, get(packageName, refresh, updateCache));
            } catch(IOException exception) {
                // ignore unparseable configurations when producing list
                ServerAPI.logError(exception);
            }
        }

        return output;
    }

    /**
     * Returns a list of all package configurations.
     *
     * @return                  The configuration settings associated with all configured packages.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData[] list() throws ServiceException {
        return list(false);
    }

    /**
     * Returns a list of all package configurations.
     *
     * @param refresh           If true, the package configurations are refreshed from disk.
     * @return                  The configuration settings associated with all configured packages.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData[] list(boolean refresh) throws ServiceException {
        return list(refresh, true);
    }

    /**
     * Returns a list of all package configurations.
     *
     * @param refresh           If true, the package configurations are refreshed from disk.
     * @param updateCache       If true, and package configuration is refreshed from disk, the cache is updated with
     *                          the refreshed configuration.
     * @return                  The configuration settings associated with all configured packages.
     * @throws ServiceException If an error occurs when substituting global variables.
     */
    public static IData[] list(boolean refresh, boolean updateCache) throws ServiceException {
        Package[] packages = PackageHelper.list(true);

        List<IData> output = new ArrayList<IData>(packages.length);

        for (Package pkg : packages) {
            try {
                String packageName = pkg.getName();

                IDataMap map = new IDataMap();
                map.put("package", packageName);
                map.put("configuration", get(packageName, refresh, updateCache));
                output.add(map);
            } catch(IOException exception) {
                // ignore unparseable configurations when producing list
                ServerAPI.logError(exception);
            }
        }

        return output.toArray(new IData[output.size()]);
    }

    /**
     * Reads the given file and returns an IData representation of its contents.
     *
     * @param file          The file to be read.
     * @return              An IData representation of the file's contents.
     * @throws IOException  If an error occurs reading the file.
     */
    private static IData read(File file) throws IOException {
        IData configuration = null;

        if (file.exists() && file.isFile() && file.canRead()) {
            IDataParser parser = getParser(file);
            if (parser != null) {
                try {
                    configuration = parser.parse(FileHelper.readToStream(file));
                } catch(Throwable exception) {
                    throw new IOException(MessageFormat.format("Unable to parse file: {0}", FileHelper.normalize(file)), exception);
                }
            }
        }

        return configuration;
    }

    /**
     * Reads the given list of files, and returns a merged IData representation of their contents.
     *
     * @param files         The list of files to be read.
     * @return              A merged IData representation of their contents.
     * @throws IOException  If an error occurs reading the files.
     */
    private static IData merge(File ...files) throws IOException {
        IData configuration = null;

        if (files != null) {
            IData[] documents = new IData[files.length];

            for (int i = 0; i < files.length; i++) {
                documents[i] = read(files[i]);
            }

            configuration = IDataHelper.merge(true, documents);
        } else {
            configuration = IDataFactory.create();
        }

        return configuration;
    }

    /**
     * Returns an IDataTextParser that can parse the given file.
     *
     * @param file The file to be parsed.
     * @return     An IDataTextParser that can parse the given file, or null if it cannot be parsed.
     */
    private static IDataParser getParser(File file) {
        IDataParser parser = null;

        String extension = FileHelper.getExtension(file);

        if (extension != null) {
            // TODO: more elegant solution for matching parsers with file extensions
            if (extension.equalsIgnoreCase("hjson")) {
                parser = new IDataHjsonParser();
            } else if (extension.equalsIgnoreCase("json")) {
                parser = new IDataJSONParser();
            } else if (extension.equalsIgnoreCase("properties")) {
                parser = new IDataPropertiesParser();
            } else if (extension.equalsIgnoreCase("values") || extension.equalsIgnoreCase("xml")) {
                parser = new IDataXMLParser();
            } else if (extension.equalsIgnoreCase("yaml")) {
                parser = new IDataYAMLParser();
            }
        }

        return parser;
    }
}
