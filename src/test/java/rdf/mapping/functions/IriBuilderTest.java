package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IriBuilderTest {

    @Test
    void prefixedIriBuildsWhenValueIsPresent() {
        assertEquals("urn:test:value", IriBuilder.prefixedIri("urn:test:", " value "));
    }

    @Test
    void prefixedIriReturnsNullWhenValueIsMissing() {
        assertNull(IriBuilder.prefixedIri("urn:test:", null));
        assertNull(IriBuilder.prefixedIri("urn:test:", " "));
    }

    @Test
    void parliamentaryActivityIriBuildsWhenAllPartsArePresent() {
        assertEquals(
                "http://purl.org/polis/ar/graph#ParliamentaryActivity_REQ_123_of_XVII",
                IriBuilder.parliamentaryActivityIri("REQ", "123", "XVII")
        );
    }

    @Test
    void parliamentaryActivityIriReturnsNullWhenAnyPartIsMissing() {
        assertNull(IriBuilder.parliamentaryActivityIri(null, "123", "XVII"));
        assertNull(IriBuilder.parliamentaryActivityIri("REQ", " ", "XVII"));
        assertNull(IriBuilder.parliamentaryActivityIri("REQ", "123", ""));
    }
}
