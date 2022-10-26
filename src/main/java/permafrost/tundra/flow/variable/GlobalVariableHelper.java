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

import com.wm.app.b2b.server.globalvariables.GlobalVariablesManager;
import com.wm.data.IData;
import com.wm.util.GlobalVariables;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.server.NodeHelper;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A collection of convenience methods for dealing with global variables.
 */
public final class GlobalVariableHelper {
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
                GlobalVariablesManager globalVariablesManager = GlobalVariablesManager.getInstance();
                exists = globalVariablesManager.globalVariableExists(key);
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
                GlobalVariablesManager globalVariablesManager = GlobalVariablesManager.getInstance();
                if (globalVariablesManager.globalVariableExists(key)) {
                    variable = globalVariablesManager.getGlobalVariableValue(key);
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
            GlobalVariablesManager globalVariablesManager = GlobalVariablesManager.getInstance();
            IData[] variables = globalVariablesManager.listGlobalVariables();
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
            GlobalVariablesManager globalVariablesManager = GlobalVariablesManager.getInstance();
            IData[] variables = globalVariablesManager.listGlobalVariables();
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
}
