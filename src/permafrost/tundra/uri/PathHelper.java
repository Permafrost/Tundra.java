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

package permafrost.tundra.uri;

import permafrost.tundra.array.ArrayHelper;
import permafrost.tundra.flow.Substitutor;

public class PathHelper {
    private static final java.util.regex.Pattern PATH_PATTERN = java.util.regex.Pattern.compile("/+");

    /**
     * Disallow instantiation of this class.
     */
    private PathHelper() {}

    /**
     * Parses a URI path string, with support for variable substitution strings.
     * @param input
     * @return
     */
    public static String[] parse(String input) {
        if (input == null || input.equals("") || input.equals("/")) return null;
        if (input.startsWith("/")) input = input.substring(1, input.length());
        if (input.endsWith("/")) input = input.substring(0, input.length() - 1);

        java.util.List<String> list = new java.util.ArrayList<String>();
        java.util.regex.Matcher substitutionMatcher = Substitutor.matcher(input);

        int index = 0;
        while(substitutionMatcher.find()) {
            int start = substitutionMatcher.start();
            int end = substitutionMatcher.end();

            if (index <= start) split(input.substring(index, start), list);
            append(substitutionMatcher.group(), list);

            index = end;
        }
        if (index <= input.length()) split(input.substring(index), list);

        return ArrayHelper.compact(list.toArray(new String[list.size()]));
    }

    private static void append(String item, java.util.List<String> list) {
        int i = list.size() - 1;
        if (i < 0) {
            list.add(item);
        } else {
            list.set(i, list.get(i) + item);
        }
    }

    private static void split(String input, java.util.List<String> list) {
        java.util.regex.Matcher matcher = PATH_PATTERN.matcher(input);

        int index = 0;
        while(matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            if (index <= start) append(input.substring(index, start), list);
            list.add("");

            index = end;
        }
        if (index <= input.length()) append(input.substring(index), list);
    }
}
