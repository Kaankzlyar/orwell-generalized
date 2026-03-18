package reconciliation;

import config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReconciliationCacheTest {

    @TempDir
    Path tempDir;

    private Path originalCachePath;

    @BeforeEach
    void setUp() throws IOException {
        originalCachePath = Config.CACHE_PATH;
        Config.CACHE_PATH = tempDir.resolve("cache.properties");
    }

    @AfterEach
    void tearDown() {
        Config.CACHE_PATH = originalCachePath;
    }

    @Test
    void loadReadsCacheFromFile() throws IOException {
        String cacheContent = """
            # Wikidata reconciliation cache
            query1=Q123
            """;
        Files.writeString(Config.CACHE_PATH, cacheContent);
        
        String result = WikidataReconciliationService.reconciliate("query1", "Q1", "1");
        
        assertEquals("http://www.wikidata.org/entity/Q123", result);
    }

    @Test
    void loadIgnoresBlankValues() throws IOException {
        String cacheContent = """
            query1=Q123
            query2=   
            """;
        Files.writeString(Config.CACHE_PATH, cacheContent);
        
        String result1 = WikidataReconciliationService.reconciliate("query1", "Q1", "1");
        String result2 = WikidataReconciliationService.reconciliate("query2", "Q1", "1");
        
        assertEquals("http://www.wikidata.org/entity/Q123", result1);
        assertNull(result2);
    }

    @Test
    void persistWritesToCacheFile() throws IOException {
        WikidataReconciliationService.reconciliate("portotest", "Q1", "1");
        WikidataReconciliationService.persistCache();
        
        assertTrue(Files.exists(Config.CACHE_PATH));
    }

    @Test
    void persistHandlesEmptyCache() {
        assertDoesNotThrow(() -> WikidataReconciliationService.persistCache());
    }
}
