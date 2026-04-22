package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrimSafeTest {

    @Test
    void trimSafeReturnsNullForNullInput() {
        assertNull(TrimSafe.trimSafe(null));
    }

    @Test
    void trimSafeTrimsLeadingAndTrailingWhitespace() {
        assertEquals("value", TrimSafe.trimSafe("  value  "));
    }

    @Test
    void trimSafePreservesBlankResultAsEmptyString() {
        assertEquals("", TrimSafe.trimSafe("   "));
    }
}
