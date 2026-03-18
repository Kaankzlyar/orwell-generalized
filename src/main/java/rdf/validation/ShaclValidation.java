package rdf.validation;

import java.io.IOException;
import java.nio.file.Files;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

import config.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ShaclValidation {

    public void validate() {
        if (!Files.isRegularFile(Config.OUTPUT_PATH)) {
            throw new IllegalStateException("Data graph not found: " + Config.OUTPUT_PATH);
        }
        if (!Files.isDirectory(Config.SHACL_DIR)) {
            throw new IllegalStateException("SHACL directory not found: " + Config.SHACL_DIR);
        }

        Model data = RDFDataMgr.loadModel(Config.OUTPUT_PATH.toString());
        Model shapes = ModelFactory.createDefaultModel();

        try (var paths = Files.list(Config.SHACL_DIR)) {
            paths
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(Config.OUTPUT_FORMAT.getName()))
                .sorted()
                .forEach(path -> {
                    System.out.println("Reading SHACL shapes from: " + path);
                    RDFDataMgr.read(shapes, path.toString());
                });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SHACL shapes from: " + Config.SHACL_DIR, e);
        }

        ValidationReport report = ShaclValidator.get().validate(shapes.getGraph(), data.getGraph());
        ShLib.printReport(report);

        if (Config.THROW_ON_SHACL_UNCONFORM && !report.conforms()) {
            throw new IllegalStateException("SHACL validation failed");
        }
    }
}
