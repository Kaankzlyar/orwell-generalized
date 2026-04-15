package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToEnglishTest {

    private static final String NS = "http://purl.org/polis/ar/core#";

    @Test
    void toEnglishNormalizesSituationInput() {
        assertEquals(NS + "Withdrawal", ToEnglish.toEnglish("  Desístência  ", "situation"));
    }

    @Test
    void toEnglishNormalizesDutyInput() {
        assertEquals(NS + "VicePAR", ToEnglish.toEnglish("VícE-PrésiDéntE", "duty"));
    }

    @Test 
    void toEnglishNormalizesSituationInputWithSlash() {
        assertEquals(NS + "Deceased", ToEnglish.toEnglish("Falecido/a", "situation"));
    }

    @Test
    void toEnglishReturnsNormalizedUnknownValue() {
        assertEquals(NS + "nao mapeado", ToEnglish.toEnglish("NãO Mapeádo", "situation"));
    }
}