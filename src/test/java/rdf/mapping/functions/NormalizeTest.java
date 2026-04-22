package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NormalizeTest {

    @Test
    void normalizeReturnsNullForNullInput() {
        assertNull(Normalize.normalize(null));
    }

    @Test
    void normalizeRemovesSpecialCharactersAndHyphenatesSpaces() {
        assertEquals("ola-mundoteste-a", Normalize.normalize("  Olá, Mundo!/Teste; \"A\"  "));
    }

    @Test
    void normalizeRemovesDiacriticsAndCollapsesWhitespace() {
        assertEquals("joao-dasilva", Normalize.normalize("  João   da-Silva!!!  "));
    }
}
