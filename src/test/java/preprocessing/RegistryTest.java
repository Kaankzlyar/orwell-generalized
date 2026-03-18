package preprocessing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import config.Config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegistryTest {

    private Registry registry;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        registry = new Registry();
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        Path resourceDir = dataDir.resolve("testresource");
        Files.createDirectories(resourceDir);
        Path testFile = resourceDir.resolve("test.xml");
        Files.writeString(testFile, "<root><item><id>1</id><name>Test</name></item></root>");
        
        Config.DATA_DIR = dataDir;
    }

    @Test
    void newRegistryHasEmptyHooksList() {
        assertTrue(registry.getHooks().isEmpty());
    }

    @Test
    void registerSingleHookAddsToList() {
        TestHook hook = new TestHook();
        registry.register(hook);
        
        assertEquals(1, registry.getHooks().size());
        assertSame(hook, registry.getHooks().get(0));
    }

    @Test
    void registerMultipleHooksAddsAll() {
        TestHook hook1 = new TestHook();
        TestHook hook2 = new TestHook();
        registry.register(hook1, hook2);
        
        assertEquals(2, registry.getHooks().size());
    }

    @Test
    void runExecutesAllRegisteredHooks() {
        TestHook hook1 = new TestHook();
        TestHook hook2 = new TestHook();
        registry.register(hook1, hook2);
        
        registry.run();
        
        assertTrue(hook1.executed);
        assertTrue(hook2.executed);
    }

    @Test
    void runPassesSameContextToAllHooks() {
        TestHook hook1 = new TestHook();
        TestHook hook2 = new TestHook();
        registry.register(hook1, hook2);
        
        registry.run();
        
        assertSame(hook1.context, hook2.context);
    }

    @Test
    void runDoesNothingWithNoHooks() {
        registry.run();
        
        assertNotNull(registry.getContext());
        assertTrue(registry.getLookupTable().isEmpty());
    }

    @Test
    void getLookupTableReturnsContextLookupTable() {
        TestHook hook = new TestHook();
        registry.register(hook);
        registry.run();
        
        Map<String, Map<String, String>> lookupTable = registry.getLookupTable();
        assertNotNull(lookupTable);
    }

    @Test
    void contextIsNullBeforeRun() {
        assertNull(registry.getContext());
    }

    private class TestHook extends Hook {
        boolean executed = false;
        ProcessingContext context;

        @Override
        public void execute(ProcessingContext context) {
            this.executed = true;
            this.context = context;
        }

        @Override
        public String getName() {
            return "TestHook";
        }
    }
}
