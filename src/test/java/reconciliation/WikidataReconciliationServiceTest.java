package reconciliation;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WikidataReconciliationServiceTest {

    private static final String BASE_URI = "http://www.wikidata.org/entity/";

    @Test
    void reconciliatePortoReturnsExpectedWikidataEntity() {
        WikidataReconciliationService service = new WikidataReconciliationService();

        URI actual = service.reconciliate("porto");

        assertEquals(URI.create(BASE_URI + "Q36433"), actual);
    }
}
