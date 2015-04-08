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

package permafrost.tundra.exception;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ExceptionHelperTest {
    @Test(expected = BaseException.class)
    public void testRaiseNoArguments() throws Exception {
        ExceptionHelper.raise();
    }

    @Test(expected = BaseException.class)
    public void testRaiseWithMessage() throws Exception {
        ExceptionHelper.raise("test");
    }

    @Test(expected = BaseException.class)
    public void testRaiseWithCause() throws Exception {
        ExceptionHelper.raise(new IllegalArgumentException("test"));
    }

    @Test(expected = BaseException.class)
    public void testRaiseWithCauseArray() throws Exception {
        Throwable[] throwables = new Throwable[2];

        for (int i = 0; i < throwables.length; i++) {
            throwables[i] = new IllegalArgumentException("test:" + i);
        }

        ExceptionHelper.raise(throwables);
    }

    @Test(expected = BaseException.class)
    public void testRaiseWithCauseCollection() throws Exception {
        List<Throwable> throwables = new ArrayList<Throwable>(2);

        for (int i = 0; i < 2; i++) {
            throwables.add(new IllegalArgumentException("test:" + i));
        }

        ExceptionHelper.raise(throwables);
    }

    @Test
    public void testGetMessageWithNull() throws Exception {
        assertEquals("Message should be an empty string", "", ExceptionHelper.getMessage((Throwable) null));
    }

    @Test
    public void testGetMessageWithThrowable() throws Exception {
        String message = "test";
        Throwable throwable = new IllegalArgumentException(message);

        assertEquals("Message should be prefixed with class name", throwable.getClass().getName() + ": " + message, ExceptionHelper.getMessage(throwable));
    }
}