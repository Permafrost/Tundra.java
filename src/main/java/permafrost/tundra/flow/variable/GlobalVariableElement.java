/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Lachlan Dowding
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

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.passman.PasswordManager;
import com.wm.security.OutboundPasswordStore;
import com.wm.util.GlobalVariables;
import com.wm.util.security.WmSecureString;
import permafrost.tundra.lang.ExceptionHelper;

/**
 * Wrapper for a Global Variable element.
 */
public class GlobalVariableElement implements Comparable<GlobalVariableElement> {
    /**
     * The key this global variable is associated with.
     */
    protected String key;
    /**
     * The value this global variable is associated with.
     */
    protected GlobalVariables.GlobalVariableValue variable;

    /**
     * Constructs a new GlobalVariableElement object.
     *
     * @param key       The key this global variable is associated with.
     * @param variable  The value this global variable is associated with.
     */
    public GlobalVariableElement(String key, GlobalVariables.GlobalVariableValue variable) {
        this.key = key;
        this.variable = variable;
    }

    /**
     * Returns the key this global variable is associated with.
     *
     * @return the key this global variable is associated with.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns this global variable's value, decrypted if necessary.
     *
     * @return this global variable's value, decrypted if necessary.
     */
    public String getValue() {
        String value = variable.getValue();
        if (variable.isSecure()) {
            try {
                PasswordManager passwordManager = OutboundPasswordStore.getStore();
                WmSecureString password = passwordManager.retrievePassword(value);
                if (password == null) {
                    value = null;
                } else {
                    value = password.toString();
                }
            } catch(Exception ex) {
                ExceptionHelper.raiseUnchecked(ex);
            }
        }
        return value;
    }

    /**
     * Returns the underlying GlobalVariables.GlobalVariableValue object this global variable is associated with.
     *
     * @return the underlying GlobalVariables.GlobalVariableValue object this global variable is associated with.
     */
    public GlobalVariables.GlobalVariableValue getVariable() {
        return variable;
    }

    /**
     * Returns true if this global variable is and should be secured.
     *
     * @return true if this global variable is and should be secured.
     */
    public boolean isSecure() {
        return variable.isSecure();
    }

    /**
     * Compares this object with another object.
     *
     * @param other The other object to be compared against.
     * @return      The result of the comparison.
     */
    @Override
    public int compareTo(GlobalVariableElement other) {
        int comparison;
        if (this == other) {
            comparison = 0;
        } else if (other == null) {
            comparison = 1;
        } else if (this.key == null) {
            if (other.key == null) {
                comparison = 0;
            } else {
                comparison = 1;
            }
        } else {
            comparison = this.key.compareTo(other.key);
        }
        return comparison;
    }

    /**
     * Returns an IData representation of this global variable.
     *
     * @return an IData representation of this global variable.
     */
    public IData toIData() {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();

        try {
            cursor.insertAfter("key", getKey());
            cursor.insertAfter("value", getValue());
            cursor.insertAfter("secured", isSecure());
        } finally {
            cursor.destroy();
        }
        return document;
    }
}
