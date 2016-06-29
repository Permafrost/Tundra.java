/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Lachlan Dowding
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package permafrost.tundra.server.security;

import com.wm.app.b2b.server.ACLGroup;
import com.wm.app.b2b.server.ACLManager;
import com.wm.app.b2b.server.Group;
import java.util.Collection;

/**
 * Collection of convenience methods for working with Integration Server ACLs.
 */
public class ACLHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ACLHelper() {}

    /**
     * Creates an ACL with the given name.
     *
     * @param name  The ACL name.
     * @param allow The groups allowed access to this ACL.
     * @param deny  The groups denied access to this ACL.
     * @param force If false an existing ACL with the given name will not be replace or updated.
     */
    public static void create(String name, Collection<Group> allow, Collection<Group> deny, boolean force) {
        create(name, allow == null ? null : allow.toArray(new Group[allow.size()]), deny == null ? null : deny.toArray(new Group[deny.size()]), force);
    }

    /**
     * Creates an ACL with the given name.
     *
     * @param name  The ACL name.
     * @param allow The groups allowed access to this ACL.
     * @param deny  The groups denied access to this ACL.
     * @param force If false an existing ACL with the given name will not be replace or updated.
     */
    public static synchronized void create(String name, Group[] allow, Group[] deny, boolean force) {
        if (force || !exists(name)) {
            ACLManager.addGroup(name, allow, deny);
        }
    }

    /**
     * Returns true if an ACL with the given name exists.
     *
     * @param name  The ACL name.
     * @return      True if an ACL with the given name exists.
     */
    public static boolean exists(String name) {
        return get(name) != null;
    }

    /**
     * Returns the ACL with the given name if it exists.
     *
     * @param name  The ACL name.
     * @return      The ACL with the given name.
     */
    public static ACLGroup get(String name) {
        return ACLManager.getGroup(name);
    }
}
