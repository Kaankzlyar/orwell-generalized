package rdf.validation;

import config.Config;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

public class ShaclValidation {

    public static void validate(Model graph) {
        if (!Files.isDirectory(Config.SHACL_DIR)) {
            throw new IllegalStateException(
                "[SHACL Validation] SHACL directory not found: " +
                    Config.SHACL_DIR
            );
        }

        // Load SHACL shapes from the SHACL directory
        Model shapes = ModelFactory.createDefaultModel();
        try (var paths = Files.list(Config.SHACL_DIR)) {
            paths.forEach(path -> {
                System.out.println(
                    "[SHACL Validation] Reading SHACL shapes from: " + path
                );
                RDFDataMgr.read(shapes, path.toString());
            });
        } catch (IOException e) {
            throw new RuntimeException(
                "[SHACL Validation] Failed to read SHACL shapes from: " +
                    Config.SHACL_DIR,
                e
            );
        }

        // Validate the graph against the loaded SHACL shapes
        ValidationReport report = ShaclValidator.get().validate(
            shapes.getGraph(),
            graph.getGraph()
        );

        System.out.println("[SHACL Validation] SHACL validation completed");

        if (Config.PRINT_SHACL_REPORT) {
            ShLib.printReport(report);
        }

        // Check if the report indicates a non-conforming graph
        if (Config.THROW_ON_SHACL_UNCONFORM && !report.conforms()) {
            throw new IllegalStateException(
                "[SHACL Validation] SHACL validation failed for union graph"
            );
        }
    }
}
