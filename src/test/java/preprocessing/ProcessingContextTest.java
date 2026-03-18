package preprocessing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingContextTest {

    private ProcessingContext context;

    @BeforeEach
    void setUp() {
        context = new ProcessingContext();
    }

    @Test
    void newContextHasEmptyLookupTable() {
        assertTrue(context.getLookupTable().isEmpty());
    }

    @Test
    void registerLookupTableAddsTable() {
        context.registerLookupTable("hook1", Map.of("key1", "value1"));
        
        assertTrue(context.getLookupTable().containsKey("hook1"));
        assertEquals(Map.of("key1", "value1"), context.getLookupTable("hook1").orElseThrow());
    }

    @Test
    void getLookupTableReturnsEmptyOptionalForUnknownHook() {
        assertTrue(context.getLookupTable("unknown").isEmpty());
    }

    @Test
    void registerLookupTableOverwritesExistingTable() {
        context.registerLookupTable("hook1", Map.of("key1", "value1"));
        context.registerLookupTable("hook1", Map.of("key2", "value2"));
        
        assertEquals(Map.of("key2", "value2"), context.getLookupTable("hook1").orElseThrow());
    }

    @Test
    void multipleHooksHaveSeparateLookupTables() {
        context.registerLookupTable("hook1", Map.of("key1", "value1"));
        context.registerLookupTable("hook2", Map.of("key2", "value2"));
        
        assertEquals(Map.of("key1", "value1"), context.getLookupTable("hook1").orElseThrow());
        assertEquals(Map.of("key2", "value2"), context.getLookupTable("hook2").orElseThrow());
    }

    @Test
    void getLookupTableReturnsUnmodifiableView() {
        context.registerLookupTable("hook1", Map.of("key1", "value1"));
        Map<String, String> table = context.getLookupTable("hook1").orElseThrow();
        
        assertThrows(UnsupportedOperationException.class, () -> table.put("key2", "value2"));
    }
}
