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

    /**
     * return true if param has non-null value
     *
     * @param item string to be checked
     * @return boolean status of operation
     */
    public static boolean hasValue(String item) {
        return ((item != null) && (item.length() > 0));
    }


    public static String getCurrentDir () {
        return System.getProperty("user.dir");
    }
}
