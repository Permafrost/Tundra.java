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

import com.wm.data.IData;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.BaseException;
import permafrost.tundra.lang.ExceptionHelper;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NameHelper {
    private static final long LOCALHOST_CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    private static volatile long localhostCacheExpiry = 0;
    private static volatile IData localhost = null;

    /**
     * Disallow instantiation of this class.
     */
    private NameHelper() {}

    /**
     * Resolves the given domain name or internet address, returning
     * an IData representation.
     *
     * @param name              The domain name or internet address to be resolved.
     * @return                  An IData representation of the resolved address,
     *                          containing the following keys: $domain, $host, $ip.
     * @throws BaseException    If the address could not be resolved.
     */
    public static IData resolve(String name) throws BaseException {
        IData output = null;
        try {
            output = toIData(InetAddress.getByName(name));
        } catch (UnknownHostException ex) {
            ExceptionHelper.raise(ex);
        }
        return output;
    }

    /**
     * Returns an IData representation of the localhost internet domain name
     * and address.
     * @return                  An IData representation of the localhost address,
     *                          containing the following keys: $domain, $host, $ip.
     * @throws BaseException    If the localhost address could not be resolved.
     */
    public static IData localhost() throws BaseException {
        IData output = null;
        try {
            if (localhost == null || localhostCacheExpiry <= System.currentTimeMillis()) {
                localhost = toIData(InetAddress.getLocalHost());
                localhostCacheExpiry = System.currentTimeMillis() + LOCALHOST_CACHE_DURATION;
            }
            output = IDataHelper.duplicate(localhost);
        } catch(UnknownHostException ex) {
            ExceptionHelper.raise(ex);
        }
        return output;
    }

    /**
     * Returns an IData representation of the given internet address.
     * @param address The address to be converted to an IDataMap.
     * @return        An IData representation of the given address,
     *                containing the following keys: $domain, $host, $ip.
     */
    private static IData toIData(InetAddress address) {
        if (address == null) return null;

        IDataMap output = new IDataMap();
        output.put("$domain", address.getCanonicalHostName().toLowerCase());
        output.put("$host", address.getHostName().toLowerCase());
        output.put("$ip", address.getHostAddress().toLowerCase());
        return output;
    }
}
