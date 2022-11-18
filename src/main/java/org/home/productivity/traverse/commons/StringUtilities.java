package org.home.productivity.traverse.commons;

import java.util.Arrays;

public class StringUtilities {

    /**
     * Checks if given string starts with any given prefixes
     * 
     * @param string
     * @param prefixes
     * @return
     */
    public static boolean startsWithIn(String string, String[] prefixes) {
        return Arrays.stream(prefixes).anyMatch(prefix -> string.startsWith(prefix));
    }
}
