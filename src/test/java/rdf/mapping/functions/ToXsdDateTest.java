package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ToXsdDateTest {

    @Test
    void toXsdDateConvertsDdMmYyyyToXsdDate() {
        assertEquals("2024-04-05", ToXsdDate.toXsdDate("05-04-2024"));
    }

    @Test
    void toXsdDateReturnsNullForInvalidDate() {
        assertNull(ToXsdDate.toXsdDate("31-02-2024"));
    }

    @Test
    void toXsdDateReturnsNullForBlankOrNullInput() {
        assertNull(ToXsdDate.toXsdDate("   "));
        assertNull(ToXsdDate.toXsdDate(null));
    }
}