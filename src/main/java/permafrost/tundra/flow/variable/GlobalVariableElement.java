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
import com.wm.util.GlobalVariables;
import permafrost.tundra.lang.ExceptionHelper;
import java.lang.reflect.Method;

/**
 * Wrapper for a Global Variable element.
 */
public class GlobalVariableElement implements Comparable<GlobalVariableElement> {
    /**
     * The optional name of the package this global variable is scoped to.
     */
    protected String packageName;
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
    public GlobalVariableElement(String packageName, String key, GlobalVariables.GlobalVariableValue variable) {
        this.packageName = packageName;
        this.key = key;
        this.variable = variable;
    }

    /**
     * Returns the name of the package this global variable is scoped to.
     *
     * @return the name of the package this global variable is scoped to.
     */
    public String getPackageName() {
        return packageName;
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
     * The GlobalVariables.getDecryptedValue(String, String) reflected method, if it exists.
     */
    private static Method GLOBALVARIABLES_GETDECRYPTEDVALUE_METHOD;
    static {
        try {
            GLOBALVARIABLES_GETDECRYPTEDVALUE_METHOD = GlobalVariables.class.getDeclaredMethod("getDecryptedValue", String.class, String.class);
        } catch(Throwable ex) {
            GLOBALVARIABLES_GETDECRYPTEDVALUE_METHOD = null;
        }
    }

    /**
     * Returns this global variable's value, decrypted if necessary.
     *
     * @return this global variable's value, decrypted if necessary.
     */
    public String getValue() {
        String value = variable.getValue();
        if (variable.isSecure()) {
            GlobalVariables variables = GlobalVariables.getInstance();
            if (GlobalVariableHelper.isPackageLevelSupported()) {
                try {
                    value = (String)GLOBALVARIABLES_GETDECRYPTEDVALUE_METHOD.invoke(variables, packageName, key);
                } catch(Exception ex) {
                    ExceptionHelper.raiseUnchecked(ex);
                }
            } else {
                value = variables.getDecryptedValue(key);
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
        } else if (this.packageName == null) {
            if (other.packageName == null) {
                comparison = compareKey(other.key);
            } else {
                comparison = 1;
            }
        } else if (other.packageName == null) {
            comparison = 1;
        } else if (this.packageName.equals(other.packageName)) {
            comparison = compareKey(other.key);
        } else {
            comparison = this.packageName.compareTo(other.packageName);
        }
        return comparison;
    }

    /**
     * Compares this object's key with another key.
     *
     * @param otherKey  The other key to compare to.
     * @return          The result of the comparison.
     */
    private int compareKey(String otherKey) {
        int comparison;
        if (this.key == null) {
            if (otherKey == null) {
                comparison = 0;
            } else {
                comparison = 1;
            }
        } else {
            comparison = this.key.compareTo(otherKey);
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
            if (packageName != null) cursor.insertAfter("package", getPackageName());
            cursor.insertAfter("key", getKey());
            cursor.insertAfter("value", getValue());
            cursor.insertAfter("secured", isSecure());
        } finally {
            cursor.destroy();
        }
        return document;
    }
}
