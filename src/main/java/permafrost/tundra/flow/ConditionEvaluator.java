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
import java.util.ArrayList;
import java.util.List;
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
    public static final Pattern CONDITION_NODE_XPATH_REGULAR_EXPRESSION_PATTERN = Pattern.compile("(?i)%([^%]*?node)(\\/[^%]+)%");

    /**
     * The conditional statement to be evaluated by this object.
     */
    protected String condition;

    /**
     * Regular expression matcher for node XPath expressions which has been matched against the condition.
     */
    protected Matcher nodeXPathMatcher;

    /**
     * Compiled node XPath expression.
     */
    protected List<XPathExpression> nodeXPathExpressions = new ArrayList<XPathExpression>();

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
            nodeXPathMatcher = CONDITION_NODE_XPATH_REGULAR_EXPRESSION_PATTERN.matcher(condition);
            while (nodeXPathMatcher.find()) {
                XPathExpression expression = null;
                try {
                    expression = XPathHelper.compile(nodeXPathMatcher.group(2), namespaceContext);
                } catch(XPathExpressionException ex) {
                    // do nothing, assume a normal IData fully-qualified key was specified rather than an XPath expression
                } finally {
                    nodeXPathExpressions.add(expression);
                }
            }
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
    public boolean evaluate(IData scope) {
        boolean result = true;

        if (condition != null) {
            if (scope == null) {
                scope = IDataFactory.create();
            } else {
                // support resolving XPath expressions against nodes
                if (nodeXPathExpressions != null) {
                    // reset the matcher for use when evaluating
                    nodeXPathMatcher.reset();

                    int i = 0;
                    while (nodeXPathMatcher.find()) {
                        String key = nodeXPathMatcher.group(1);
                        XPathExpression expression = nodeXPathExpressions.get(i);

                        if (expression != null) {
                            Object node = IDataHelper.get(scope, key);
                            if (node instanceof Node) {
                                StringBuffer buffer = new StringBuffer();
                                try {
                                    Nodes nodes = XPathHelper.get((Node)node, expression);
                                    if (nodes != null && nodes.size() > 0) {
                                        nodeXPathMatcher.appendReplacement(buffer, "\"" + NodeHelper.getValue(nodes.get(0)) + "\"");
                                    }
                                } catch (XPathExpressionException ex) {
                                    throw new RuntimeException(ex);
                                }
                                nodeXPathMatcher.appendTail(buffer);
                                condition = buffer.toString();
                            }
                        }

                        i++;
                    }
                }
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
        ConditionEvaluator evaluator = new ConditionEvaluator(condition);
        return evaluator.evaluate(scope);
    }
}

