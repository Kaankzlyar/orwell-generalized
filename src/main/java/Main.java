import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import rdf.mapping.MappingPairPlanner;
import rdf.mapping.RDFMapper;
import rdf.validation.ShaclValidation;

public class Main {

    private static final Path TMP_DIR = Path.of("tmp");
    private static final String DISABLE_RECONCILIATION_FLAG = "--disable-reconciliation";

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean reconciliationEnabled = true;
        for (String arg : args) {
            if (DISABLE_RECONCILIATION_FLAG.equals(arg)) {
                reconciliationEnabled = false;
            } else {
                throw new IllegalArgumentException(
                        "Unknown argument: " + arg + ". Supported flags: " + DISABLE_RECONCILIATION_FLAG
                );
            }
        }

        // Extract the data from the source and store it in a temporary directory
        // Add flag to decide if downloaded data should be deleted or not

        // Generate the tmp mapping files
        MappingPairPlanner planner = new MappingPairPlanner(TMP_DIR);
        planner.setReconciliationEnabled(reconciliationEnabled);
        List<Path> mappingFiles = planner.createMappingPairs();
        
        // Map the data to RDF using the generated mapping files
        RDFMapper mapper = new RDFMapper(mappingFiles, reconciliationEnabled);
        Path graph = mapper.map();

        // SHACL Validation
        ShaclValidation validator = new ShaclValidation();
        validator.validate(graph);
        
        // Clean up the tmp directory
        if (Files.exists(TMP_DIR)) {
            try (var paths = Files.walk(TMP_DIR)) {
                paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            }
        }
    }
}
