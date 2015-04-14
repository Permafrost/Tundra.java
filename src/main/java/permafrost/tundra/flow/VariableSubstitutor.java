package permafrost.tundra.flow;

import com.wm.data.IData;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import permafrost.tundra.data.IDataHelper;

/**
 * Performs webMethods Integration Server flow language variable substitution on
 * percent-delimited strings.
 */
public class VariableSubstitutor {
    /**
     * A regular expression pattern for detecting variable substitution statements in strings.
     */
    protected static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("%([^%]+)%");
    protected String substitutionString, defaultValue;

    /**
     * Constructs a new VariableSubstitutor object for performing variable substitution on
     * the given string by replacing all occurrences of substrings matching "%key%" with
     * the associated value from a specified scope.
     * @param substitutionString    A string to perform variable substitution on.
     */
    public VariableSubstitutor(String substitutionString) {
        this(substitutionString, null);
    }

    /**
     * Constructs a new VariableSubstitutor object for performing variable substitution on the given string by
     * replacing all occurrences of substrings matching "%key%" with the associated value from a specified scope;
     * if the key has no value, the given defaultValue (if not null) is used instead.
     * @param substitutionString    A string to perform variable substitution on.
     * @param defaultValue          A default value to be substituted when the variable being substituted
     *                              has a value of null.
     */
    public VariableSubstitutor(String substitutionString, String defaultValue) {
        if (substitutionString == null) throw new IllegalArgumentException("substitutionString must not be null");
        this.substitutionString = substitutionString;
        this.defaultValue = defaultValue;
    }

    /**
     * Performs variable substitution by replacing all occurrences of substrings matching "%key%" with the
     * associated value from the given scope.
     * @param scope                 An IData document containing the variables being substituted.
     * @return                      The string after variable substitution has been performed.
     */
    public String substitute(IData scope) {
        if (substitutionString == null || scope == null) return substitutionString;

        Matcher matcher = matcher(substitutionString);
        StringBuffer output = new StringBuffer();

        while(matcher.find()) {
            String key = matcher.group(1);
            Object value = IDataHelper.get(scope, key);

            if (value != null && value instanceof String) {
                matcher.appendReplacement(output, Matcher.quoteReplacement((String)value));
            } else if (value == null && defaultValue != null) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(defaultValue));
            } else {
                matcher.appendReplacement(output, Matcher.quoteReplacement(matcher.group(0)));
            }
        }

        matcher.appendTail(output);
        return output.toString();
    }

    /**
     * Returns a regular expression matcher which matches percent-delimited variable substitution statements.
     * @return A regular expression matcher which returns match results for variable substitution statements.
     */
    public Matcher matcher() {
        return matcher(this.substitutionString);
    }

    /**
     * Returns a regular expression matcher which matches percent-delimited variable substitution statements
     * in the given string.
     * @param substitutionString The string to match against.
     * @return                   A regular expression matcher which returns match results for variable
     *                           substitution statements.
     */
    public static Matcher matcher(String substitutionString) {
        return SUBSTITUTION_PATTERN.matcher(substitutionString);
    }

    /**
     * Performs variable substitution on the given string by replacing all occurrences
     * of substrings matching "%key%" with the associated value from the given scope.
     * @param substitutionString    A string to perform variable substitution on.
     * @param scope                 An IData document containing the variables being substituted.
     * @return                      The string after variable substitution has been performed.
     */
    public static String substitute(String substitutionString, IData scope) {
        return substitute(substitutionString, null, scope);
    }

    /**
     * Performs variable substitution on the given string by replacing all occurrences
     * of substrings matching "%key%" with the associated value from the given scope;
     * if the key has no value, the given defaultValue (if not null) is used instead.
     * @param substitutionString    A string to perform variable substitution on.
     * @param scope                 An IData document containing the variables being substituted.
     * @param defaultValue          A default value to be substituted when the variable being substituted
     *                              has a value of null.
     * @return                      The string after variable substitution has been performed.
     */
    public static String substitute(String substitutionString, String defaultValue, IData scope) {
        VariableSubstitutor substitutor = new VariableSubstitutor(substitutionString, defaultValue);
        return substitutor.substitute(scope);
    }
}
