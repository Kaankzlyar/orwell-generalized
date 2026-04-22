package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ToBooleanTest {

    @Test
    void toBooleanParsesSupportedValues() {
        assertEquals(Boolean.TRUE, ToBoolean.toBoolean("S"));
        assertEquals(Boolean.FALSE, ToBoolean.toBoolean("n"));
    }

    @Test
    void toBooleanReturnsNullForBlankOrUnknownValues() {
        assertNull(ToBoolean.toBoolean(" "));
        assertNull(ToBoolean.toBoolean("x"));
        assertNull(ToBoolean.toBoolean(null));
    }
}
