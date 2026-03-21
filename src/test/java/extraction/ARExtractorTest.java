package extraction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ARExtractorTest {

    @TempDir
    Path tempDir;

    @Test
    void parseConfigParsesNestedStructure() throws IOException {
        Path sourceFile = tempDir.resolve("ar.json");
        String json = """
            {
                "informacaobase": {
                    "xvii": "www.parlamento.com/xvii"
                }
            }
            """;
        Files.writeString(sourceFile, json);

        TestableARExtractor extractor = new TestableARExtractor(sourceFile);
        Map<String, Map<String, URI>> result = extractor.parseConfig(sourceFile);

        assertEquals(1, result.size());
        assertTrue(result.containsKey("informacaobase"));
        assertEquals(1, result.get("informacaobase").size());
        assertEquals(URI.create("www.parlamento.com/xvii"), result.get("informacaobase").get("xvii"));
    }

    @Test
    void parseConfigParsesMultipleDatasetsAndLegislatures() throws IOException {
        Path sourceFile = tempDir.resolve("ar.json");
        String json = """
            {
                "informacaobase": {
                    "xvii": "www.parlamento.com/xvii",
                    "xvi": "www.parlamento.com/xvi"
                },
                "iniciativas": {
                    "xvii": "www.parlamento.com/iniciativas/xvii"
                }
            }
            """;
        Files.writeString(sourceFile, json);

        TestableARExtractor extractor = new TestableARExtractor(sourceFile);
        Map<String, Map<String, URI>> result = extractor.parseConfig(sourceFile);

        assertEquals(2, result.size());
        assertEquals(2, result.get("informacaobase").size());
        assertEquals(1, result.get("iniciativas").size());
    }

    @Test
    void parseConfigThrowsWhenFileMissing() {
        TestableARExtractor extractor = new TestableARExtractor(Path.of("nonexistent.json"));
        assertThrows(IllegalStateException.class, () -> extractor.parseConfig(Path.of("nonexistent.json")));
    }

    @Test
    void parseConfigThrowsWhenUrlIsNotTextual() throws IOException {
        Path sourceFile = tempDir.resolve("ar.json");
        String json = """
            {
                "informacaobase": {
                    "xvii": 123
                }
            }
            """;
        Files.writeString(sourceFile, json);

        TestableARExtractor extractor = new TestableARExtractor(sourceFile);
        assertThrows(IllegalStateException.class, () -> extractor.parseConfig(sourceFile));
    }

    @Test
    void parseConfigThrowsWhenUrlIsEmpty() throws IOException {
        Path sourceFile = tempDir.resolve("ar.json");
        String json = """
            {
                "informacaobase": {
                    "xvii": ""
                }
            }
            """;
        Files.writeString(sourceFile, json);

        TestableARExtractor extractor = new TestableARExtractor(sourceFile);
        assertThrows(IllegalStateException.class, () -> extractor.parseConfig(sourceFile));
    }

    private static class TestableARExtractor extends ARExtractor {
        private final Path testSourcePath;

        TestableARExtractor(Path testSourcePath) {
            this.testSourcePath = testSourcePath;
        }

        @Override
        protected Path SOURCE_PATH() {
            return testSourcePath;
        }
    }
}
