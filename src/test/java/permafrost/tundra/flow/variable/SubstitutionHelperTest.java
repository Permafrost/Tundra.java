/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Lachlan Dowding
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

package permafrost.tundra.flow.variable;

import com.wm.data.IData;
import com.wm.data.IDataFactory;
import org.junit.Before;
import org.junit.Test;
import permafrost.tundra.data.IDataHelper;
import static org.junit.Assert.*;

public class SubstitutionHelperTest {
    IData scope = IDataFactory.create();

    @Before
    public void setUp() throws Exception {
        IDataHelper.put(scope, "a", "1");
        IDataHelper.put(scope, "b", "2");
        IDataHelper.put(scope, "c/d", "3");
        IDataHelper.put(scope, "e", 4);
    }

    @Test
    public void testSingleSubstringSubstitutionString() {
        String testCase = "Testing substring %a% substitution";
        String result = SubstitutionHelper.substitute(testCase, String.class, null, null, scope);
        assertEquals("Testing substring 1 substitution", result);
    }

    @Test
    public void testMultiSubstringSubstitutionString() {
        String testCase = "Testing substring %a% substitution %e%";
        String result = SubstitutionHelper.substitute(testCase, String.class, null, null, scope);
        assertEquals("Testing substring 1 substitution 4", result);
    }

    @Test
    public void testNonexistentSubstringSubstitutionString() {
        String testCase = "Testing substring %unknown% substitution";
        String result = SubstitutionHelper.substitute(testCase, String.class, null, null, scope);
        assertEquals(testCase, result);
    }

    @Test
    public void testNonexistentSubstringSubstitutionStringWithDefault() {
        String testCase = "Testing substring %unknown% substitution";
        String result = SubstitutionHelper.substitute(testCase, String.class, "4", null, scope);
        assertEquals("Testing substring 4 substitution", result);
    }

    @Test
    public void testSubstitutionString() {
        String testCase = "%a%";
        String result = SubstitutionHelper.substitute(testCase, String.class, null, null, scope);
        assertEquals("1", result);
    }

    @Test
    public void testSubstitutionInteger() {
        String testCase = "%e%";
        Object result = SubstitutionHelper.substitute(testCase, Object.class, null, null, scope);
        assertEquals(4, result);
    }
}