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

package permafrost.tundra.server;

import com.wm.passman.PasswordManager;
import com.wm.security.OutboundPasswordStore;
import com.wm.util.security.WmSecureString;
import permafrost.tundra.lang.ExceptionHelper;

/**
 * Collection of convenience methods for working with Integration Server outbound passwords.
 */
public class OutboundPasswordHelper {
    /**
     * Disallow instantiation of this class.
     */
    private OutboundPasswordHelper() {}

    /**
     * Returns the plaintext password associated with the given outbound password store handle.
     *
     * @param handle    The outbound password store handle for the password to be retrieved.
     * @return          The plaintext password associated with the given handle, or null if not found.
     */
    public static String getPassword(String handle) {
        String password = null;
        try {
            PasswordManager passwordManager = OutboundPasswordStore.getStore();
            WmSecureString secureString = passwordManager.retrievePassword(handle);
            if (secureString != null) {
                password = secureString.toString();
            }
        } catch(Exception ex) {
            ExceptionHelper.raiseUnchecked(ex);
        }
        return password;
    }
}