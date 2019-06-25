/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Lachlan Dowding
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

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.User;
import com.wm.app.b2b.server.UserManager;

/**
 * Convenience methods for working with Integration Server user objects.
 */
public class UserHelper {
    /**
     * Disallow instantiation of this class.
     */
    private UserHelper() {}

    /**
     * Returns the user associated with the given name.
     *
     * @param name  The name of the user.
     * @return      The user associated with the given name.
     */
    public static User get(String name) {
        return UserManager.getUser(name);
    }

    /**
     * Returns the current user, or null if there is no current user.
     *
     * @return the current user, or null if there is no current user.
     */
    public static User getCurrent() {
        User currentUser = null;
        InvokeState currentState = InvokeState.getCurrentState();
        if (currentState != null) {
            currentUser = currentState.getUser();
        }

        return currentUser;
    }

    /**
     * Returns the current user name, or null if there is no current user.
     *
     * @return the current user name, or null if there is no current user.
     */
    public static String getCurrentName() {
        String currentName = null;
        User currentUser = getCurrent();

        if (currentUser != null) {
            currentName = currentUser.getName();
        }

        return currentName;
    }
}
