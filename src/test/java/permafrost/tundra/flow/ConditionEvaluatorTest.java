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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.w3c.dom.Document;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.xml.dom.DocumentHelper;
import permafrost.tundra.xml.sax.InputSourceHelper;

public class ConditionEvaluatorTest {

    @Test
    public void testEvaluateXPathCondition() throws Exception {
        String content = "<a><z>1</z><z>2</z><z>3</z><b><c>Example</c></b><b><c><d>Example 2</d></c></b><b></b></a>";
        Document document = DocumentHelper.parse(InputSourceHelper.normalize(InputStreamHelper.normalize(content)));

        IDataMap scope = new IDataMap();
        scope.put("document", document);

        assertTrue(ConditionEvaluator.evaluate("%document/a/z[1]% == 1", scope));
        assertTrue(ConditionEvaluator.evaluate("%document/a/z[1]% == 1 and %document/a/z[2]% == 2", scope));
        assertTrue(ConditionEvaluator.evaluate("%document/a/y% == $null", scope));
        assertFalse(ConditionEvaluator.evaluate("%document/a/y% != $null", scope));
        assertTrue(ConditionEvaluator.evaluate("%document/a% != $null", scope));
    }

    @Test
    public void testEvaluateXPathAndBranchCondition() throws Exception {
        String content = "<a><z>1</z><z>2</z><z>3</z><b><c>Example</c></b><b><c><d>Example 2</d></c></b><b></b></a>";
        Document document = DocumentHelper.parse(InputSourceHelper.normalize(InputStreamHelper.normalize(content)));

        IDataMap scope = new IDataMap();
        scope.put("document", document);
        scope.put("domain", "example.com");

        assertTrue(ConditionEvaluator.evaluate("%document/a/z% == 1 and %domain% == \"example.com\"", scope));
    }

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
