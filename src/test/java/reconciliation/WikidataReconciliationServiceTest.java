package reconciliation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class WikidataReconciliationServiceTest {

    private static final String BASE_URI = "http://www.wikidata.org/entity/";
    private static final String ELECTORAL_DISTRICT_TYPE = "Q59576414";

    @Test
    void reconciliatePortoReturnsExpectedWikidataEntity() {
        String actual = WikidataReconciliationService.reconciliate("Porto", ELECTORAL_DISTRICT_TYPE, "10");

        String portoEntity = "Q59193855";

        assertEquals(BASE_URI + portoEntity, actual);
    }

    @Test
    void reconciliateEuropaReturnsWrongWikidataEntity(){
        String actual = WikidataReconciliationService.reconciliate("Europa", ELECTORAL_DISTRICT_TYPE, "10");

        String europaEntity = "Q60160068";

        assertNotEquals(BASE_URI + europaEntity, actual);
    }

    @Test
    void reconciliateEuropaReturnsExpectedWikidataEntity(){
        String actual = WikidataReconciliationService.reconciliate("Círculo Europa", ELECTORAL_DISTRICT_TYPE, "2");

        String europaEntity = "Q60160068";

        assertEquals(BASE_URI + europaEntity, actual);
    }
}
