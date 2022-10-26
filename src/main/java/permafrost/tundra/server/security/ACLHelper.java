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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Collection of convenience methods for working with Integration Server ACLs.
 */
public final class ACLHelper {
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
     * @param force If false an existing ACL with the given name will not be replaced or updated.
     */
    public static synchronized ACLGroup create(String name, Group[] allow, Group[] deny, boolean force) {
        return createOrMerge(name, allow, deny, force, false);
    }

    /**
     * Creates or merges an ACL with the given name.
     *
     * @param name  The ACL name.
     * @param allow The groups allowed access to this ACL.
     * @param deny  The groups denied access to this ACL.
     * @param force If false an existing ACL with the given name will not be replaced or updated.
     * @param merge If true, existing allow and deny groups will be merged with the given allow and deny lists,
     *              otherwise if false they will be replaced.
     */
    public static synchronized ACLGroup createOrMerge(String name, Group[] allow, Group[] deny, boolean force, boolean merge) {
        ACLGroup acl = null;
        if (name != null && (force || !exists(name))) {
            if (merge && exists(name)) {
                acl = get(name);
                allow = mergeGroupLists(allow, getAllowList(acl));
                deny = mergeGroupLists(deny, getDenyList(acl));
            }

            acl = ACLManager.addGroup(name, allow, deny);
        }
        return acl;
    }

    /**
     * Returns the ACL with the given name, creating it first if it does not yet exist.
     *
     * @param name  The name of the ACL.
     * @return      The ACL with the given name.
     */
    public static synchronized ACLGroup findOrCreate(String name) {
        if (!exists(name)) {
            create(name, null, null, false);
        }
        return get(name);
    }

    /**
     * Returns the list of allowed groups for the given ACL.
     *
     * @param acl   The ACL whose allowed groups are to be returned.
     * @return      The list of allowed groups from the given ACL.
     */
    public static Group[] getAllowList(ACLGroup acl) {
        return getGroupList(acl.getAllowList());
    }

    /**
     * Returns the list of denied groups for the given ACL.
     *
     * @param acl   The ACL whose denied groups are to be returned.
     * @return      The list of denied groups from the given ACL.
     */
    public static Group[] getDenyList(ACLGroup acl) {
        return getGroupList(acl.getDenyList());
    }

    /**
     * Returns a Group[] constructed from the given Enumeration of group names.
     *
     * @param enumeration   An enumeration containing group names.
     * @return              The resulting Group[] array.
     */
    private static Group[] getGroupList(Enumeration enumeration) {
        SortedSet<String> set = new TreeSet<String>();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object element = enumeration.nextElement();
                if (element instanceof String) {
                    set.add((String)element);
                }
            }
        }

        List<Group> groups = new ArrayList<Group>(set.size());
        for (String name : set) {
            groups.add(GroupHelper.get(name));
        }

        return groups.toArray(new Group[0]);
    }

    /**
     * Merges the given Group[] arrays together into a single Group[] array that does not contain duplicates.
     *
     * @param groupLists    The list of Group[] arrays to be merged.
     * @return              The merged Group[] array.
     */
    private static Group[] mergeGroupLists(Group[]... groupLists) {
        Set<String> set = new TreeSet<String>();
        if (groupLists != null) {
            for (Group[] groupList : groupLists) {
                if (groupList != null) {
                    for (Group group : groupList) {
                        if (group != null) {
                            set.add(group.getName());
                        }
                    }
                }
            }
        }

        List<Group> groups = new ArrayList<Group>(set.size());
        for (String name : set) {
            groups.add(GroupHelper.get(name));
        }

        return groups.toArray(new Group[0]);
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

    /**
     * Returns the list of all groups defined on this Integration Server.
     * @return The list of all groups defined on this Integration Server.
     */
    public static ACLGroup[] list() {
        Enumeration enumeration = ACLManager.groupKeys();
        SortedSet<ACLGroup> set = new TreeSet<ACLGroup>(new ACLGroupComparator());
        while (enumeration.hasMoreElements()) {
            Object element = enumeration.nextElement();
            if (element instanceof String) {
                String name = (String)element;
                if (name != null) {
                    ACLGroup group = ACLManager.getGroup(name);
                    if (group != null) {
                        set.add(group);
                    }
                }
            }
        }
        return set.toArray(new ACLGroup[0]);
    }

    /**
     * Compares instance of ACLGroup class.
     */
    private static class ACLGroupComparator implements Comparator<ACLGroup> {
        /**
         * Compares two instances of the ACLGroup class.
         *
         * @param thisGroup  The first object to be compared.
         * @param otherGroup The second object to be compared.
         * @return           The result of the comparison.
         */
        @Override
        public int compare(ACLGroup thisGroup, ACLGroup otherGroup) {
            return thisGroup.getName().compareTo(otherGroup.getName());
        }
    }
}
