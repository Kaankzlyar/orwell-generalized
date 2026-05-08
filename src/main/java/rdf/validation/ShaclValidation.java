package rdf.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

import config.Config;

public class ShaclValidation {

    public void validate() {
        Path outputDir = Config.OUTPUT_PATH.getParent();
        if (!Files.isDirectory(outputDir)) {
            System.out.println("Output directory not found: " + outputDir);
            return;
        }
        if (!Files.isDirectory(Config.SHACL_DIR)) {
            throw new IllegalStateException("SHACL directory not found: " + Config.SHACL_DIR);
        }

        Model shapes = ModelFactory.createDefaultModel();
        try (var paths = Files.list(Config.SHACL_DIR)) {
            paths
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(Config.OUTPUT_FORMAT.getDefaultFileExtension()))
                .sorted()
                .forEach(path -> {
                    System.out.println("Reading SHACL shapes from: " + path);
                    RDFDataMgr.read(shapes, path.toString());
                });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SHACL shapes from: " + Config.SHACL_DIR, e);
        }

        try (Stream<Path> graphFiles = Files.list(outputDir)) {
            graphFiles
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(Config.OUTPUT_FORMAT.getDefaultFileExtension()))
                .sorted()
                .forEach(graphPath -> {
                    System.out.println("Validating graph: " + graphPath);
                    Model data = RDFDataMgr.loadModel(graphPath.toString());
                    System.out.println("Graph size: " + data.size() + " triples");
                    ValidationReport report = ShaclValidator.get().validate(shapes.getGraph(), data.getGraph());
                    System.out.println("SHACL validation completed for: " + graphPath.getFileName());
                    ShLib.printReport(report);
                    if (Config.THROW_ON_SHACL_UNCONFORM && !report.conforms()) {
                        throw new IllegalStateException("SHACL validation failed for: " + graphPath);
                    }
                });
        } catch (IOException e) {
            throw new RuntimeException("Failed to list graph files from: " + outputDir, e);
        }
    }
}
