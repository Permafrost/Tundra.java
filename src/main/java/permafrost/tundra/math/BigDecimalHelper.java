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

package permafrost.tundra.math;

import com.wm.app.b2b.server.ServiceException;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.ObjectHelper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * A collection of convenience methods for working with decimals.
 */
public class BigDecimalHelper {
    /**
     * The default rounding mode used by the methods in this class.
     */
    public static RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Disallow instantiation of this class.
     */
    private BigDecimalHelper() {}

    /**
     * Parses the given string and returns a decimal representation.
     * @param string A string to be parsed as a decimal.
     * @return       A decimal representation of the given string.
     */
    public static BigDecimal parse(String string) {
        if (string == null) return null;
        return new BigDecimal(string);
    }

    /**
     * Parses the given strings and returns their decimal representations.
     * @param strings One or more strings to be parsed as a decimal.
     * @return        A decimal representation of the given strings.
     */
    public static BigDecimal[] parse(String[] strings) {
        if (strings == null) return null;

        BigDecimal[] decimals = new BigDecimal[strings.length];

        for (int i = 0; i < strings.length; i++) {
            decimals[i] = parse(strings[i]);
        }

        return decimals;
    }

    /**
     * Returns a string representation of the given decimal.
     * @param decimal The decimal to convert to a string representation.
     * @return        The string representation of the given decimal.
     */
    public static String emit(BigDecimal decimal) {
        return ObjectHelper.stringify(decimal);
    }

    /**
     * Returns a string representation of the given list of decimals.
     * @param decimals The list of decimals to convert to string representations.
     * @return         The string representations of the given list of decimals.
     */
    public static String[] emit(BigDecimal[] decimals) {
        if (decimals == null) return null;

        String[] strings = new String[decimals.length];

        for (int i = 0; i < decimals.length; i++) {
            strings[i] = emit(decimals[i]);
        }

        return strings;
    }

    /**
     * Returns a BigDecimal representation of the given object, if it an
     * instance of java.lang.Number or a java.lang.String which can be
     * parsed as a decimal number.
     * @param object An object to be converted to a BigDecimal.
     * @return       A BigDecimal representation of the given object.
     */
    public static BigDecimal normalize(Object object) {
        BigDecimal decimal = null;

        if (object instanceof BigDecimal) {
            decimal = (BigDecimal)object;
        } else if (object instanceof BigInteger) {
            decimal = new BigDecimal((BigInteger)object);
        } else if (object instanceof Number) {
            decimal = new BigDecimal(((Number)object).doubleValue());
        } else if (object instanceof String) {
            decimal = parse((String) object);
        }

        return decimal;
    }

    /**
     * Returns BigDecimal representations of the given list of Objects.
     * @param values The objects to convert to BigDecimal representations.
     * @return       BigDecimal representations of the given objects.
     */
    public static BigDecimal[] normalize(Object[] values) {
        if (values == null) return null;

        BigDecimal[] decimals = new BigDecimal[values.length];

        for (int i = 0; i < values.length; i++) {
            decimals[i] = normalize(values[i]);
        }

        return decimals;
    }

    /**
     * Returns the absolute value of the given decimal number.
     * @param decimal A decimal number.
     * @return        The absolute value of the given decimal number.
     */
    public static BigDecimal absolute(BigDecimal decimal) {
        if (decimal == null) return null;
        return decimal.abs();
    }

    /**
     * Returns the absolute values of the given decimal numbers.
     * @param decimals A list of decimal numbers.
     * @return         The absolute values of the given list of decimal numbers.
     */
    public static BigDecimal[] absolute(BigDecimal[] decimals) {
        if (decimals == null) return null;

        BigDecimal[] results = new BigDecimal[decimals.length];

        for (int i = 0; i < decimals.length; i++) {
            results[i] = absolute(decimals[i]);
        }

        return results;
    }

    /**
     * Returns the negated value of the given decimal number.
     * @param decimal A decimal number.
     * @return        The negated value of the given decimal number.
     */
    public static BigDecimal negate(BigDecimal decimal) {
        if (decimal == null) return null;
        return decimal.negate();
    }

    /**
     * Returns the negated values of the given decimal numbers.
     * @param decimals A list of decimal numbers.
     * @return         The negated values of the given list of decimal numbers.
     */
    public static BigDecimal[] negate(BigDecimal[] decimals) {
        if (decimals == null) return null;

        BigDecimal[] results = new BigDecimal[decimals.length];

        for (int i = 0; i < decimals.length; i++) {
            results[i] = negate(decimals[i]);
        }

        return results;
    }

    /**
     * Returns the sum of all the given decimals.
     * @param operands The decimal numbers to be summed.
     * @return         The sum of all the given decimal numbers.
     */
    public static BigDecimal add(BigDecimal ...operands) {
        BigDecimal result = null;

        if (operands != null) {
            for (BigDecimal operand : operands) {
                if (operand != null) {
                    if (result == null) {
                        result = operand;
                    } else {
                        result = result.add(operand);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Subtracts one decimal from another returning the result.
     * @param minuend    The decimal to be subtracted from.
     * @param subtrahend The decimal to be subtracted.
     * @return           The result of subtracting the subtrahend from the minuend.
     */
    public static BigDecimal subtract(BigDecimal minuend, BigDecimal subtrahend) {
        BigDecimal result = null;

        if (minuend != null) {
            result = minuend;
            if (subtrahend != null) {
                result = result.subtract(subtrahend);
            }
        }

        return result;
    }

    /**
     * Returns the multiplication of all the given decimals.
     * @param operands The decimal numbers to be summed.
     * @return         The multiplication of all the given decimal numbers.
     */
    public static BigDecimal multiply(BigDecimal ...operands) {
        BigDecimal result = null;

        if (operands != null) {
            for (BigDecimal operand : operands) {
                if (operand != null) {
                    if (result == null) {
                        result = operand;
                    } else {
                        result = result.multiply(operand);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Divides the given dividend by the divisor.
     * @param dividend     The decimal to be divided.
     * @param divisor      The decimal to divide by.
     * @return             The result of dividing the dividend by the divisor.
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return divide(dividend, divisor, (RoundingMode)null);
    }

    /**
     * Divides the given dividend by the divisor.
     * @param dividend     The decimal to be divided.
     * @param divisor      The decimal to divide by.
     * @param precision    The number of decimal places preserved in the result.
     * @return             The result of dividing the dividend by the divisor.
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int precision) {
        return divide(dividend, divisor, precision, null);
    }

    /**
     * Divides the given dividend by the divisor.
     * @param dividend     The decimal to be divided.
     * @param divisor      The decimal to divide by.
     * @param roundingMode The rounding algorithm to use when rounding the result.
     * @return             The result of dividing the dividend by the divisor.
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, RoundingMode roundingMode) {
        int precision = 0;
        if (dividend != null) {
            precision = dividend.scale();
        }
        if (divisor != null) {
            if (divisor.scale() > precision) precision = divisor.scale();
        }

        return divide(dividend, divisor, precision, roundingMode);
    }

    /**
     * Divides the given dividend by the divisor.
     * @param dividend     The decimal to be divided.
     * @param divisor      The decimal to divide by.
     * @param precision    The number of decimal places preserved in the result.
     * @param roundingMode The rounding algorithm to use when rounding the result.
     * @return             The result of dividing the dividend by the divisor.
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int precision, RoundingMode roundingMode) {
        if (roundingMode == null) roundingMode = DEFAULT_ROUNDING_MODE;

        BigDecimal result = null;

        if (dividend != null) {
            result = dividend;
            if (divisor != null) {
                result = result.divide(divisor, precision, roundingMode);
            }
        }

        return result;
    }

    /**
     * Returns the exponentiation of the given base raised to power of the given exponent.
     * @param base     The decimal base to be raised to the power of the given exponent.
     * @param exponent The exponent to raise the given base to.
     * @return         The result of raising the given base to the power of the given exponent.
     */
    public static BigDecimal power(BigDecimal base, BigInteger exponent) {
        if (exponent == null) return base;
        return power(base, exponent.intValue());
    }

    /**
     * Returns the exponentiation of the given base raised to power of the given exponent.
     * @param base     The decimal base to be raised to the power of the given exponent.
     * @param exponent The exponent to raise the given base to.
     * @return         The result of raising the given base to the power of the given exponent.
     */
    public static BigDecimal power(BigDecimal base, int exponent) {
        if (base == null) return null;
        return base.pow(exponent);
    }

    /**
     * Rounds the given decimal to the given precision using the default rounding algorithm.
     * @param decimal   A decimal to be rounded.
     * @param precision The number of decimal places to round to.
     * @return          The given decimal rounded to the given precision using the default algorithm.
     */
    public static BigDecimal round(BigDecimal decimal, int precision) {
        return round(decimal, precision, null);
    }

    /**
     * Rounds the given decimal to the given precision with the given rounding algorithm.
     * @param decimal      A decimal to be rounded.
     * @param precision    The number of decimal places to round to.
     * @param roundingMode The rounding algorithm to be used.
     * @return             The given decimal rounded to the given precision using the given algorithm.
     */
    public static BigDecimal round(BigDecimal decimal, int precision, RoundingMode roundingMode) {
        if (decimal == null) return null;
        if (roundingMode == null) roundingMode = DEFAULT_ROUNDING_MODE;
        return decimal.setScale(precision, roundingMode);
    }

    /**
     * Returns the largest of the given list of decimal numbers.
     * @param decimals A list of decimal numbers.
     * @return         The largest of the given numbers.
     */
    public static BigDecimal maximum(BigDecimal ...decimals) {
        BigDecimal result = null;

        if (decimals != null) {
            for (BigDecimal decimal : decimals) {
                if (decimal != null) {
                    if (result == null) {
                        result = decimal;
                    } else {
                        result = result.max(decimal);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the smallest of the given list of decimal numbers.
     * @param decimals A list of decimal numbers.
     * @return         The smallest of the given numbers.
     */
    public static BigDecimal minimum(BigDecimal ...decimals) {
        BigDecimal result = null;

        if (decimals != null) {
            for (BigDecimal decimal : decimals) {
                if (decimal != null) {
                    if (result == null) {
                        result = decimal;
                    } else {
                        result = result.min(decimal);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the average or mean from the given list of decimal numbers.
     * @param decimals A list of decimal numbers.
     * @return         The average or mean value of the given list of values.
     */
    public static BigDecimal average(BigDecimal ...decimals) {
        BigDecimal result = null;

        if (decimals != null) {
            int length = 0;
            for (BigDecimal decimal : decimals) {
                if (decimal != null) {
                    if (result == null) {
                        result = decimal;
                    } else {
                        result = result.add(decimal);
                    }
                    length++;
                }
            }
            if (result != null) {
                result = divide(result, new BigDecimal(length));
            }
        }

        return result;
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     * @param decimal The string to validate.
     * @param raise   True if an exception should be thrown if the string is not a valid decimal number.
     * @return        True if the string can be parsed as a decimal number, otherwise false.
     * @throws ServiceException If raise is true and the given string is not a valid decimal number.
     */
    public static boolean validate(String decimal, boolean raise) throws ServiceException {
        boolean valid = false;
        try {
            if (decimal != null) {
                parse(decimal);
                valid = true;
            }
        } catch(NumberFormatException ex) {
            if (raise) ExceptionHelper.raise(ex);
        }
        return valid;
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     * @param decimal The string to validate.
     * @return        True if the string can be parsed as a decimal number, otherwise false.
     */
    public static boolean validate(String decimal) {
        boolean result = false;

        try {
            result = validate(decimal, false);
        } catch(ServiceException ex) {
            // suppress the exception
        }

        return result;
    }
}
