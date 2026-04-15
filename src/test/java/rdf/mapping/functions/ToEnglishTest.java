package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToEnglishTest {

    @Test
    void toEnglishNormalizesSituationInput() {
        assertEquals("Withdrawal", ToEnglish.toEnglish("  Desístência  ", "situation"));
    }

    @Test
    void toEnglishNormalizesDutyInput() {
        assertEquals("VicePAR", ToEnglish.toEnglish("VícE-PrésiDéntE", "duty"));
    }

    @Test 
    void toEnglishNormalizesSituationInputWithSlash() {
        assertEquals("Deceased", ToEnglish.toEnglish("Falecido/a", "situation"));
    }

    @Test
    void toEnglishReturnsNormalizedUnknownValue() {
        assertEquals("nao mapeado", ToEnglish.toEnglish("NãO Mapeádo", "situation"));
    }
}