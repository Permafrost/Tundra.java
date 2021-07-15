/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Lachlan Dowding
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

import java.util.HashMap;
import java.util.Map;

/**
 * The logging levels supported by the Integration Server logging facility.
 */
public enum ServerLogLevel {
    OFF(-1), FATAL(0), ERROR(1), WARN(2), INFO(4), DEBUG(7), TRACE(10);

    /**
     * The default logging level used by Tundra.
     */
    public static final ServerLogLevel DEFAULT_LOG_LEVEL = ServerLogLevel.DEBUG;
    /**
     * The internal level code used by Integration Server for this logging level.
     */
    private final int levelCode;
    /**
     * Translation table for converting from a level code back to a ServerLogLevel.
     */
    private static final Map<Integer, ServerLogLevel> TRANSLATE_CODE_TO_LEVEL = new HashMap<Integer, ServerLogLevel>();

    // initialize the translation table for code to ServerLogLevel.
    static {
        for (ServerLogLevel type : ServerLogLevel.values()) {
            TRANSLATE_CODE_TO_LEVEL.put(type.levelCode, type);
        }
        // include the gaps in the translation table also
        TRANSLATE_CODE_TO_LEVEL.put(3, INFO);
        TRANSLATE_CODE_TO_LEVEL.put(5, DEBUG);
        TRANSLATE_CODE_TO_LEVEL.put(6, DEBUG);
        TRANSLATE_CODE_TO_LEVEL.put(8, TRACE);
        TRANSLATE_CODE_TO_LEVEL.put(9, TRACE);
    }

    /**
     * Create a new ServerLogLevel.
     *
     * @param levelCode The internal level code used by Integration Server for this logging level.
     */
    ServerLogLevel(int levelCode) {
        this.levelCode = levelCode;
    }

    /**
     * Returns the level code associated with this ServerLogLevel.
     *
     * @return the level code associated with this ServerLogLevel.
     */
    public int getLevelCode() {
        return levelCode;
    }

    /**
     * Returns the ServerLogLevel with the given code.
     *
     * @param levelCode     The code of the level to be returned.
     * @return              The ServerLogLevel with the given code.
     */
    public ServerLogLevel normalize(int levelCode) {
        ServerLogLevel level;
        if (levelCode < 0) {
            level = OFF;
        } else if (levelCode > 10) {
            level = TRACE;
        } else {
            level = TRANSLATE_CODE_TO_LEVEL.get(levelCode);
        }
        return level;
    }

    /**
     * Returns the ServerLogLevel with the given name.
     *
     * @param levelName         The name of the level to be returned.
     * @return                  The ServerLogLevel with the given name.
     */
    public static ServerLogLevel normalize(String levelName) {
        if (levelName == null) return null;
        return valueOf(levelName.trim().toUpperCase());
    }
}
