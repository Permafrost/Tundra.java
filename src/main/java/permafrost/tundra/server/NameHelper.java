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

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.ExceptionHelper;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NameHelper {
    /**
     * Disallow instantiation of this class.
     */
    private NameHelper() {}

    /**
     * Resolves the given domain name or internet address, returning an IData representation.
     *
     * @param name The domain name or internet address to be resolved.
     * @return An IData representation of the resolved address, containing the following keys: $domain, $host, $ip.
     * @throws ServiceException If the address could not be resolved.
     */
    public static IData resolve(String name) throws ServiceException {
        IData output = null;
        try {
            output = InternetAddress.resolve(name).getIData();
        } catch (UnknownHostException ex) {
            ExceptionHelper.raise(ex);
        }
        return output;
    }

    /**
     * Returns an IData representation of the localhost internet domain name and address.
     *
     * @return An IData representation of the localhost address, containing the following keys: $domain, $host, $ip.
     * @throws ServiceException If the localhost address could not be resolved.
     */
    public static IData localhost() throws ServiceException {
        IData output = null;
        try {
            output = InternetAddress.localhost().getIData();
        } catch (UnknownHostException ex) {
            ExceptionHelper.raise(ex);
        }
        return output;
    }

    /**
     * Convenience class for resolving domain names and converting to IData representations.
     */
    private static class InternetAddress implements IDataCodable {
        /**
         * Cache localhost in volatile class member to optimize performance by avoiding the thread synchronization in
         * the java.net.InetAddress class.
         */
        protected static final long LOCALHOST_EXPIRE_DURATION = 5 * 60 * 1000; // 5 minutes
        protected static volatile long localhostExpireTime = 0;
        protected static volatile InternetAddress localhost;

        protected String domain, host, ip;

        /**
         * Create a new InternetAddress from the given InetAddress object.
         *
         * @param address The address to use to create this object.
         */
        public InternetAddress(InetAddress address) {
            if (address == null) throw new NullPointerException("address must not be null");
            this.domain = address.getCanonicalHostName().toLowerCase();
            this.host = address.getHostName().toLowerCase();
            this.ip = address.getHostAddress().toLowerCase();
        }

        /**
         * Returns the domain name associated with this internet address.
         *
         * @return The domain name associated with this internet address.
         */
        public String getDomain() {
            return domain;
        }

        /**
         * Returns the host name associated with this internet address.
         *
         * @return The host name associated with this internet address.
         */
        public String getHost() {
            return host;
        }

        /**
         * Returns the IP address associated with this internet address.
         *
         * @return The IP address associated with this internet address.
         */
        public String getIPAddress() {
            return ip;
        }

        /**
         * Returns an IData representation of this internet address.
         *
         * @return An IData representation of this internet address, containing the following keys: $domain, $host, $ip.
         */
        public IData getIData() {
            IDataMap output = new IDataMap();
            output.put("$domain", domain);
            output.put("$host", host);
            output.put("$ip", ip);
            return output;
        }

        /**
         * This method is not implemented by this class.
         *
         * @param input An IData to use to set the member variables of this class.
         * @throws UnsupportedOperationException Because this method is not implemented by this class.
         */
        public void setIData(IData input) {
            throw new UnsupportedOperationException("setIData is not supported by this class");
        }

        /**
         * Returns the localhost address.
         *
         * @return The localhost address.
         * @throws UnknownHostException If the localhost cannot be resolved.
         */
        public static InternetAddress localhost() throws UnknownHostException {
            // thread synchronization is not required because we don't care if we miss an update
            if (localhost == null || localhostExpireTime <= System.currentTimeMillis()) {
                localhost = new InternetAddress(InetAddress.getLocalHost());
                localhostExpireTime = System.currentTimeMillis() + LOCALHOST_EXPIRE_DURATION;
            }
            return localhost;
        }

        /**
         * Returns the internet address that the given name resolves to.
         *
         * @param name The name to be resolved.
         * @return The internet address which resolves to the given name.
         * @throws UnknownHostException If the name cannot be resolved.
         */
        public static InternetAddress resolve(String name) throws UnknownHostException {
            return new InternetAddress(InetAddress.getByName(name));
        }
    }
}
