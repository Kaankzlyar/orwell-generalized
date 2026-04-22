package rdf.mapping.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ToRequisitionTest {

    private static final String NS = "http://purl.org/polis/ar/mp-activity#";

    @Test
    void toRequisitionMapsKnownValues() {
        assertEquals(NS + "Request", ToRequisition.toRequisition("REQ"));
        assertEquals(NS + "Inquiry", ToRequisition.toRequisition("PER"));
    }

    @Test
    void toRequisitionReturnsNullForBlankOrUnknownValues() {
        assertNull(ToRequisition.toRequisition(" "));
        assertNull(ToRequisition.toRequisition("OTHER"));
        assertNull(ToRequisition.toRequisition(null));
    }
}
