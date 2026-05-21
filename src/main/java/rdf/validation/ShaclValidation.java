package rdf.validation;

import static cli.Options.printShaclReport;
import static cli.Options.throwOnShaclUnconform;

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

    public static boolean validate(Model graph) {
        if (!Files.isDirectory(Config.SHACL_DIR)) {
            throw new IllegalStateException(
                "[SHACL Validation] SHACL directory not found: " +
                    Config.SHACL_DIR
            );
        }

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

        ValidationReport report = ShaclValidator.get().validate(
            shapes.getGraph(),
            graph.getGraph()
        );

        System.out.println("[SHACL Validation] SHACL validation completed");

        if (printShaclReport()) {
            ShLib.printReport(report);
        }

        // If the option to throw on SHACL unconformity is enabled and the report indicates non-conformity, throw an exception
        if (throwOnShaclUnconform() && !report.conforms()) {
            throw new IllegalStateException(
                "[SHACL Validation] SHACL validation failed for union graph"
            );
        }

        // Otherwise, return whether the data conforms to the SHACL shapes, to be used for conditionals of subsequent steps (e.g. moving data from tmp to real data directory)
        return report.conforms();
    }
}
