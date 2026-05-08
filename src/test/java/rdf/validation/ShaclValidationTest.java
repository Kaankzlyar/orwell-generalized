package rdf.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import config.Config;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ShaclValidationTest {

    @TempDir
    Path tempDir;

    private Path originalOutputDir;
    private Path originalShaclDir;

    @BeforeEach
    void setUp() {
        originalOutputDir = Config.OUTPUT_DIR;
        originalShaclDir = Config.SHACL_DIR;

        Config.OUTPUT_DIR = tempDir.resolve("output");
        Config.SHACL_DIR = tempDir.resolve("shacl");
    }

    @AfterEach
    void tearDown() {
        Config.OUTPUT_DIR = originalOutputDir;
        Config.SHACL_DIR = originalShaclDir;
    }

    @Test
    void validateValidatesGraphAgainstShaclShapes() throws Exception {
        Files.createDirectories(Config.SHACL_DIR);

        Path shapeFile = Config.SHACL_DIR.resolve("test-shape.ttl");
        Files.writeString(
            shapeFile,
            """
            @prefix sh: <http://www.w3.org/ns/shacl#> .
            @prefix ex: <http://example.org/> .
            ex:TestShape a sh:NodeShape ;
                sh:targetClass ex:Source .
            """
        );

        Model graph = ModelFactory.createDefaultModel();
        graph.add(
            graph.createResource("http://example.org/s1"),
            ResourceFactory.createProperty("http://example.org/type"),
            graph.createResource("http://example.org/Source")
        );

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(
                new PrintStream(output, true, StandardCharsets.UTF_8)
            );
            ShaclValidation.validate(graph);
        } finally {
            System.setOut(originalOut);
        }

        String logs = output.toString(StandardCharsets.UTF_8);
        assertTrue(
            logs.contains("[SHACL Validation] Reading SHACL shapes from: " + shapeFile)
        );
        assertTrue(logs.contains("[SHACL Validation] SHACL validation completed"));
    }
}
