package org.sv.webcrawler.util;

public class Utils {

    public static final String DOUBLE_QUOTE = "\"";
    public static final String SPACE = " ";
    public static final String COMMA = ",";
    public static final String COMMA_SPACE = COMMA + SPACE;
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    public static final String WWW = "www";
    public static final String HREF = "href";
    public static final String SRC = "src";

    // Set of values that imply a true value.
    private static final String[] trueValues = {"Y", "YES", "TRUE", "T"};

    // Set of values that imply a false value.
    private static final String[] falseValues = {"N", "NO", "FALSE", "F"};

    /**
     * return true if param has non-null value
     *
     * @param item string to be checked
     * @return boolean status of operation
     */
    public static boolean hasValue(String item) {
        return ((item != null) && (item.length() > 0));
    }

    /**
     * Return the boolean equivalent of the string argument.
     *
     * @param value Value containing string representation of a boolean value.
     * @return Boolean true/false depending on the value of the input.
     * @throws Exception Thrown if input does not have a valid value.
     */
    public static boolean getBoolean(String value) throws Exception {
        if (!hasValue(value)) {
            throw new Exception("ERROR: Can't convert a null/empty string value to a boolean.");
        }

        value = value.trim();

        for (String trueValue : trueValues) {
            if (value.equalsIgnoreCase(trueValue)) {
                return true;
            }
        }

        for (String falseValue1 : falseValues) {
            if (value.equalsIgnoreCase(falseValue1)) {
                return false;
            }
        }

        //Construct error message containing list of valid values
        StringBuilder validValues = new StringBuilder();

        for (int Ix = 0; Ix < trueValues.length; Ix++) {
            if (Ix > 0) {
                validValues.append(", ");
            }

            validValues.append(trueValues[Ix]);
        }

        for (String falseValue : falseValues) {
            validValues.append(", ");
            validValues.append(falseValue);
        }

        throw new Exception("ERROR: Candidate boolean value [" + value
            + "] not in valid-value set [" + validValues.toString() + "].");
    }


    /**
     * Return the boolean equivalent of the string argument.
     *
     * @param value       Value containing string representation of a boolean value.
     * @param defaultBool Default boolean to use if the value is empty
     *                    or if it is an invalid value.
     * @return Boolean true/false depending on the value of the input.
     * @throws Exception Thrown if input does not have a valid value.
     */
    public static boolean getBoolean(String value, boolean defaultBool) throws Exception {
        if (!hasValue(value)) {
            return defaultBool;
        }

        try {
            return getBoolean(value);
        } catch (Exception e) {
            return defaultBool;
        }
    }

    /**
     * returns true if char is numeric, else false
     *
     * @param ch char to check
     * @return boolean status of operation
     */
    public static boolean isNumeric(char ch) {
        //final String log = "isNumeric: ";
        int zero = (int) '0';
        int nine = (int) '9';
        int chVal = (int) ch;
        if (chVal <= nine && chVal >= zero) {
            //printMsg ( log + "Return TRUE for ch [" + ch + "]" );
            return true;
        }
        //printMsg ( log + "Return FALSE for ch [" + ch + "]" );
        return false;
    }
}
