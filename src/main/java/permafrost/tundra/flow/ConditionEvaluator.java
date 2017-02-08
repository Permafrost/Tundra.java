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

import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.lang.flow.ExpressionEvaluator;
import com.wm.lang.flow.MalformedExpressionException;
import org.w3c.dom.Node;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.xml.dom.NodeHelper;
import permafrost.tundra.xml.dom.Nodes;
import permafrost.tundra.xml.xpath.XPathHelper;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

/**
 * Performs webMethods Integration Server flow language conditional statement evaluation against a specified scope.
 */
public class ConditionEvaluator {
    /**
     * Regular expression pattern for matching an IData node XPath expression.
     */
    public static final Pattern CONDITION_NODE_XPATH_REGULAR_EXPRESSION_PATTERN = Pattern.compile("(?i)%([^%\\/]+)(\\/[^%]+)%");

    /**
     * The conditional statement to be evaluated by this object.
     */
    protected String condition;

    /**
     * Compiled node XPath expression.
     */
    protected Map<Integer, XPathExpression> expressions;

    /**
     * Constructs a new flow condition.
     *
     * @param condition         The conditional statement to be evaluated.
     */
    public ConditionEvaluator(String condition) {
        this(condition, null);
    }

    /**
     * Constructs a new flow condition.
     *
     * @param condition         The conditional statement to be evaluated.
     * @param namespaceContext  An optional namespace context used when resolving XPath expressions.
     */
    public ConditionEvaluator(String condition, NamespaceContext namespaceContext) {
        this.condition = condition;

        if (condition != null) {
            Matcher matcher = CONDITION_NODE_XPATH_REGULAR_EXPRESSION_PATTERN.matcher(condition);
            Map<Integer, XPathExpression> expressions = new TreeMap<Integer, XPathExpression>();
            int i = 0;
            while (matcher.find()) {
                try {
                    expressions.put(i, XPathHelper.compile(matcher.group(2), namespaceContext));
                } catch(XPathExpressionException ex) {
                    // do nothing, assume a normal IData fully-qualified key was specified rather than an XPath expression
                } finally {
                    i++;
                }
            }

            if (expressions.size() > 0) this.expressions = Collections.unmodifiableMap(expressions);
        }
    }

    /**
     * Returns the condition that is evaluated by this object.
     *
     * @return The condition that is evaluated by this object.
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Evaluates the conditional statement against the given scope.
     *
     * @param scope The scope against which the conditional statement is evaluated.
     * @return      True if the conditional statement evaluates to true, otherwise false.
     */
    public synchronized boolean evaluate(IData scope) {
        String condition = this.condition;
        boolean result = true;

        if (condition != null) {
            if (scope == null) {
                scope = IDataFactory.create();
            } else if (expressions != null) {
                Matcher matcher = CONDITION_NODE_XPATH_REGULAR_EXPRESSION_PATTERN.matcher(condition);
                StringBuffer buffer = new StringBuffer();
                int i = 0;

                while (matcher.find()) {
                    String key = matcher.group(1);
                    XPathExpression expression = expressions.get(i);

                    if (expression != null) {
                        Node node = IDataHelper.get(scope, key, Node.class);
                        if (node != null) {
                            try {
                                Nodes nodes = XPathHelper.get(node, expression);
                                if (nodes != null && nodes.size() > 0) {
                                    matcher.appendReplacement(buffer, Matcher.quoteReplacement("\"" + NodeHelper.getValue(nodes.get(0)) + "\""));
                                } else {
                                    matcher.appendReplacement(buffer, Matcher.quoteReplacement("$null"));
                                }
                            } catch (XPathExpressionException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                    i++;
                }

                matcher.appendTail(buffer);
                condition = buffer.toString();
            }

            try {
                result = ExpressionEvaluator.evalToBoolean(condition, scope);
            } catch (MalformedExpressionException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        return result;
    }

    /**
     * Evaluates the conditional statement against the given scope. This is a convenience method which constructs a new
     * Condition object and evaluates it.
     *
     * @param condition The conditional statement to be evaluated.
     * @param scope     The scope against which the conditional statement is evaluated.
     * @return          True if the conditional statement evaluates to true, otherwise false.
     */
    public static boolean evaluate(String condition, IData scope) {
        return evaluate(condition, scope, null);
    }

    /**
     * Evaluates the conditional statement against the given scope. This is a convenience method which constructs a new
     * Condition object and evaluates it.
     *
     * @param condition         The conditional statement to be evaluated.
     * @param scope             The scope against which the conditional statement is evaluated.
     * @param namespaceContext  The namespace context used when evaluating XPath expressions against Node objects.
     * @return                  True if the conditional statement evaluates to true, otherwise false.
     */
    public static boolean evaluate(String condition, IData scope, NamespaceContext namespaceContext) {
        ConditionEvaluator evaluator = new ConditionEvaluator(condition, namespaceContext);
        return evaluator.evaluate(scope);
    }
}

