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

package permafrost.tundra.flow;

import org.junit.Test;
import permafrost.tundra.data.IDataMap;

import static org.junit.Assert.*;

public class ConditionEvaluatorTest {

    @Test
    public void testEvaluateNullCondition() throws Exception {
        IDataMap map = new IDataMap();
        map.put("a", "1");
        map.put("b", "2");

        String condition = null;

        boolean result = ConditionEvaluator.evaluate(condition, map);

        assertTrue(result);
    }

    @Test
    public void testEvaluateNullScope() throws Exception {
        String condition = "%a% == 1";

        boolean result = ConditionEvaluator.evaluate(condition, null);

        assertTrue(!result);
    }

    @Test
    public void testEvaluateTrueCondition() throws Exception {
        IDataMap map = new IDataMap();
        map.put("a", "1");
        map.put("b", "2");

        String condition = "%a% == 1";

        boolean result = ConditionEvaluator.evaluate(condition, map);

        assertTrue(result);
    }

    @Test
    public void testEvaluateFalseCondition() throws Exception {
        IDataMap map = new IDataMap();
        map.put("a", "1");
        map.put("b", "2");

        String condition = "%a% == 2";

        boolean result = ConditionEvaluator.evaluate(condition, map);

        assertFalse(result);
    }

    @Test
    public void testEvaluateTrueOrCondition() throws Exception {
        IDataMap map = new IDataMap();
        map.put("a", "1");
        map.put("b", "2");

        String condition = "%a% == 2 or %b% == 2";

        boolean result = ConditionEvaluator.evaluate(condition, map);

        assertTrue(result);
    }

    @Test
    public void testEvaluateFalseOrCondition() throws Exception {
        IDataMap map = new IDataMap();
        map.put("a", "1");
        map.put("b", "2");

        String condition = "%a% == 2 or %b% == 1";

        boolean result = ConditionEvaluator.evaluate(condition, map);

        assertTrue(!result);
    }

    @Test
    public void testEvaluateTrueAndCondition() throws Exception {
        IDataMap map = new IDataMap();
        map.put("a", "1");
        map.put("b", "2");

        String condition = "%a% == 1 and %b% == 2";

        boolean result = ConditionEvaluator.evaluate(condition, map);

        assertTrue(result);
    }

    @Test
    public void testEvaluateFalseAndCondition() throws Exception {
        IDataMap map = new IDataMap();
        map.put("a", "1");
        map.put("b", "2");

        String condition = "%a% == 2 and %b% == 2";

        boolean result = ConditionEvaluator.evaluate(condition, map);

        assertTrue(!result);
    }
}
