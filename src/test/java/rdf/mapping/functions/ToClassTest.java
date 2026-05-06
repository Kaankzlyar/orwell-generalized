package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToClassTest {

    private static final String NS = "http://purl.org/polis/ar/core#";

    @Test
    void toClassNormalizesSituationInput() {
        assertEquals(NS + "Withdrawal", ToClass.toClass("  Desístência  ", "situation"));
    }

    @Test
    void toClassNormalizesDutyInput() {
        assertEquals(NS + "VicePAR", ToClass.toClass("VícE-PrésiDéntE", "duty"));
    }

    @Test 
    void toClassNormalizesSituationInputWithSlash() {
        assertEquals(NS + "Deceased", ToClass.toClass("Falecido/a", "situation"));
    }

    @Test
    void toClassReturnsNormalizedUnknownValue() {
        assertEquals("nao mapeado", ToClass.toClass("NãO Mapeádo", "situation"));
    }
}
