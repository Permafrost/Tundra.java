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
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A collection of convenience methods for working with integers.
 */
public final class BigIntegerHelper {
    /**
     * The default radix used by methods in this class.
     */
    public static int DEFAULT_RADIX = 10;

    /**
     * Disallow instantiation of this class.
     */
    private BigIntegerHelper() {}

    /**
     * Returns a java.math.BigInteger object by parsing the given an integer string.
     *
     * @param string A string to be parsed.
     * @return A java.math.BigInteger representation of the given string.
     */
    public static BigInteger parse(String string) {
        return parse(string, DEFAULT_RADIX);
    }

    /**
     * Returns a java.math.BigInteger object by parsing the given an integer string, using the given radix.
     *
     * @param string A string to be parsed.
     * @param radix  The radix to use when interpreting the given string.
     * @return A java.math.BigInteger representation of the given string.
     */
    public static BigInteger parse(String string, int radix) {
        if (string == null) return null;
        return new BigInteger(string, radix);
    }

    /**
     * Returns java.math.BigInteger representations of the given String[].
     *
     * @param strings A list of strings to parse.
     * @return A list of java.math.BigDecimal representations of the given strings.
     */
    public static BigInteger[] parse(String[] strings) {
        return parse(strings, DEFAULT_RADIX);
    }

    /**
     * Returns java.math.BigInteger representations of the given String[].
     *
     * @param strings A list of strings to parse.
     * @param radix   The radix to use when interpreting the given strings.
     * @return A list of java.math.BigDecimal representations of the given strings.
     */
    public static BigInteger[] parse(String[] strings, int radix) {
        if (strings == null) return null;

        BigInteger[] integers = new BigInteger[strings.length];

        for (int i = 0; i < strings.length; i++) {
            integers[i] = parse(strings[i], radix);
        }

        return integers;
    }

    /**
     * Returns a string representation of the given integer.
     *
     * @param integer The integer to convert to a string representation.
     * @return The string representation of the given integer.
     */
    public static String emit(BigInteger integer) {
        if (integer == null) return null;
        return integer.toString();
    }

    /**
     * Returns a string representation of the given list of integers.
     *
     * @param integers The list of integers to convert to string representations.
     * @return The string representations of the given list of integers.
     */
    public static String[] emit(BigInteger[] integers) {
        if (integers == null) return null;

        String[] strings = new String[integers.length];

        for (int i = 0; i < integers.length; i++) {
            strings[i] = emit(integers[i]);
        }

        return strings;
    }

    /**
     * Returns a BigInteger representation of the given object, if it an instance of java.lang.Number or a
     * java.lang.String which can be parsed as an integer.
     *
     * @param object An object to be converted to a BigInteger.
     * @return A BigInteger representation of the given object.
     */
    public static BigInteger normalize(Object object) {
        return normalize(object, DEFAULT_RADIX);
    }

    /**
     * Returns a BigInteger representation of the given object, if it an instance of java.lang.Number or a
     * java.lang.String which can be parsed as an integer.
     *
     * @param object An object to be converted to a BigInteger.
     * @param radix  The radix to use when interpreting the given strings.
     * @return A BigInteger representation of the given object.
     */
    public static BigInteger normalize(Object object, int radix) {
        BigInteger integer = null;

        if (object instanceof BigInteger) {
            integer = (BigInteger)object;
        } else if (object instanceof BigDecimal) {
            integer = ((BigDecimal)object).toBigInteger();
        } else if (object instanceof Number) {
            integer = BigInteger.valueOf(((Number)object).longValue());
        } else if (object instanceof String) {
            integer = parse((String)object, radix);
        }

        return integer;
    }

    /**
     * Returns BigInteger representations of the given list of Objects.
     *
     * @param values The objects to convert to BigInteger representations.
     * @return BigInteger representations of the given objects.
     */
    public static BigInteger[] normalize(Object[] values) {
        return normalize(values, DEFAULT_RADIX);
    }

    /**
     * Returns BigInteger representations of the given list of Objects.
     *
     * @param values The objects to convert to BigInteger representations.
     * @param radix  The radix to use when interpreting the given strings.
     * @return BigInteger representations of the given objects.
     */
    public static BigInteger[] normalize(Object[] values, int radix) {
        if (values == null) return null;

        BigInteger[] integers = new BigInteger[values.length];

        for (int i = 0; i < values.length; i++) {
            integers[i] = normalize(values[i], radix);
        }

        return integers;
    }

    /**
     * Returns the absolute value of the given integer number.
     *
     * @param integer An integer number.
     * @return The absolute value of the given integer number.
     */
    public static BigInteger absolute(BigInteger integer) {
        if (integer == null) return null;
        return integer.abs();
    }

    /**
     * Returns the absolute values of the given integer numbers.
     *
     * @param integers A list of integer numbers.
     * @return The absolute values of the given list of integer numbers.
     */
    public static BigInteger[] absolute(BigInteger[] integers) {
        if (integers == null) return null;

        BigInteger[] results = new BigInteger[integers.length];

        for (int i = 0; i < integers.length; i++) {
            results[i] = absolute(integers[i]);
        }

        return results;
    }

    /**
     * Returns the negated value of the given integer number.
     *
     * @param integer A integer number.
     * @return The negated value of the given integer number.
     */
    public static BigInteger negate(BigInteger integer) {
        if (integer == null) return null;
        return integer.negate();
    }

    /**
     * Returns the negated values of the given integer numbers.
     *
     * @param integers A list of integer numbers.
     * @return The negated values of the given list of integer numbers.
     */
    public static BigInteger[] negate(BigInteger[] integers) {
        if (integers == null) return null;

        BigInteger[] results = new BigInteger[integers.length];

        for (int i = 0; i < integers.length; i++) {
            results[i] = negate(integers[i]);
        }

        return results;
    }

    /**
     * Returns the sum of all the given integers.
     *
     * @param operands The integer numbers to be summed.
     * @return The sum of all the given integer numbers.
     */
    public static BigInteger add(BigInteger... operands) {
        BigInteger result = null;

        if (operands != null) {
            for (BigInteger operand : operands) {
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
     * Increments the given integer by one.
     *
     * @param integer The integer to be incremented.
     * @return The given integer incremented by one, or one if input is null.
     */
    public static BigInteger increment(BigInteger integer) {
        return add(integer == null ? BigInteger.ZERO : integer, BigInteger.ONE);
    }

    /**
     * Decrements the given integer by one.
     *
     * @param integer The integer to be decremented.
     * @return The given integer decremented by one, or minus one if input is null.
     */
    public static BigInteger decrement(BigInteger integer) {
        return subtract(integer == null ? BigInteger.ZERO : integer, BigInteger.ONE);
    }

    /**
     * Subtracts one integer from another returning the result.
     *
     * @param minuend    The integer to be subtracted from.
     * @param subtrahend The integer to be subtracted.
     * @return The result of subtracting the subtrahend from the minuend.
     */
    public static BigInteger subtract(BigInteger minuend, BigInteger subtrahend) {
        BigInteger result = null;

        if (minuend != null && subtrahend != null) {
            result = minuend.subtract(subtrahend);
        }

        return result;
    }

    /**
     * Returns the multiplication of all the given integers.
     *
     * @param operands The integer numbers to be summed.
     * @return The multiplication of all the given integer numbers.
     */
    public static BigInteger multiply(BigInteger... operands) {
        BigInteger result = null;

        if (operands != null) {
            for (BigInteger operand : operands) {
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
     *
     * @param dividend The integer to be divided.
     * @param divisor  The integer to divide by.
     * @return An array of two integers, the result of the division, and the remainder.
     */
    public static BigInteger[] divideAndRemainder(BigInteger dividend, BigInteger divisor) {
        BigInteger[] result = null;

        if (dividend != null && divisor != null) {
            result = dividend.divideAndRemainder(divisor);
        }

        return result;
    }

    /**
     * Divides the given dividend by the divisor.
     *
     * @param dividend The integer to be divided.
     * @param divisor  The integer to divide by.
     * @return The result of dividing the dividend by the divisor.
     */
    public static BigInteger divide(BigInteger dividend, BigInteger divisor) {
        BigInteger result = null;

        if (dividend != null && divisor != null) {
            result = dividend.divide(divisor);
        }

        return result;
    }

    /**
     * Returns the remainder from dividing the given dividend by the divisor.
     *
     * @param dividend The integer to be divided.
     * @param divisor  The integer to divide by.
     * @return The remainder of dividing the dividend by the divisor.
     */
    public static BigInteger remainder(BigInteger dividend, BigInteger divisor) {
        BigInteger result = null;

        if (dividend != null && divisor != null) {
            result = dividend.remainder(divisor);
        }

        return result;
    }

    /**
     * Returns the exponentiation of the given base raised to power of the given exponent.
     *
     * @param base     The integer base to be raised to the power of the given exponent.
     * @param exponent The exponent to raise the given base to.
     * @return The result of raising the given base to the power of the given exponent.
     */
    public static BigInteger power(BigInteger base, int exponent) {
        if (base == null) return null;
        return base.pow(exponent);
    }

    /**
     * Returns a BigInteger whose value is shifted by the given distance. The shift distance when positive performs a
     * left shift and when negative performs a right shift.
     *
     * @param integer  The integer to be shifted.
     * @param distance The distance to shift the integer.
     * @return The given integer shifted the given distance, left if distance is positive, and right if distance is
     * negative.
     */
    public static BigInteger shift(BigInteger integer, int distance) {
        if (integer == null) return null;
        return integer.shiftLeft(distance);
    }

    /**
     * Returns the largest of the given list of integer numbers.
     *
     * @param integers A list of integer numbers.
     * @return The largest of the given numbers.
     */
    public static BigInteger maximum(BigInteger... integers) {
        BigInteger result = null;

        if (integers != null) {
            for (BigInteger integer : integers) {
                if (integer != null) {
                    if (result == null) {
                        result = integer;
                    } else {
                        result = result.max(integer);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the smallest of the given list of integer numbers.
     *
     * @param integers A list of integer numbers.
     * @return The smallest of the given numbers.
     */
    public static BigInteger minimum(BigInteger... integers) {
        BigInteger result = null;

        if (integers != null) {
            for (BigInteger integer : integers) {
                if (integer != null) {
                    if (result == null) {
                        result = integer;
                    } else {
                        result = result.min(integer);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the average or mean from the given list of integer numbers.
     *
     * @param integers A list of integer numbers.
     * @return The average or mean value of the given list of values.
     */
    public static BigInteger average(BigInteger... integers) {
        BigInteger result = null;

        if (integers != null) {
            int length = 0;
            for (BigInteger integer : integers) {
                if (integer != null) {
                    if (result == null) {
                        result = integer;
                    } else {
                        result = result.add(integer);
                    }
                    length++;
                }
            }
            if (result != null) {
                result = divide(result, BigInteger.valueOf(length));
            }
        }

        return result;
    }

    /**
     * Returns true if the given string can be parsed as a integer number.
     *
     * @param integer The string to validate.
     * @param raise   True if an exception should be thrown if the string is not a valid integer number.
     * @return True if the string can be parsed as a integer number, otherwise false.
     * @throws ServiceException If raise is true and the given string is not a valid integer number.
     */
    public static boolean validate(String integer, boolean raise) throws ServiceException {
        boolean valid = false;
        try {
            if (integer != null) {
                parse(integer);
                valid = true;
            }
        } catch (NumberFormatException ex) {
            if (raise) ExceptionHelper.raise(ex);
        }
        return valid;
    }

    /**
     * Returns true if the given string can be parsed as a integer number.
     *
     * @param integer The string to validate.
     * @return True if the string can be parsed as a integer number, otherwise false.
     */
    public static boolean validate(String integer) {
        boolean result = false;

        try {
            result = validate(integer, false);
        } catch (ServiceException ex) {
            // suppress the exception
        }

        return result;
    }
}
