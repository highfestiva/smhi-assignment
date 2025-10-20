package com.pixeldoctrine.smhi_assignment.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SanitizerTest {
    @Test
    void testSanitizeShort() {
        assertEquals("usernameis-me", Sanitizer.sanitizeShort("$user.name/%¤#\"!is-me"));
    }
}
