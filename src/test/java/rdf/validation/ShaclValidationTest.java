package rdf.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import config.Config;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ShaclValidationTest {

    @TempDir
    Path tempDir;

    private Path originalOutputPath;
    private Path originalShaclDir;
    private boolean originalThrowOnShaclUnconform;

    @BeforeEach
    void setUp() {
        originalOutputPath = Config.OUTPUT_PATH;
        originalShaclDir = Config.SHACL_DIR;
        originalThrowOnShaclUnconform = Config.THROW_ON_SHACL_UNCONFORM;

        Config.OUTPUT_PATH = tempDir.resolve("output").resolve("graph.ttl");
        Config.SHACL_DIR = tempDir.resolve("shacl");
        Config.THROW_ON_SHACL_UNCONFORM = true;
    }

    @AfterEach
    void tearDown() {
        Config.OUTPUT_PATH = originalOutputPath;
        Config.SHACL_DIR = originalShaclDir;
        Config.THROW_ON_SHACL_UNCONFORM = originalThrowOnShaclUnconform;
    }

    @Test
    void validateOnlyUsesProvidedGraphPaths() throws Exception {
        Files.createDirectories(Config.OUTPUT_PATH.getParent());
        Files.createDirectories(Config.SHACL_DIR);

        Path currentGraph = Config.OUTPUT_PATH.getParent().resolve("graph-xvii.ttl");
        Path staleGraph = Config.OUTPUT_PATH.getParent().resolve("graph-xvi.ttl");
        Files.writeString(
            Config.SHACL_DIR.resolve("empty.ttl"),
            "@prefix sh: <http://www.w3.org/ns/shacl#> .\n"
        );
        Files.writeString(
            currentGraph,
            "@prefix ex: <http://example.org/> .\nex:current ex:p ex:o .\n"
        );
        Files.writeString(
            staleGraph,
            "@prefix ex: <http://example.org/> .\nex:stale ex:p ex:o .\n"
        );

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            new ShaclValidation().validate(List.of(currentGraph));
        } finally {
            System.setOut(originalOut);
        }

        String logs = output.toString(StandardCharsets.UTF_8);
        assertTrue(logs.contains("Validating graph: " + currentGraph));
        assertFalse(logs.contains("Validating graph: " + staleGraph));
    }
}
