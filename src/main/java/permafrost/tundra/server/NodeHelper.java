/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lachlan Dowding
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

import com.wm.app.b2b.server.ACLManager;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSInterface;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSType;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for working with webMethods Integration Server namespace nodes.
 */
public class NodeHelper {
    /**
     * Disallow instantiation of this class.
     */
    private NodeHelper() {}

    /**
     * Returns the webMethods Integration Server namespace node type for the given node name.
     *
     * @param name The node name to return the type of.
     * @return The type of the given node.
     */
    public static NSType getNodeType(String name) {
        return getNodeType(getNode(name));
    }

    /**
     * Returns the webMethods Integration Server namespace node type for the given node.
     *
     * @param node The node to return the type of.
     * @return The type of the given node.
     */
    public static NSType getNodeType(NSNode node) {
        if (node == null) return null;
        return node.getNodeTypeObj();
    }

    /**
     * Returns the webMethods Integration Server namespace name for the given String.
     *
     * @param name The String to be converted to a namespace name.
     * @return The namespace name representing the given String.
     */
    public static NSName getName(String name) {
        if (name == null) return null;
        return NSName.create(name);
    }

    /**
     * Returns the webMethods Integration Server namespace node with the given name.
     *
     * @param name The name to return the corresponding node for.
     * @return The namespace node representing the given name.
     */
    public static NSNode getNode(String name) {
        return getNode(getName(name));
    }

    /**
     * Returns the webMethods Integration Server namespace node with the given name.
     *
     * @param name The name to return the corresponding node for.
     * @return The namespace node representing the given name.
     */
    public static NSNode getNode(NSName name) {
        if (name == null) return null;
        return Namespace.current().getNode(name);
    }

    /**
     * Returns the webMethods Integration Server namespace interface with the given name.
     *
     * @param name The name to return the corresponding interface for.
     * @return The namespace interface representing the given name.
     */
    public static NSInterface getInterface(String name) {
        return getInterface(getName(name));
    }

    /**
     * Returns the webMethods Integration Server namespace interface with the given name.
     *
     * @param name The name to return the corresponding interface for.
     * @return The namespace interface representing the given name.
     */
    public static NSInterface getInterface(NSName name) {
        NSNode node = getNode(name);
        return node instanceof NSInterface ? (NSInterface)node : null;
    }

    /**
     * Returns true if the given webMethods Integration Server namespace node exists.
     *
     * @param name The namespace node to check existence of.
     * @return True if the given webMethods Integration Server namespace node exists.
     */
    public static boolean exists(String name) {
        return exists(getName(name));
    }

    /**
     * Returns true if the given webMethods Integration Server namespace node exists.
     *
     * @param name The namespace node to check existence of.
     * @return True if the given webMethods Integration Server namespace node exists.
     */
    public static boolean exists(NSName name) {
        return name != null && Namespace.current().nodeExists(name);
    }

    /**
     * Returns a list of all the child leaf nodes defined under the root interface on this server.
     *
     * @param pattern The regular expression pattern used to filter the returned nodes by name; if null all nodes are
     *                returned.
     * @param type    The type of node to be returned; if null all node types are returned.
     * @param recurse Whether to recursively list child interfaces.
     * @return A sorted set of child nodes.
     */
    public static SortedSet<String> listRoot(String pattern, String type, boolean recurse) {
        return listRoot(pattern == null ? null : Pattern.compile(pattern), type, recurse);
    }

    /**
     * Returns a list of all the child leaf nodes defined under the root interface on this server.
     *
     * @param pattern The regular expression pattern used to filter the returned nodes by name; if null all nodes are
     *                returned.
     * @param type    The type of node to be returned; if null all node types are returned.
     * @param recurse Whether to recursively list child interfaces.
     * @return A sorted set of child nodes.
     */
    public static SortedSet<String> listRoot(Pattern pattern, String type, boolean recurse) {
        return listRoot(pattern, NSType.create(type), recurse);
    }

    /**
     * Returns a list of all the child leaf nodes defined under the root interface on this server.
     *
     * @param pattern The regular expression pattern used to filter the returned nodes by name; if null all nodes are
     *                returned.
     * @param type    The type of node to be returned; if null all node types are returned.
     * @param recurse Whether to recursively list child interfaces.
     * @return A sorted set of child nodes.
     */
    public static SortedSet<String> listRoot(Pattern pattern, NSType type, boolean recurse) {
        return list((NSInterface)null, pattern, type, recurse);
    }

    /**
     * Returns a list of all the child leaf nodes defined under the given parent interface on this server.
     *
     * @param parent  The interface to be listed.
     * @param pattern The regular expression pattern used to filter the returned nodes by name; if null all nodes are
     *                returned.
     * @param type    The type of node to be returned; if null all node types are returned.
     * @param recurse Whether to recursively list child interfaces.
     * @return A sorted set of child nodes.
     */
    public static SortedSet<String> list(String parent, String pattern, String type, boolean recurse) {
        return list(getInterface(parent), pattern, type, recurse);
    }

    /**
     * Returns a list of all the child leaf nodes defined under the given parent interface on this server.
     *
     * @param parent  The interface to be listed.
     * @param pattern The regular expression pattern used to filter the returned nodes by name; if null all nodes are
     *                returned.
     * @param type    The type of node to be returned; if null all node types are returned.
     * @param recurse Whether to recursively list child interfaces.
     * @return A sorted set of child nodes.
     */
    public static SortedSet<String> list(NSInterface parent, String pattern, String type, boolean recurse) {
        return list(parent, pattern == null ? null : Pattern.compile(pattern), type, recurse);
    }

    /**
     * Returns a list of all the child leaf nodes defined under the given parent interface on this server.
     *
     * @param parent  The interface to be listed.
     * @param pattern The regular expression pattern used to filter the returned nodes by name; if null all nodes are
     *                returned.
     * @param type    The type of node to be returned; if null all node types are returned.
     * @param recurse Whether to recursively list child interfaces.
     * @return A sorted set of child nodes.
     */
    public static SortedSet<String> list(NSInterface parent, Pattern pattern, String type, boolean recurse) {
        return list(parent, pattern, type == null ? null : NSType.create(type), recurse);
    }

    /**
     * Returns a list of all the child leaf nodes defined under the given parent interface on this server.
     *
     * @param parent  The interface to be listed.
     * @param pattern The regular expression pattern used to filter the returned nodes by name; if null all nodes are
     *                returned.
     * @param type    The type of node to be returned; if null all node types are returned.
     * @param recurse Whether to recursively list child interfaces.
     * @return A sorted set of child nodes.
     */
    public static SortedSet<String> list(NSInterface parent, Pattern pattern, NSType type, boolean recurse) {
        if (parent == null) parent = Namespace.current().getRootNode();

        SortedSet<String> children = new TreeSet<String>();
        NSNode[] nodes = parent.getNodes();

        for (NSNode node : nodes) {
            if (node instanceof NSInterface && recurse) {
                children.addAll(list((NSInterface)node, pattern, type, true));
            }

            String name = node.getNSName().toString();

            if ((pattern == null || pattern.matcher(name).matches()) && (type == null || type.equals(node.getNodeTypeObj()))) {
                children.add(name);
            }
        }

        return children;
    }

    /**
     * Grants the specified permissions on the given node.
     *
     * @param node        The node to grant the permission on.
     * @param permissions The permissions to be granted.
     */
    public static void setPermissions(String node, IData[] permissions) {
        if (node == null || permissions == null) return;

        for (IData permission : permissions) {
            if (permission != null) {
                IDataCursor cursor = permission.getCursor();
                String type = IDataUtil.getString(cursor, "type");
                String acl = IDataUtil.getString(cursor, "acl");
                cursor.destroy();

                setPermission(node, type, acl);
            }
        }
    }

    /**
     * Grants the specified permission on the given node.
     *
     * @param node              The node to grant the permission on.
     * @param permission        The permission to be granted.
     * @param accessControlList The ACL to grant the permission to.
     */
    public static void setPermission(String node, String permission, String accessControlList) {
        setPermission(node, NodePermission.normalize(permission), accessControlList);
    }

    /**
     * Grants the specified permission on the given node.
     *
     * @param node              The node to grant the permission on.
     * @param permission        The permission to be granted.
     * @param accessControlList The ACL to grant the permission to.
     */
    public static void setPermission(String node, NodePermission permission, String accessControlList) {
        if (node == null || permission == null || accessControlList == null) return;

        switch (permission) {
            case LIST:
                ACLManager.setBrowseAclGroup(node, accessControlList);
                break;
            case READ:
                ACLManager.setReadAclGroup(node, accessControlList);
                break;
            case WRITE:
                ACLManager.setWriteAclGroup(node, accessControlList);
                break;
            case EXECUTE:
                ACLManager.setAclGroup(node, accessControlList);
                break;
            default:
                throw new IllegalArgumentException("Permission not supported: " + permission);
        }
    }
}
