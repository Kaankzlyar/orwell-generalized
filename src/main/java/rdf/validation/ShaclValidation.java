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

    public void validate() {
        if (!Files.isDirectory(Config.OUTPUT_DIR)) {
            System.out.println(
                "Output directory not found: " + Config.OUTPUT_DIR
            );
            return;
        }
        if (!Files.isDirectory(Config.SHACL_DIR)) {
            throw new IllegalStateException(
                "SHACL directory not found: " + Config.SHACL_DIR
            );
        }

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

        List<Path> graphPaths;
        try (var paths = Files.list(Config.OUTPUT_DIR)) {
            graphPaths = paths.toList();
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to list output directory: " + Config.OUTPUT_DIR,
                e
            );
        }

        if (graphPaths.isEmpty()) {
            System.out.println("No graph files found for SHACL validation.");
            return;
        }

        Model data = ModelFactory.createDefaultModel();
        graphPaths.forEach(graphPath -> {
            System.out.println(
                "Loading graph for SHACL validation: " + graphPath
            );
            RDFDataMgr.read(data, graphPath.toString());
        });

        System.out.println("Union graph size: " + data.size() + " triples");

        ValidationReport report = ShaclValidator.get().validate(
            shapes.getGraph(),
            data.getGraph()
        );

        System.out.println("SHACL validation completed for union graph");
        ShLib.printReport(report);
        if (Config.THROW_ON_SHACL_UNCONFORM && !report.conforms()) {
            throw new IllegalStateException(
                "SHACL validation failed for union graph"
            );
        }
    }
}
