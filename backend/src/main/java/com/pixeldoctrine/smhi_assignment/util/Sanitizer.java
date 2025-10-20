package com.pixeldoctrine.smhi_assignment.util;

public class Sanitizer {

    public static String sanitizeShort(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() > 100) {
            // clip string *before* regex, to avoid performance hit on DoS attacks
            s = s.substring(0, 100);
        }
        return s.replaceAll("[^a-zA-Z0-9_\\-]", "");
    }
}
