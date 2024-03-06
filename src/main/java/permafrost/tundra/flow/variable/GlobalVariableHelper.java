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

package permafrost.tundra.flow.variable;

import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.globalvariables.GlobalVariablesException;
import com.wm.app.b2b.server.globalvariables.GlobalVariablesManager;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.util.GlobalVariables;
import permafrost.tundra.data.IDataCursorHelper;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataHjsonParser;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.data.IDataParser;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.StringHelper;
import permafrost.tundra.server.NodeHelper;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A collection of convenience methods for dealing with global variables.
 */
public final class GlobalVariableHelper {
    /**
     * Whether the global variable feature is supported by this Integration Server.
     */
    private static final boolean isSupported = NodeHelper.exists("wm.server.globalvariables:getGlobalVariableValue");
    /**
     * Whether package-level global variables are supported by this Integration Server.
     */
    private static boolean isPackageLevelSupported = true;

    static {
        try {
            Class.forName("com.wm.app.b2b.server.globalvariables.PackageVariablesManager");
        } catch(Exception ex) {
            isPackageLevelSupported = false;
        }
    }

    /**
     * Disallow instantiation of this class.
     */
    private GlobalVariableHelper() {}

    /**
     * Returns true if this Integration Server version supports global variables.
     *
     * @return True if this Integration Server version supports global variables.
     */
    public static boolean isSupported() {
        return isSupported;
    }

    /**
     * Returns true if this Integration Server version supports package-level global variables.
     *
     * @return True if this Integration Server version supports package-level global variables.
     */
    public static boolean isPackageLevelSupported() {
        return isPackageLevelSupported;
    }

    /**
     * The GlobalVariablesManager.getGlobalVariableValue(String, String) reflected method, if it exists.
     */
    private static Method GLOBALVARIABLESMANAGER_GETGLOBALVARIABLEVALUE_METHOD;
    static {
        try {
            GLOBALVARIABLESMANAGER_GETGLOBALVARIABLEVALUE_METHOD = GlobalVariablesManager.class.getDeclaredMethod("getGlobalVariableValue", String.class, String.class);
        } catch(Throwable ex) {
            GLOBALVARIABLESMANAGER_GETGLOBALVARIABLEVALUE_METHOD = null;
        }
    }

    /**
     * Returns true if a global variable with given key exists.
     *
     * @param key           The key to check existence of.
     * @return              True if a global variable with the given key exists.
     */
    public static boolean exists(String key) {
        return exists(null, key);
    }

    /**
     * Returns true if a global variable with given key exists.
     *
     * @param packageName   The optional name of the package this global variable is scoped to.
     * @param key           The key to check existence of.
     * @return              True if a global variable with the given key exists.
     */
    public static boolean exists(String packageName, String key) {
        return getVariable(packageName, key) != null;
    }

    /**
     * Returns the global variable value associated with the given key.
     *
     * @param key               The key for the value to be returned.
     * @return                  The global variable value associated with the given key.
     */
    public static String get(String key) {
        return get(null, key);
    }

    /**
     * Returns the global variable value associated with the given key.
     *
     * @param packageName       Optional name of package in which the global variable key is scoped.
     * @param key               The key for the value to be returned.
     * @return                  The global variable value associated with the given key.
     */
    public static String get(String packageName, String key) {
        GlobalVariableElement variable = getVariable(packageName, key);
        return variable == null ? null : variable.getValue();
    }

    /**
     * Returns a GlobalVariableElement object representation of the global variable associated with the given key.
     *
     * @param key The key for the value to be returned.
     * @return    The global variable object associated with the given key.
     */
    public static GlobalVariableElement getVariable(String packageName, String key) {
        GlobalVariables.GlobalVariableValue variable = null;
        if (isSupported() && key != null) {
            try {
                GlobalVariablesManager manager = GlobalVariablesManager.getInstance();
                if (isPackageLevelSupported()) {
                    variable = (GlobalVariables.GlobalVariableValue)GLOBALVARIABLESMANAGER_GETGLOBALVARIABLEVALUE_METHOD.invoke(manager, packageName, key);
                } else {
                    variable = manager.getGlobalVariableValue(key);
                }
            } catch(InvocationTargetException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof GlobalVariablesException) {
                    if (cause.getMessage().contains("ISS.0146.9004")) {
                        // ignore exception for variables that do not exist
                    } else {
                        ExceptionHelper.raiseUnchecked(cause);
                    }
                }
            } catch(GlobalVariablesException ex) {
                if (ex.getMessage().contains("ISS.0146.9004")) {
                    // ignore exception for variables that do not exist
                } else {
                    ExceptionHelper.raiseUnchecked(ex);
                }
            } catch(Exception ex) {
                ExceptionHelper.raiseUnchecked(ex);
            }
        }
        return variable == null ? null : new GlobalVariableElement(packageName, key, variable);
    }

    /**
     * Returns all global variables as an IData document.
     *
     * @return                  All global variables as an IData document.
     */
    public static IData list() {
        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        try {
            if (isSupported()) {
                Map<String, IDataCursor> packageVariables = new TreeMap<String, IDataCursor>();

                GlobalVariablesManager manager = GlobalVariablesManager.getInstance();
                IData[] variables = manager.listGlobalVariables();
                for (IDataMap variable : IDataMap.of(variables)) {
                    if (variable != null) {
                        String packageName = (String)variable.get("packageName");
                        String key = (String)variable.get("key");
                        String value = (String)variable.get("value");
                        String isSecure = (String)variable.get("isSecure");

                        if (key != null) {
                            if (isSecure != null && isSecure.equals("true")) {
                                value = get(packageName, key);
                            }

                            if (packageName == null) {
                                cursor.insertAfter(key, value);
                            } else {
                                IDataCursor packageCursor = packageVariables.get(packageName);
                                if (packageCursor == null) {
                                    IData packageVars = IDataFactory.create();
                                    packageCursor = packageVars.getCursor();
                                    packageVariables.put(packageName, packageCursor);
                                    cursor.insertAfter(packageName, packageVars);
                                }
                                packageCursor.insertAfter(key, value);
                            }
                        }
                    }
                }

                for (IDataCursor packageCursor : packageVariables.values()) {
                    packageCursor.destroy();
                }
            }
        } finally {
            cursor.destroy();
        }

        return output;
    }

    /**
     * Returns all global variables as a GlobalVariableElement[] array.
     *
     * @return                  All global variables as a GlobalVariableElement[] array.
     */
    public static GlobalVariableElement[] listVariables() {
        SortedSet<GlobalVariableElement> set = new TreeSet<GlobalVariableElement>();

        if (isSupported()) {
            GlobalVariablesManager manager = GlobalVariablesManager.getInstance();
            IData[] variables = manager.listGlobalVariables();
            for (IDataMap document : IDataMap.of(variables)) {
                if (document != null) {
                    String packageName = (String)document.get("packageName");
                    String key = (String)document.get("key");
                    GlobalVariableElement variable = getVariable(packageName, key);
                    if (variable != null) {
                        set.add(variable);
                    }
                }
            }
        }

        return set.toArray(new GlobalVariableElement[0]);
    }

    /**
     * Lists all global variables as an IData[].
     * @return all global variables as an IData[].
     */
    public static IData[] listVariablesAsIDataArray() {
        GlobalVariableElement[] array = GlobalVariableHelper.listVariables();
        List<IData> list = new ArrayList<IData>(array.length);

        for (GlobalVariableElement item : array) {
            if (item != null) {
                list.add(item.toIData());
            }
        }

        return list.toArray(new IData[0]);
    }

    /**
     * Exports all global variables as a JSON string.
     * @return all global variables as a JSON string.
     */
    public static String export() throws ServiceException {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();

        IDataParser parser = new IDataHjsonParser(true);
        String export = null;
        try {
            cursor.insertAfter("recordWithNoID", listVariablesAsIDataArray());
            export = StringHelper.trim(parser.emit(document, null, String.class));
        } catch(IOException ex) {
            ExceptionHelper.raise(ex);
        } finally {
            cursor.destroy();
        }

        return export;
    }

    /**
     * Merges all global variables specified in the given JSON string into this
     * Integration Server.
     *
     * @param importJSON A JSON string containing global variables, created or
     *                   compatible with the export method.
     */
    public static void merge(String importJSON) throws ServiceException {
        IDataHjsonParser parser = new IDataHjsonParser(true);
        try {
            IData document = parser.parse(importJSON);
            IDataCursor cursor = document.getCursor();
            try {
                IData[] documents = IDataCursorHelper.get(cursor, IData[].class, "recordWithNoID");
                merge(documents);
            } finally {
                cursor.destroy();
            }
        } catch(IOException ex) {
            ExceptionHelper.raise(ex);
        }
    }

    /**
     * Merges the given list of global variables into this Integration Server.
     *
     * @param documents The list of global variables to be merged.
     */
    private static void merge(IData[] documents) throws ServiceException {
        if (documents != null) {
            List<Throwable> exceptions = new LinkedList<Throwable>();
            for (IData document : documents) {
                try {
                    merge(document);
                } catch(Throwable ex) {

                    // TODO: remove the next line, it is temporary only
                    ExceptionHelper.raiseUnchecked(ex);
                    exceptions.add(ex);
                }
            }

            if (exceptions.size() > 0) {
                ExceptionHelper.raise(exceptions);
            }
        }
    }

    /**
     * Merges the given IData representation of a global variable into this
     * Integration Server by either creating or updating it if it already
     * exists.
     *
     * @param document  The global variable to be merged.
     */
    private static void merge(IData document) throws ServiceException {
        if (document != null) {
            IDataCursor cursor = document.getCursor();
            try {
                String packageName = IDataHelper.get(cursor, "package", String.class);
                String key = IDataHelper.get(cursor, "key", String.class);
                String value = IDataHelper.get(cursor, "value", String.class);
                boolean secured = IDataHelper.getOrDefault(cursor, "secured", Boolean.class, false);

                merge(packageName, key, value, secured);
            } catch (Exception ex) {
                ExceptionHelper.raise(ex);
            } finally {
                cursor.destroy();
            }
        }
    }

    /**
     * Merges the given global variable into this Integration Server by
     * creating it if it does not exist, otherwise updating the existing global
     * variable with the same key.
     *
     * @param variable    The global variable to be merged.
     */
    public static void merge(GlobalVariableElement variable) throws ServiceException {
        if (variable != null) {
            merge(variable.getPackageName(), variable.getKey(), variable.getValue(), variable.isSecure());
        }
    }

    /**
     * The GlobalVariablesManager.addGlobalVariable(String, String, String, boolean) reflected method, if it exists.
     */
    private static Method GLOBALVARIABLESMANAGER_ADDGLOBALVARIABLE_METHOD;
    static {
        try {
            GLOBALVARIABLESMANAGER_ADDGLOBALVARIABLE_METHOD = GlobalVariablesManager.class.getDeclaredMethod("addGlobalVariable", String.class, String.class, String.class, boolean.class);
        } catch(Throwable ex) {
            GLOBALVARIABLESMANAGER_ADDGLOBALVARIABLE_METHOD = null;
        }
    }

    /**
     * The GlobalVariablesManager.editGlobalVariable(String, String, String, boolean) reflected method, if it exists.
     */
    private static Method GLOBALVARIABLESMANAGER_EDITGLOBALVARIABLE_METHOD;
    static {
        try {
            GLOBALVARIABLESMANAGER_EDITGLOBALVARIABLE_METHOD = GlobalVariablesManager.class.getDeclaredMethod("editGlobalVariable", String.class, String.class, String.class, boolean.class);
        } catch(Throwable ex) {
            GLOBALVARIABLESMANAGER_EDITGLOBALVARIABLE_METHOD = null;
        }
    }

    /**
     * Merges the given global variable key and value into this Integration
     * Server by creating it if it does not exist, otherwise updating the
     * existing global variable with the same key.
     *
     * @param packageName       The optional name of the package this global variable is scoped to.
     * @param key               The global variable key.
     * @param value             The global variable value.
     * @param secured           Whether the global variable is secret and should
     *                          be secured.
     * @throws ServiceException If an error occurs.
     */
    public static void merge(String packageName, String key, String value, boolean secured) throws ServiceException {
        try {
            GlobalVariablesManager manager = GlobalVariablesManager.getInstance();
            GlobalVariableElement existingVariable = getVariable(packageName, key);
            if (existingVariable == null) {
                if (isPackageLevelSupported()) {
                    GLOBALVARIABLESMANAGER_ADDGLOBALVARIABLE_METHOD.invoke(manager, packageName, key, value, secured);
                } else {
                    manager.addGlobalVariable(key, value, secured);
                }
            } else {
                if (isPackageLevelSupported()) {
                    GLOBALVARIABLESMANAGER_EDITGLOBALVARIABLE_METHOD.invoke(manager, packageName, key, value, secured);
                } else {
                    manager.editGlobalVariable(key, value, secured);
                }
            }
        } catch(Exception ex) {
            ExceptionHelper.raise(ex);
        }
    }
}
