package reconciliation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WikidataReconciliationServiceTest {

    private static final String BASE_URI = "http://www.wikidata.org/entity/";

    @Test
    void reconciliatePortoReturnsExpectedWikidataEntity() {
        WikidataReconciliationService service = new WikidataReconciliationService();

        String actual = service.reconciliate("porto");

        assertEquals(BASE_URI + "Q36433", actual);
    }
}
