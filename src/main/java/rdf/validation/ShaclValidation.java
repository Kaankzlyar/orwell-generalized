package rdf.validation;

import config.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

public class ShaclValidation {

    public void validate(List<Path> graphPaths) {
        if (graphPaths.isEmpty()) {
            System.out.println("No graph files selected for SHACL validation.");
            return;
        }
        if (!Files.isDirectory(Config.SHACL_DIR)) {
            throw new IllegalStateException(
                "SHACL directory not found: " + Config.SHACL_DIR
            );
        }

        // Load the SHACL shapes from the SHACL directory
        Model shapes = ModelFactory.createDefaultModel();
        try (var paths = Files.list(Config.SHACL_DIR)) {
            paths.forEach(path -> {
                System.out.println("Reading SHACL shapes from: " + path);
                RDFDataMgr.read(shapes, path.toString());
            });
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to read SHACL shapes from: " + Config.SHACL_DIR,
                e
            );
        }

        // Validate each graph against the loaded SHACL shapes
        graphPaths.forEach(graphPath -> {
            System.out.println("Validating graph: " + graphPath);

            Model data = RDFDataMgr.loadModel(graphPath.toString());

            System.out.println("Graph size: " + data.size() + " triples");

            ValidationReport report = ShaclValidator.get().validate(
                shapes.getGraph(),
                data.getGraph()
            );

            System.out.println(
                "SHACL validation completed for: " + graphPath.getFileName()
            );

            ShLib.printReport(report);
            if (Config.THROW_ON_SHACL_UNCONFORM && !report.conforms()) {
                throw new IllegalStateException(
                    "SHACL validation failed for: " + graphPath
                );
            }
        });
    }
}
