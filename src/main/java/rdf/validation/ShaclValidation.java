package rdf.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShaclValidation {

    private boolean fail = true;

    private static final Path SHACL_DIR = Path.of("shacl");
    private static final String SHACL_FILE_EXTENSION = ".ttl";

    public void validate(Path dataTtl) {
        if (!Files.isRegularFile(dataTtl)) {
            throw new IllegalStateException("Data graph not found: " + dataTtl);
        }
        if (!Files.isDirectory(SHACL_DIR)) {
            throw new IllegalStateException("SHACL directory not found: " + SHACL_DIR);
        }

        Model data = RDFDataMgr.loadModel(dataTtl.toString());
        Model shapes = ModelFactory.createDefaultModel();

        try (var paths = Files.list(SHACL_DIR)) {
            paths
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(SHACL_FILE_EXTENSION))
                .sorted()
                .forEach(path -> {
                    System.out.println("Reading SHACL shapes from: " + path);
                    RDFDataMgr.read(shapes, path.toString());
                });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SHACL shapes from: " + SHACL_DIR, e);
        }

        ValidationReport report = ShaclValidator.get().validate(shapes.getGraph(), data.getGraph());
        ShLib.printReport(report);

        if (fail && !report.conforms()) {
            throw new IllegalStateException("SHACL validation failed");
        }
    }
}
