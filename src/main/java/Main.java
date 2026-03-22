import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static config.Config.*;
import extraction.ARExtractor;
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
    private static final String DISABLE_MAPPING_FLAG = "-dm";
    private static final String DISABLE_SHACL_FAILURE = "-ds";

    public static void main(String[] args) throws IOException, InterruptedException {

        // CLI
        processArgs(args);

        // Extract the data from the sources and store it in the data/ directory
        if (EXTRACTION_ENABLED) {
            List<DataExtractor> extractors = List.of(
                    new ARExtractor()
            );

            for (DataExtractor extractor : extractors) {
                extractor.extract();
            }
        }

        Registry registry = new Registry();
        registry.register(
                new ParliamentarianReconciliation()
        );
        registry.run();

        try {
            // RDF Mapping
            if(MAPPING_ENABLED){
                MappingPairPlanner planner = new MappingPairPlanner();
                List<Path> mappingFiles = planner.createMappingPairs();
    
                RDFMapper mapper = new RDFMapper(mappingFiles);
                mapper.map();
            }

            // SHACL Validation
            if (SHACL_ENABLED){
                ShaclValidation validator = new ShaclValidation();
                validator.validate();
            }
        } finally {
            
            if (RECONCILIATION_ENABLED) {
                WikidataReconciliationService.persistCache();
            }

            if(DELETE_TMP){
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

    private static void processArgs(String[] args){
        for (String arg : args) {
            switch (arg) {
                case DISABLE_RECONCILIATION_FLAG:
                    RECONCILIATION_ENABLED = false;
                    break;
                case DISABLE_EXTRACTION_FLAG:
                    EXTRACTION_ENABLED = false;
                    break;
                case DISABLE_SHACL_FAILURE:
                    THROW_ON_SHACL_UNCONFORM = false;
                    break;
                case DISABLE_MAPPING_FLAG:
                    MAPPING_ENABLED = false;
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
    }
}
