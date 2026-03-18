import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import config.Config;
import extraction.DataExtractor;
import preprocessing.Registry;
import preprocessing.hooks.*;
import rdf.mapping.MappingPairPlanner;
import rdf.mapping.RDFMapper;
import rdf.validation.ShaclValidation;
import reconciliation.WikidataReconciliationService;

public class Main {

    private static final String DISABLE_RECONCILIATION_FLAG = "-dr";
    private static final String DISABLE_EXTRACTION_FLAG = "-de";
    private static final String DISABLE_SHACL_FAILURE = "-ds";

    public static void main(String[] args) throws IOException, InterruptedException {

        // CLI
        for (String arg : args) {
            switch (arg) {
                case DISABLE_RECONCILIATION_FLAG:
                    Config.RECONCILIATION_ENABLED = false;
                    break;
                case DISABLE_EXTRACTION_FLAG:
                    Config.EXTRACTION_ENABLED = false;
                    break;
                case DISABLE_SHACL_FAILURE:
                    Config.THROW_ON_SHACL_UNCONFORM = false;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unknown argument: " + arg + ". Supported flags: "
                                    + DISABLE_EXTRACTION_FLAG + ", "
                                    + DISABLE_RECONCILIATION_FLAG + ", "
                                    + DISABLE_SHACL_FAILURE
                    );
            }
        }

        // Extract the data from the source and store it in the data/ directory
        if (Config.EXTRACTION_ENABLED) {
            DataExtractor extractor = new DataExtractor();
            extractor.extract();
        }

        Registry registry = new Registry();
        registry.register(
                new ParliamentarianReconciliation()
        );
        registry.run();

        // Start the mapping and reconciliation process, which will generate the RDF graph
        try {
            // Generate the tmp mapping files
            MappingPairPlanner planner = new MappingPairPlanner();
            List<Path> mappingFiles = planner.createMappingPairs();

            // Map the data to RDF using the generated mapping files
            RDFMapper mapper = new RDFMapper(mappingFiles);
            mapper.map();

            // SHACL Validation
            ShaclValidation validator = new ShaclValidation();
            validator.validate();
        } finally {
            
            if (Config.RECONCILIATION_ENABLED) {
                WikidataReconciliationService.persistCache();
            }

            // Clean up the tmp directory
            if (Files.exists(Config.TMP_DIR)) {
                try (var paths = Files.walk(Config.TMP_DIR)) {
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
