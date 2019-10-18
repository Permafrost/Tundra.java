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
import com.wm.data.IData;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.math.BigDecimalFormatter;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.LocaleHelper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * A collection of convenience methods for working with decimals.
 */
public final class BigDecimalHelper {

    /**
     * Disallow instantiation of this class.
     */
    private BigDecimalHelper() {}

    /**
     * Parses the given string and returns a decimal representation.
     *
     * @param decimalString A string to be parsed as a decimal.
     * @return              A decimal representation of the given string.
     */
    public static BigDecimal parse(String decimalString) {
        return parse(decimalString, (String)null, (Locale)null);
    }

    /**
     * Parses the given string and returns a decimal representation.
     *
     * @param decimalString  A string to be parsed as a decimal.
     * @param locale         The locale to use if the string is only parseable in this localized format.
     * @return               A decimal representation of the given string.
     */
    public static BigDecimal parse(String decimalString, Locale locale) {
        return parse(decimalString, (String)null, locale);
    }

    /**
     * Parses the given string and returns a decimal representation.
     *
     * @param decimalString   A string to be parsed as a decimal.
     * @param decimalPattern  A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                        string.
     * @return                A decimal representation of the given string.
     */
    public static BigDecimal parse(String decimalString, String decimalPattern) {
        return parse(decimalString, decimalPattern, null);
    }

    /**
     * Parses the given string and returns a decimal representation.
     *
     * @param decimalString  A string to be parsed as a decimal.
     * @param decimalPattern A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                       string.
     * @param locale         The locale to use if the string is only parseable in this localized format.
     * @return               A decimal representation of the given string.
     */
    public static BigDecimal parse(String decimalString, String decimalPattern, Locale locale) {
        if (decimalString == null) return null;

        BigDecimal result;

        if (decimalPattern == null) {
            try {
                result = new BigDecimal(decimalString);
            } catch(NumberFormatException ex) {
                try {
                    // try parsing with the number format for the default locale
                    result = new BigDecimal(NumberFormat.getInstance(LocaleHelper.normalize(locale)).parse(decimalString).doubleValue());
                } catch(ParseException pe) {
                    throw new IllegalArgumentException(MessageFormat.format("Unparseable number: {0} does not conform to the java.math.BigDecimal grammar",  decimalString), pe);
                }
            }
        } else {
            DecimalFormat parser = new DecimalFormat(decimalPattern);
            parser.setParseBigDecimal(true);
            try {
                result = (BigDecimal)parser.parse(decimalString);
            } catch (ParseException ex) {
                throw new IllegalArgumentException(MessageFormat.format("Unparseable number: {0} does not conform to the specified pattern {1}",  decimalString, decimalPattern), ex);

            }
        }

        return result;
    }

    /**
     * Parses the given string and returns a decimal representation.
     *
     * @param decimalString   A string to be parsed as a decimal.
     * @param decimalPatterns A list java.text.DecimalFormat pattern strings one of which describes the format of the
     *                        given decimal string.
     * @return                A decimal representation of the given string.
     */
    public static BigDecimal parse(String decimalString, String[] decimalPatterns) {
        return parse(decimalString, decimalPatterns, null);
    }

    /**
     * Parses the given string and returns a decimal representation.
     *
     * @param decimalString   A string to be parsed as a decimal.
     * @param decimalPatterns A list java.text.DecimalFormat pattern strings one of which describes the format of the
     *                        given decimal string.
     * @param locale          The locale to use if the string is only parseable in this localized format.
     * @return                A decimal representation of the given string.
     */
    public static BigDecimal parse(String decimalString, String[] decimalPatterns, Locale locale) {
        if (decimalString == null) return null;

        BigDecimal result = null;

        if (decimalPatterns == null || decimalPatterns.length == 0) {
            result = parse(decimalString, (String)null, locale);
        } else {
            boolean parsed = false;
            Throwable exception = null;
            for (String decimalPattern : decimalPatterns) {
                try {
                    result = parse(decimalString, decimalPattern, locale);
                    parsed = true;
                    break;
                } catch (IllegalArgumentException ex) {
                    exception = ex;
                }
            }
            if (!parsed) {
                throw new IllegalArgumentException(MessageFormat.format("Unparseable number: {0} does not conform to any of the specified patterns [{1}]",  decimalString, ArrayHelper.join(decimalPatterns, "\", \"")), exception);
            }
        }

        return result;
    }

    /**
     * Parses the given strings and returns their decimal representations.
     *
     * @param decimals One or more strings to be parsed as a decimal.
     * @return         A decimal representation of the given strings.
     */
    public static BigDecimal[] parse(String[] decimals) {
        return parse(decimals, (Locale)null);
    }

    /**
     * Parses the given strings and returns their decimal representations.
     *
     * @param decimals       One or more strings to be parsed as a decimal.
     * @param decimalPattern A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                       string.
     * @return               A decimal representation of the given strings.
     */
    public static BigDecimal[] parse(String[] decimals, String decimalPattern) {
        return parse(decimals, decimalPattern, (Locale)null);
    }

    /**
     * Parses the given strings and returns their decimal representations.
     *
     * @param decimals One or more strings to be parsed as a decimal.
     * @param locale   The locale to use if the string is only parseable in this localized format.
     * @return         A decimal representation of the given strings.
     */
    public static BigDecimal[] parse(String[] decimals, Locale locale) {
        return parse(decimals, (String)null, locale);
    }

    /**
     * Parses the given strings and returns their decimal representations.
     *
     * @param decimalStrings One or more strings to be parsed as a decimal.
     * @param locale         The locale to use if the string is only parseable in this localized format.
     * @param decimalPattern A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                       string.
     * @return               A decimal representation of the given strings.
     */
    public static BigDecimal[] parse(String[] decimalStrings, String decimalPattern, Locale locale) {
        if (decimalStrings == null) return null;

        BigDecimal[] decimals = new BigDecimal[decimalStrings.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = parse(decimalStrings[i], decimalPattern, locale);
        }

        return decimals;
    }

    /**
     * Parses the given strings and returns their decimal representations.
     *
     * @param decimalStrings One or more strings to be parsed as a decimal.
     * @param decimalPatterns A list of java.text.DecimalFormat pattern string one of which describes the format of the
     *                        given decimal strings.
     * @return               A decimal representation of the given strings.
     */
    public static BigDecimal[] parse(String[] decimalStrings, String[] decimalPatterns) {
        return parse(decimalStrings, decimalPatterns, null);
    }

    /**
     * Parses the given strings and returns their decimal representations.
     *
     * @param decimalStrings  One or more strings to be parsed as a decimal.
     * @param locale          The locale to use if the string is only parseable in this localized format.
     * @param decimalPatterns A list of java.text.DecimalFormat pattern string one of which describes the format of the
     *                        given decimal strings.
     * @return                A decimal representation of the given strings.
     */
    public static BigDecimal[] parse(String[] decimalStrings, String[] decimalPatterns, Locale locale) {
        if (decimalStrings == null) return null;

        BigDecimal[] decimals = new BigDecimal[decimalStrings.length];

        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = parse(decimalStrings[i], decimalPatterns, locale);
        }

        return decimals;
    }

    /**
     * Returns a string representation of the given decimal.
     *
     * @param decimal The decimal to convert to a string representation.
     * @return        The string representation of the given decimal.
     */
    public static String emit(BigDecimal decimal) {
        return emit(decimal, (Locale)null);
    }

    /**
     * Returns a string representation of the given decimal.
     *
     * @param decimal The decimal to convert to a string representation.
     * @param locale  The locale to use for emitting a localized number format.
     * @return        The string representation of the given decimal.
     */
    public static String emit(BigDecimal decimal, Locale locale) {
        return emit(decimal, null, locale);
    }

    /**
     * Returns a string representation of the given decimal.
     *
     * @param decimal        The decimal to convert to a string representation.
     * @param decimalPattern A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                       strings.
     * @return               The string representation of the given decimal.
     */
    public static String emit(BigDecimal decimal, String decimalPattern) {
        return emit(decimal, decimalPattern, null);
    }

    /**
     * Returns a string representation of the given decimal.
     *
     * @param decimal        The decimal to convert to a string representation.
     * @param decimalPattern A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                       strings.
     * @param locale         The locale to use for emitting a localized number format, if no pattern is specified.
     * @return               The string representation of the given decimal.
     */
    public static String emit(BigDecimal decimal, String decimalPattern, Locale locale) {
        if (decimal == null) return null;

        String output;

        if (decimalPattern == null) {
            if (locale == null) {
                output = decimal.toPlainString();
            } else {
                output = NumberFormat.getInstance(locale).format(decimal);
            }
        } else {
            output = new DecimalFormat(decimalPattern).format(decimal);
        }

        return output;
    }

    /**
     * Returns a string representation of the given list of decimals.
     *
     * @param decimals The list of decimals to convert to string representations.
     * @return         The string representations of the given list of decimals.
     */
    public static String[] emit(BigDecimal[] decimals) {
        return emit(decimals, (String)null);
    }


    /**
     * Returns a string representation of the given list of decimals.
     *
     * @param decimals The list of decimals to convert to string representations.
     * @param locale   The locale to use for emitting a localized number format, if no pattern is specified.
     * @return         The string representations of the given list of decimals.
     */
    public static String[] emit(BigDecimal[] decimals, Locale locale) {
        return emit(decimals, null, locale);
    }

    /**
     * Returns a string representation of the given list of decimals.
     *
     * @param decimals       The list of decimals to convert to string representations.
     * @param decimalPattern A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                       strings.
     * @return               The string representations of the given list of decimals.
     */
    public static String[] emit(BigDecimal[] decimals, String decimalPattern) {
        return emit(decimals, decimalPattern, null);
    }

    /**
     * Returns a string representation of the given list of decimals.
     *
     * @param decimals       The list of decimals to convert to string representations.
     * @param decimalPattern A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                       strings.
     * @param locale         The locale to use for emitting a localized number format, if no pattern is specified.
     * @return               The string representations of the given list of decimals.
     */
    public static String[] emit(BigDecimal[] decimals, String decimalPattern, Locale locale) {
        if (decimals == null) return null;

        String[] strings = new String[decimals.length];

        for (int i = 0; i < decimals.length; i++) {
            strings[i] = emit(decimals[i], decimalPattern, locale);
        }

        return strings;
    }

    /**
     * Formats the given decimal string according to the given pattern.
     *
     * @param input      The decimal string to be formatted.
     * @param inPattern  A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                   strings.
     * @param outPattern The pattern the input string is reformatted to.
     * @return           The given input string reformatted to the desired pattern.
     */
    public static String format(String input, String inPattern, String outPattern) {
        return emit(parse(input, inPattern), outPattern);
    }

    /**
     * Formats the given decimal string according to the given pattern.
     *
     * @param input      The decimal string to be formatted.
     * @param inPatterns A list of patterns one of which the input string adheres to.
     * @param outPattern The pattern the input string is reformatted to.
     * @return           The given input string reformatted to the desired pattern.
     */
    public static String format(String input, String[] inPatterns, String outPattern) {
        return emit(parse(input, inPatterns), outPattern);
    }

    /**
     * Formats the given decimal strings according to the given pattern.
     *
     * @param input      The decimal strings to be formatted.
     * @param outPattern The pattern the input strings are reformatted to.
     * @param inPattern  A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                   strings.
     * @return           The given input strings reformatted to the desired pattern.
     */
    public static String[] format(String[] input, String inPattern, String outPattern) {
        return emit(parse(input, inPattern), outPattern);
    }

    /**
     * Formats the given decimal strings according to the given pattern.
     *
     * @param input      The decimal strings to be formatted.
     * @param outPattern The pattern the input strings are reformatted to.
     * @param inPatterns A list of patterns one of which the input strings adhere to.
     * @return           The given input strings reformatted to the desired pattern.
     */
    public static String[] format(String[] input, String[] inPatterns, String outPattern) {
        return emit(parse(input, inPatterns), outPattern);
    }

    /**
     * Formats decimal strings in the given IData document according to the given pattern.
     *
     * @param document   The IData document containing decimal strings to be formatted.
     * @param inPattern  A java.text.DecimalFormat pattern string describing the format of the given decimal
     *                   strings.
     * @param outPattern The pattern the input strings are reformatted to.
     * @return           The reformatted decimal strings.
     */
    public static IData format(IData document, String inPattern, String outPattern) {
        return Transformer.transform(document, new BigDecimalFormatter(inPattern, outPattern));
    }

    /**
     * Formats decimal strings in the given IData document according to the given pattern.
     *
     * @param document   The IData document containing decimal strings to be formatted.
     * @param outPattern The pattern the input strings are reformatted to.
     * @param inPatterns A list of patterns one of which the input strings adhere to.
     * @return           The reformatted decimal strings.
     */
    public static IData format(IData document, String[] inPatterns, String outPattern) {
        return Transformer.transform(document, new BigDecimalFormatter(inPatterns, outPattern));
    }

    /**
     * Returns a BigDecimal representation of the given object, if it an instance of java.lang.Number or a
     * java.lang.String which can be parsed as a decimal number.
     *
     * @param object    An object to be converted to a BigDecimal.
     * @return          A BigDecimal representation of the given object.
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
            decimal = parse((String)object);
        }

        return decimal;
    }

    /**
     * Returns BigDecimal representations of the given list of Objects.
     *
     * @param values    The objects to convert to BigDecimal representations.
     * @return          BigDecimal representations of the given objects.
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
     *
     * @param decimal   A decimal number.
     * @return          The absolute value of the given decimal number.
     */
    public static BigDecimal absolute(BigDecimal decimal) {
        return decimal == null ? null : decimal.abs();
    }

    /**
     * Returns the absolute values of the given decimal numbers.
     *
     * @param decimals  A list of decimal numbers.
     * @return          The absolute values of the given list of decimal numbers.
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
     *
     * @param decimal   A decimal number.
     * @return          The negated value of the given decimal number.
     */
    public static BigDecimal negate(BigDecimal decimal) {
        return decimal == null ? null : decimal.negate();
    }

    /**
     * Returns the negated values of the given decimal numbers.
     *
     * @param decimals  A list of decimal numbers.
     * @return          The negated values of the given list of decimal numbers.
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
     *
     * @param decimals      The decimal numbers to be summed.
     * @return              The sum of all the given decimal numbers.
     */
    public static BigDecimal add(BigDecimal... decimals) {
        BigDecimal result = null;

        if (decimals != null) {
            for (BigDecimal operand : decimals) {
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
     *
     * @param minuend       The decimal to be subtracted from.
     * @param subtrahend    The decimal to be subtracted.
     * @return              The result of subtracting the subtrahend from the minuend.
     */
    public static BigDecimal subtract(BigDecimal minuend, BigDecimal subtrahend) {
        BigDecimal result = null;

        if (minuend != null && subtrahend != null) {
            result = minuend.subtract(subtrahend);
        }

        return result;
    }

    /**
     * Returns the multiplication of all the given decimals.
     *
     * @param decimals      The decimal numbers to be summed.
     * @return              The multiplication of all the given decimal numbers.
     */
    public static BigDecimal multiply(BigDecimal... decimals) {
        BigDecimal result = null;

        if (decimals != null) {
            for (BigDecimal operand : decimals) {
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
     * @param dividend      The decimal to be divided.
     * @param divisor       The decimal to divide by.
     * @param precision     The number of decimal places preserved in the result.
     * @param roundingMode  The rounding algorithm to use when rounding the result.
     * @return              The result of dividing the dividend by the divisor.
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int precision, RoundingMode roundingMode) {
        BigDecimal result = null;

        if (dividend != null && divisor != null) {
            result = dividend.divide(divisor, PrecisionHelper.normalize(precision, dividend, divisor), RoundingModeHelper.normalize(roundingMode));
        }

        return result;
    }

    /**
     * Returns the exponentiation of the given base raised to power of the given exponent.
     *
     * @param base      The decimal base to be raised to the power of the given exponent.
     * @param exponent  The exponent to raise the given base to.
     * @return          The result of raising the given base to the power of the given exponent.
     */
    public static BigDecimal power(BigDecimal base, int exponent) {
        return base == null ? null : base.pow(exponent);
    }

    /**
     * Rounds the given decimal to the given precision with the given rounding algorithm.
     *
     * @param decimal       A decimal to be rounded.
     * @param precision     The number of decimal places to round to.
     * @param roundingMode  The rounding algorithm to be used.
     * @return              The given decimal rounded to the given precision using the given algorithm.
     */
    public static BigDecimal round(BigDecimal decimal, int precision, RoundingMode roundingMode) {
        if (precision < 0 || decimal == null) return decimal;
        return decimal.setScale(PrecisionHelper.normalize(precision), RoundingModeHelper.normalize(roundingMode));
    }

    /**
     * Rounds the given list of decimals to the given precision with the given rounding algorithm.
     *
     * @param decimals      A list of decimals to be rounded.
     * @param precision     The number of decimal places to round to.
     * @param roundingMode  The rounding algorithm to be used.
     * @return              The given list of decimals rounded to the given precision using the given algorithm.
     */
    public static BigDecimal[] round(BigDecimal[] decimals, int precision, RoundingMode roundingMode) {
        if (decimals == null) return null;

        BigDecimal[] output = new BigDecimal[decimals.length];

        for (int i = 0; i < decimals.length; i++) {
            output[i] = round(decimals[i], precision, roundingMode);
        }

        return output;
    }

    /**
     * Returns the largest of the given list of decimal numbers.
     *
     * @param decimals      A list of decimal numbers.
     * @return              The largest of the given numbers.
     */
    public static BigDecimal maximum(BigDecimal... decimals) {
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
     *
     * @param decimals      A list of decimal numbers.
     * @return              The smallest of the given numbers.
     */
    public static BigDecimal minimum(BigDecimal... decimals) {
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
     *
     * @param precision     The number of decimal places to round to.
     * @param roundingMode  The rounding algorithm to be used.
     * @param decimals      A list of decimal numbers.
     * @return              The average or mean value of the given list of values.
     */
    public static BigDecimal average(int precision, RoundingMode roundingMode, BigDecimal... decimals) {
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
                result = divide(result, new BigDecimal(length), precision, roundingMode);
            }
        }

        return result;
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     *
     * @param decimal The string to validate.
     * @return        True if the string can be parsed as a decimal number, otherwise false.
     */
    public static boolean validate(String decimal) {
        return validate(decimal, null, null);
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     *
     * @param decimal        The string to validate.
     * @param decimalPattern A java.text.DecimalFormat pattern string describing the format of the given decimal string.
     * @return               True if the string can be parsed as a decimal number, otherwise false.
     */
    public static boolean validate(String decimal, String decimalPattern) {
        return validate(decimal, decimalPattern, null);
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     *
     * @param decimal The string to validate.
     * @param locale  The locale to use if the string is only parseable in this localized format.
     * @return        True if the string can be parsed as a decimal number, otherwise false.
     */
    public static boolean validate(String decimal, Locale locale) {
        return validate(decimal, null, locale);
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     *
     * @param decimal        The string to validate.
     * @param decimalPattern A java.text.DecimalFormat pattern string describing the format of the given decimal string.
     * @param locale         The locale to use if the string is only parseable in this localized format.
     * @return               True if the string can be parsed as a decimal number, otherwise false.
     */
    public static boolean validate(String decimal, String decimalPattern, Locale locale) {
        boolean result = false;

        try {
            result = validate(decimal, decimalPattern, locale, false);
        } catch (ServiceException ex) {
            // suppress the exception, which will never be thrown anyway
        }

        return result;
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     *
     * @param decimal           The string to validate.
     * @param raise             True if an exception should be thrown if the string is not a valid decimal number.
     * @return                  True if the string can be parsed as a decimal number, otherwise false.
     * @throws ServiceException If raise is true and the given string is not a valid decimal number.
     */
    public static boolean validate(String decimal, boolean raise) throws ServiceException {
        return validate(decimal, null, null, raise);
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     *
     * @param decimal           The string to validate.
     * @param decimalPattern    A java.text.DecimalFormat pattern string describing the format of the given decimal string.
     * @param raise             True if an exception should be thrown if the string is not a valid decimal number.
     * @return                  True if the string can be parsed as a decimal number, otherwise false.
     * @throws ServiceException If raise is true and the given string is not a valid decimal number.
     */
    public static boolean validate(String decimal, String decimalPattern, boolean raise) throws ServiceException {
        return validate(decimal, decimalPattern, null, raise);
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     *
     * @param decimal           The string to validate.
     * @param raise             True if an exception should be thrown if the string is not a valid decimal number.
     * @param locale            The locale to use if the string is only parseable in this localized format.
     * @return                  True if the string can be parsed as a decimal number, otherwise false.
     * @throws ServiceException If raise is true and the given string is not a valid decimal number.
     */
    public static boolean validate(String decimal, Locale locale, boolean raise) throws ServiceException {
        return validate(decimal, null, locale, raise);
    }

    /**
     * Returns true if the given string can be parsed as a decimal number.
     *
     * @param decimal           The string to validate.
     * @param decimalPattern    A java.text.DecimalFormat pattern string describing the format of the given decimal string.
     * @param locale            The locale to use if the string is only parseable in this localized format.
     * @param raise             True if an exception should be thrown if the string is not a valid decimal number.
     * @return                  True if the string can be parsed as a decimal number, otherwise false.
     * @throws ServiceException If raise is true and the given string is not a valid decimal number.
     */
    public static boolean validate(String decimal, String decimalPattern, Locale locale, boolean raise) throws ServiceException {
        boolean valid = false;
        try {
            if (decimal != null) {
                parse(decimal, decimalPattern, locale);
                valid = true;
            }
        } catch(Exception ex) {
            if (raise) ExceptionHelper.raise(ex);
        }
        return valid;
    }

    /**
     * Returns true if all given decimals are equal, ignoring scale.
     *
     * @param decimals  The decimals to compare.
     * @return          True if all given decimals are equal, ignoring scale.
     */
    public static boolean equals(BigDecimal... decimals) {
        if (decimals == null || decimals.length <= 1) return true;

        boolean result = false;

        for (int i = 0; i < decimals.length - 1; i++) {
            result = decimals[i].compareTo(decimals[i + 1]) == 0;
            if (!result) break;
        }

        return result;
    }

    /**
     * Returns true if the given decimal is equal to zero, ignoring scale.
     *
     * @param decimal   The decimal to compare to zero.
     * @return          True if the given decimal is equal to zero, ignoring scale.
     */
    public static boolean isZero(BigDecimal decimal) {
        return equals(decimal, BigDecimal.ZERO);
    }
}
