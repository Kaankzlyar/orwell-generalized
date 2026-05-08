package rdf.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import config.Config;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

        Config.OUTPUT_DIR = tempDir.resolve("output");
        Config.SHACL_DIR = tempDir.resolve("shacl");
        Config.THROW_ON_SHACL_UNCONFORM = true;
    }

    @AfterEach
    void tearDown() {
        Config.OUTPUT_DIR = originalOutputPath;
        Config.SHACL_DIR = originalShaclDir;
        Config.THROW_ON_SHACL_UNCONFORM = originalThrowOnShaclUnconform;
    }

    @Test
    void validateLoadsAllOutputGraphsIntoOneModel() throws Exception {
        Files.createDirectories(tempDir.resolve(Config.OUTPUT_DIR));
        Files.createDirectories(tempDir.resolve(Config.SHACL_DIR));

        Path currentGraph = tempDir
            .resolve(Config.OUTPUT_DIR)
            .resolve("graph-xvii.ttl");
        Path previousGraph = tempDir
            .resolve(Config.OUTPUT_DIR)
            .resolve("graph-xvi.ttl");

        Files.writeString(
            currentGraph,
            """
            @prefix ex: <http://example.org/> .
            ex:current a ex:Source ;
                ex:ref ex:sharedTarget .
            """
        );
        Files.writeString(
            previousGraph,
            """
            @prefix ex: <http://example.org/> .
            ex:previous ex:p ex:o .
            """
        );

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(
                new PrintStream(output, true, StandardCharsets.UTF_8)
            );
            new ShaclValidation().validate();
        } finally {
            System.setOut(originalOut);
        }

        String logs = output.toString(StandardCharsets.UTF_8);
        assertTrue(
            logs.contains(
                "Loading graph for SHACL validation: " + previousGraph
            )
        );
        assertTrue(
            logs.contains("Loading graph for SHACL validation: " + currentGraph)
        );
        assertTrue(logs.contains("SHACL validation completed for union graph"));
    }
}
