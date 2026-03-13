import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import extraction.DataExtractor;
import rdf.mapping.MappingPairPlanner;
import rdf.mapping.RDFMapper;
import rdf.validation.ShaclValidation;
import reconciliation.WikidataReconciliationService;

public class Main {

    private static final Path TMP_DIR = Path.of("tmp");
    private static final Path DATA_DIR = Path.of("data");
    private static final String DISABLE_RECONCILIATION_FLAG = "-dr";
    private static final String DISABLE_EXTRACTION_FLAG = "-de";
    private static final String DISABLE_SHACL_FAILURE = "-ds";

    public static void main(String[] args) throws IOException, InterruptedException {

        // CLI
        boolean reconciliationEnabled = true;
        boolean extractionEnabled = true;
        boolean shaclFail = true;
        for (String arg : args) {
            switch (arg) {
                case DISABLE_RECONCILIATION_FLAG:
                    reconciliationEnabled = false;
                    break;
                case DISABLE_EXTRACTION_FLAG:
                    extractionEnabled = false;
                    break;
                case DISABLE_SHACL_FAILURE:
                    shaclFail = false;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unknown argument: " + arg + ". Supported flags: " + DISABLE_RECONCILIATION_FLAG
                    );
            }
        }

        // Extract the data from the source and store it in the data/ directory
        if (extractionEnabled) {
            DataExtractor extractor = new DataExtractor(DATA_DIR);
            extractor.extract();
        }

        // Start the mapping and reconciliation process, which will generate the RDF graph
        try {
            // Generate the tmp mapping files
            MappingPairPlanner planner = new MappingPairPlanner(TMP_DIR, DATA_DIR);
            planner.setReconciliationEnabled(reconciliationEnabled);
            List<Path> mappingFiles = planner.createMappingPairs();

            // Map the data to RDF using the generated mapping files
            RDFMapper mapper = new RDFMapper(mappingFiles, reconciliationEnabled);
            Path graph = mapper.map();

            // SHACL Validation
            ShaclValidation validator = new ShaclValidation(shaclFail);
            validator.validate(graph);
        } finally {
            if (reconciliationEnabled) {
                WikidataReconciliationService.persistCache();
            }

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
}
