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

package permafrost.tundra.net.http;

/**
 * Represents all the possible allowed HTTP request methods.
 */
public enum HTTPMethod {
    GET(0), PUT(1), POST(2), HEAD(3), CONNECT(4), OPTIONS(5), DELETE(6), TRACE(7);

    private int value;
    private static java.util.Map<Integer, HTTPMethod> map = new java.util.HashMap<Integer, HTTPMethod>();

    HTTPMethod(int value) {
        this.value = value;
    }

    static {
        for (HTTPMethod method : HTTPMethod.values()) {
            map.put(method.value, method);
        }
    }

    /**
     * Returns an HTTPMethod for the given ordinal value.
     *
     * @param value The value to be converted to an HTTPMethod.
     * @return The HTTPMethod representing the given value.
     */
    public static HTTPMethod valueOf(int value) {
        return map.get(value);
    }

    /**
     * Returns an HTTPMethod for the given string value.
     *
     * @param value The value to be converted to an HTTPMethod.
     * @return The HTTPMethod representing the given value.
     */
    public static HTTPMethod normalize(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }
}
