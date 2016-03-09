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
import com.wm.passman.PasswordManager;
import com.wm.security.OutboundPasswordStore;
import com.wm.util.GlobalVariables;
import com.wm.util.security.WmSecureString;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.server.NodeHelper;

/**
 * A collection of convenience methods for dealing with global variables.
 */
public class GlobalVariableHelper {
    private static final boolean isSupported = NodeHelper.exists("wm.server.globalvariables:getGlobalVariableValue");;

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
     * Returns the global variable value associated with the given key.
     *
     * @param key               The key for the value to be returned.
     * @return                  The global variable value associated with the given key.
     * @throws ServiceException If a password decryption or global variable error occurs.
     */
    public static String get(String key) throws ServiceException {
        String value = null;

        if (isSupported() && key != null) {
            try {
                GlobalVariablesManager globalVariablesManager = GlobalVariablesManager.getInstance();

                if (globalVariablesManager.globalVariableExists(key)) {
                    GlobalVariables.GlobalVariableValue variable = globalVariablesManager.getGlobalVariableValue(key);

                    value = variable.getValue();

                    if (variable.isSecure()) {
                        PasswordManager passwordManager = OutboundPasswordStore.getStore();
                        WmSecureString password = passwordManager.retrievePassword(value);
                        value = password.toString();
                    }
                }
            } catch (Exception ex) {
                ExceptionHelper.raise(ex);
            }
        }
        return value;
    }

    /**
     * Returns all global variables as an IData document.
     *
     * @return                  All global variables as an IData document.
     * @throws ServiceException If a password decryption or global variable error occurs.
     */
    public static IData list() throws ServiceException {
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
}
