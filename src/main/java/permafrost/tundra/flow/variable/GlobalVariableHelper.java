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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A collection of convenience methods for dealing with global variables.
 */
public final class GlobalVariableHelper {
    /**
     * Whether the global variable feature is supported by this Integration
     * Server.
     */
    private static final boolean isSupported = NodeHelper.exists("wm.server.globalvariables:getGlobalVariableValue");

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
     * Returns true if a global variable with given key exists.
     *
     * @param key   The key to check existence of.
     * @return      True if a global variable with the given key exists.
     */
    public static boolean exists(String key) {
        boolean exists = false;

        if (isSupported() && key != null) {
            try {
                GlobalVariablesManager manager = GlobalVariablesManager.getInstance();
                exists = manager.globalVariableExists(key);
            } catch (Exception ex) {
                ExceptionHelper.raiseUnchecked(ex);
            }
        }

        return exists;
    }

    /**
     * Returns the global variable value associated with the given key.
     *
     * @param key               The key for the value to be returned.
     * @return                  The global variable value associated with the given key.
     */
    public static String get(String key) {
        GlobalVariableElement variable = getVariable(key);
        return variable == null ? null : variable.getValue();
    }

    /**
     * Returns a GlobalVariableElement object representation of the global variable associated with the given key.
     *
     * @param key The key for the value to be returned.
     * @return    The global variable object associated with the given key.
     */
    public static GlobalVariableElement getVariable(String key) {
        GlobalVariables.GlobalVariableValue variable = null;
        if (isSupported() && key != null) {
            try {
                GlobalVariablesManager manager = GlobalVariablesManager.getInstance();
                if (manager.globalVariableExists(key)) {
                    variable = manager.getGlobalVariableValue(key);
                }
            } catch (Exception ex) {
                ExceptionHelper.raiseUnchecked(ex);
            }
        }
        return variable == null ? null : new GlobalVariableElement(key, variable);
    }

    /**
     * Returns all global variables as an IData document.
     *
     * @return                  All global variables as an IData document.
     */
    public static IData list() {
        IDataMap output = new IDataMap();

        if (isSupported()) {
            GlobalVariablesManager manager = GlobalVariablesManager.getInstance();
            IData[] variables = manager.listGlobalVariables();
            for (IDataMap variable : IDataMap.of(variables)) {
                if (variable != null) {
                    String key = (String)variable.get("key");
                    String value = (String)variable.get("value");
                    String isSecure = (String)variable.get("isSecure");

                    if (key != null) {
                        if (isSecure != null && isSecure.equals("true")) {
                            value = get(key);
                        }

                        output.put(key, value);
                    }
                }
            }
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
                    String key = (String)document.get("key");
                    GlobalVariableElement variable = getVariable(key);
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
                String key = IDataHelper.get(cursor, "key", String.class);
                String value = IDataHelper.get(cursor, "value", String.class);
                boolean secured = IDataHelper.getOrDefault(cursor, "secured", Boolean.class, false);

                merge(key, value, secured);
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
            merge(variable.getKey(), variable.getValue(), variable.isSecure());
        }
    }

    /**
     * Merges the given global variable key and value into this Integration
     * Server by creating it if it does not exist, otherwise updating the
     * existing global variable with the same key.
     *
     * @param key               The global variable key.
     * @param value             The global variable value.
     * @param secured           Whether the global variable is secret and should
     *                          be secured.
     * @throws ServiceException If an error occurs.
     */
    public static void merge(String key, String value, boolean secured) throws ServiceException {
        try {
            GlobalVariablesManager manager = GlobalVariablesManager.getInstance();
            GlobalVariableElement existingVariable = GlobalVariableHelper.getVariable(key);
            if (existingVariable == null) {
                manager.addGlobalVariable(key, value, secured);
            } else {
                manager.editGlobalVariable(key, value, secured);
            }
        } catch(Exception ex) {
            ExceptionHelper.raise(ex);
        }
    }
}
