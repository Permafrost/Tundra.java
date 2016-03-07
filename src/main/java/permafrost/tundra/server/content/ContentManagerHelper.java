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

package permafrost.tundra.server.content;

import com.wm.app.b2b.server.ContentHandlerFactory;
import com.wm.app.b2b.server.ContentManager;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

/**
 * A collection of convenience methods for working with the Integration Server ContentManager.
 */
public final class ContentManagerHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ContentManagerHelper() {}

    /**
     * Returns all the currently registered content types and associated content handler factories.
     *
     * @return The registered content types and associated content handler factories.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ContentHandlerFactory> getRegistrations() {
        Map<String, ContentHandlerFactory> registrations = new TreeMap<String, ContentHandlerFactory>();

        try {
            // use reflection to get access to package-private member variable
            ContentManager manager = new ContentManager();
            Field gHandlers = manager.getClass().getDeclaredField("gHandlers");
            gHandlers.setAccessible(true);
            registrations.putAll((Map)gHandlers.get(manager));
        } catch(NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        return registrations;
    }
}
