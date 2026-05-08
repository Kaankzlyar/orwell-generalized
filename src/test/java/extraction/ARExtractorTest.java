package extraction;

import config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ARExtractorTest {

    @TempDir
    Path tempDir;

    private Set<String> originalDisabledLegislatures;

    @BeforeEach
    void setUp() {
        originalDisabledLegislatures = Config.DISABLED_LEGISLATURES;
        Config.DISABLED_LEGISLATURES = Collections.emptySet();
    }

    @AfterEach
    void tearDown() {
        Config.DISABLED_LEGISLATURES = originalDisabledLegislatures;
    }

    @Test
    void parseSourcesParsesNestedStructure() throws IOException {
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
        List<SourceNode> result = extractor.parseSources(sourceFile);

        assertEquals(1, result.size());
        SourceNode.SourceObject dataset = (SourceNode.SourceObject) result.get(0);
        assertEquals("informacaobase", dataset.key());
        assertEquals(1, dataset.children().size());
        SourceNode.SourceValue legislature = (SourceNode.SourceValue) dataset.children().get(0);
        assertEquals("xvii", legislature.key());
        assertEquals(URI.create("www.parlamento.com/xvii"), legislature.uri());
    }

    @Test
    void parseSourcesParsesMultipleDatasetsAndLegislatures() throws IOException {
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
        List<SourceNode> result = extractor.parseSources(sourceFile);

        assertEquals(2, result.size());
        SourceNode.SourceObject informacaobase = (SourceNode.SourceObject) result.get(0);
        assertEquals("informacaobase", informacaobase.key());
        assertEquals(2, informacaobase.children().size());
        SourceNode.SourceObject iniciativas = (SourceNode.SourceObject) result.get(1);
        assertEquals("iniciativas", iniciativas.key());
        assertEquals(1, iniciativas.children().size());
    }

    @Test
    void parseSourcesThrowsWhenFileMissing() {
        TestableARExtractor extractor = new TestableARExtractor(Path.of("nonexistent.json"));
        assertThrows(IllegalStateException.class, () -> extractor.parseSources(Path.of("nonexistent.json")));
    }

    @Test
    void parseSourcesThrowsWhenUrlIsNotTextual() throws IOException {
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
        assertThrows(IllegalStateException.class, () -> extractor.parseSources(sourceFile));
    }

    @Test
    void parseSourcesThrowsWhenUrlIsEmpty() throws IOException {
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
        assertThrows(IllegalStateException.class, () -> extractor.parseSources(sourceFile));
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
