package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToDeterministicUriTest {

    @Test
    void toDeterministicUriReturnsStableValueForSameInput() {
        String first = ToDeterministicUri.toDeterministicUri(
            "YPA", "AR", "Debate na Escola", "2025-01-14", "2304.0", "Escola Secundaria", "Lisboa"
        );

        String second = ToDeterministicUri.toDeterministicUri(
            "YPA", "AR", "Debate na Escola", "2025-01-14", "2304.0", "Escola Secundaria", "Lisboa"
        );

        assertEquals(first, second);
        assertTrue(first.startsWith("http://purl.org/polis/ar/graph#YPA_"));
    }

    @Test
    void toDeterministicUriNormalizesWhitespaceCaseAndAccents() {
        String normalized = ToDeterministicUri.toDeterministicUri(
            "YPA", "AR", "Debate na Escola", "2025-01-14", "2304.0", "Escola Secundaria", "Lisboa"
        );

        String noisy = ToDeterministicUri.toDeterministicUri(
            "YPA", "ar", "  DEBATE   NA   ESCOLA  ", "2025-01-14", "2304.0", "Escola Secundária", " Lisboa "
        );

        assertEquals(normalized, noisy);
    }

    @Test
    void toDeterministicUriChangesWhenKeysChange() {
        String base = ToDeterministicUri.toDeterministicUri(
            "YPA", "AR", "Debate na Escola", "2025-01-14", "2304.0", "Escola Secundaria", "Lisboa"
        );

        String changed = ToDeterministicUri.toDeterministicUri(
            "YPA", "AR", "Debate na Escola", "2025-01-14", "2304.0", "Escola Secundaria", "Porto"
        );

        assertNotEquals(base, changed);
    }

    @Test
    void toDeterministicUriSupportsSingleKey() {
        String first = ToDeterministicUri.toDeterministicUri("YPA", "AR", "2304.0");
        String second = ToDeterministicUri.toDeterministicUri("YPA", "AR", "2304.0");

        assertEquals(first, second);
        assertTrue(first.startsWith("http://purl.org/polis/ar/graph#YPA_"));
    }

    @Test
    void toDeterministicUriIgnoresMissingKeysInVarargsMode() {
        String sparse = ToDeterministicUri.toDeterministicUri(
            "YPA", "AR", "Debate", null, "", "   ", "Lisboa"
        );

        String compact = ToDeterministicUri.toDeterministicUri(
            "YPA", "AR", "Debate", "Lisboa"
        );

        assertEquals(sparse, compact);
    }
}