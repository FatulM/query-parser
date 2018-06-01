package com.github.fatulm.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Some text utilities
 */
class TextUtils {
    private TextUtils() {
    }

    /**
     * has some differences with {@link String#split(String)}
     *
     * @param str string which we want to split
     * @param c   splitter
     * @return list of  parts
     */
    public static List<String> stringSplit(String str, char c) {
        List<String> output = new ArrayList<>();

        stringSplit0(str, c, output);

        return output;
    }

    /**
     * @param str      string which we want to split
     * @param splitter splitter character including only one character
     * @param array    list of parts
     */
    private static void stringSplit0(String str, char splitter, List<String> array) {
        int index = str.indexOf(splitter);

        if (index == -1) {
            array.add(str);
            return;
        }

        String first = (index == 0) ? "" : str.substring(0, index);
        String second = (index == str.length() - 1) ? "" : str.substring(index + 1);

        array.add(first);
        stringSplit0(second, splitter, array);
    }

    /**
     * if input is not null and is empty converts it to null
     *
     * @param str inout string
     * @return converted string
     */
    public static String convertEmptyStringToNull(String str) {
        return str == null || str.isEmpty() ? null : str;
    }
}
