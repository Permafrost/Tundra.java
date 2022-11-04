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
import com.wm.app.b2b.server.Group;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.UserManager;
import permafrost.tundra.lang.ExceptionHelper;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Collection of convenience methods for working with Integration Server groups.
 */
public final class GroupHelper {
    /**
     * Disallow instantiation of this class.
     */
    private GroupHelper() {}

    /**
     * Creates a new local group with the given name unless it already exists.
     *
     * @param name              The group name.
     * @throws ServiceException If the group cannot be created.
     */
    public static synchronized void create(String name) throws ServiceException {
        try {
            if (name != null) {
                int index = name.indexOf('/');
                if (index == -1 || name.startsWith("local/")) {
                    if (index >= 0) {
                        name = name.substring(index + 1);
                    }
                    UserManager.addGroup(name);
                }
            }
        } catch (Exception ex) {
            ExceptionHelper.raise(ex);
        }
    }

    /**
     * Returns the group with the given name if it exists, otherwise creates a new group with the given name.
     *
     * @param name              The group name.
     * @return                  Either the existing group with the given name, or a newly created group with the name.
     * @throws ServiceException If the group cannot be created.
     */
    public static synchronized Group findOrCreate(String name) throws ServiceException {
        Group group = get(name);
        if (group == null) {
            create(name);
            group = get(name);
        }
        return group;
    }

    /**
     * Returns the groups with the given names if they exists, otherwise creates new groups with the given names.
     *
     * @param names             The group names.
     * @return                  Either the existing groups with the given names, or newly created groups with the names.
     * @throws ServiceException If a group cannot be created.
     */
    public static synchronized Group[] findOrCreate(String[] names) throws ServiceException {
        if (names == null) return null;

        Group[] groups = new Group[names.length];

        for(int i = 0; i < names.length; i++) {
            groups[i] = findOrCreate(names[i]);
        }

        return groups;
    }

    /**
     * Returns true if a group with the given name exists.
     *
     * @param name  The group name.
     * @return      True if a group with the given name exists.
     */
    public static boolean exists(String name) {
        return get(name) != null;
    }

    /**
     * Returns the group with the given name.
     *
     * @param name  The group name.
     * @return      The group with the given name, or null if no group with the given name exists.
     */
    public static Group get(String name) {
        return UserManager.getGroup(ACLGroup.cleanGroupName(name), true);
    }

    /**
     * Returns the list of groups that correspond to the given list of names.
     *
     * @param names The list of group names.
     * @return      The list of groups that correspond to the given list of names.
     */
    public static Group[] get(String[] names) {
        if (names == null) return null;

        Group[] groups = new Group[names.length];

        for(int i = 0; i < names.length; i++) {
            groups[i] = get(names[i]);
        }

        return groups;
    }

    /**
     * Returns the list of all groups defined on this Integration Server.
     * @return The list of all groups defined on this Integration Server.
     */
    public static Group[] list() {
        Enumeration enumeration = UserManager.listGroups(true);
        SortedSet<Group> set = new TreeSet<Group>(new GroupComparator());
        while(enumeration.hasMoreElements()) {
            Object element = enumeration.nextElement();
            if (element instanceof String) {
                Group group = get((String)element);
                if (group != null) {
                    set.add(group);
                }
            }
        }
        return set.toArray(new Group[0]);
    }

    /**
     * Compares instance of Group class.
     */
    private static class GroupComparator implements Comparator<Group> {
        /**
         * Compares two instances of the Group class.
         *
         * @param thisGroup  The first object to be compared.
         * @param otherGroup The second object to be compared.
         * @return           The result of the comparison.
         */
        @Override
        public int compare(Group thisGroup, Group otherGroup) {
            return thisGroup.getName().compareTo(otherGroup.getName());
        }
    }
}
