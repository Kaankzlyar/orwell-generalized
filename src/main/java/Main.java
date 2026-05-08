import static config.Config.*;
import static rdf.validation.ShaclValidation.*;

import extraction.ARExtractor;
import extraction.DataExtractor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import preprocessing.Registry;
import preprocessing.hooks.*;
import rdf.GraphLoader;
import rdf.mapping.MappingPairPlanner;
import rdf.mapping.RDFMapper;
import reconciliation.WikidataReconciliationService;
import utils.Benchmark;

public class Main {

    private static final String DISABLE_RECONCILIATION_FLAG = "-dr";
    private static final String DISABLE_EXTRACTION_FLAG = "-de";
    private static final String DISABLE_MAPPING_FLAG = "-dm";
    private static final String DISABLE_SHACL_FAILURE = "-df";
    private static final String DISABLE_SHACL_FLAG = "-ds";
    private static final String DISABLE_SHACL_REPORT_FLAG = "-r";
    private static final String ENABLE_LOG = "-l";

    public static void main(String[] args)
        throws IOException, InterruptedException {
        processArgs(args);

        Benchmark benchmark = new Benchmark();

        // Extraction
        if (EXTRACTION_ENABLED) {
            benchmark.startTiming("Extraction");
            extract();
            benchmark.endTiming();
        }

        // Preprocessing
        benchmark.startTiming("Preprocessing");
        preprocess();
        benchmark.endTiming();

        try {
            // Mapping
            if (MAPPING_ENABLED) {
                benchmark.startTiming("MappingPairPlanner");
                var mappingGroups = planMapping();
                benchmark.endTiming();

                benchmark.startTiming("RDFMapper");
                map(mappingGroups);
                benchmark.endTiming();
            }

            // Load Model
            benchmark.startTiming("Load Model");
            Model finalGraph = GraphLoader.loadGraph();
            benchmark.endTiming();

            // SHACL Validation
            if (SHACL_ENABLED) {
                benchmark.startTiming("SHACL Validation");
                validate(finalGraph);
                benchmark.endTiming();
            }
        } finally {
            if (RECONCILIATION_ENABLED) {
                WikidataReconciliationService.persistCache();
            }

            if (DELETE_TMP) {
                deleteTmpDir();
            }
        }

        benchmark.printTimingSummary();
    }

    private static void processArgs(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case DISABLE_RECONCILIATION_FLAG:
                    RECONCILIATION_ENABLED = false;
                    System.out.println("Reconciliation disabled.");
                    break;
                case DISABLE_EXTRACTION_FLAG:
                    EXTRACTION_ENABLED = false;
                    System.out.println("Extraction disabled.");
                    break;
                case DISABLE_SHACL_FAILURE:
                    THROW_ON_SHACL_UNCONFORM = false;
                    System.out.println("SHACL throwing on unconform disabled.");
                    break;
                case DISABLE_MAPPING_FLAG:
                    MAPPING_ENABLED = false;
                    System.out.println("Mapping disabled.");
                    break;
                case DISABLE_SHACL_REPORT_FLAG:
                    PRINT_SHACL_REPORT = false;
                    System.out.println("Print SHACL report disabled.");
                    break;
                case DISABLE_SHACL_FLAG:
                    SHACL_ENABLED = false;
                    System.out.println("SHACL disabled.");
                    break;
                case ENABLE_LOG:
                    LOG_ENABLED = true;
                    System.out.println("Reconciliation logging enabled.");
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unknown argument: " +
                            arg +
                            ". Supported flags: " +
                            DISABLE_EXTRACTION_FLAG +
                            ", " +
                            DISABLE_RECONCILIATION_FLAG +
                            ", " +
                            DISABLE_SHACL_FAILURE +
                            ", " +
                            DISABLE_MAPPING_FLAG +
                            ", " +
                            DISABLE_SHACL_REPORT_FLAG +
                            ", " +
                            DISABLE_SHACL_FLAG +
                            ", " +
                            ENABLE_LOG
                    );
            }
        }
    }

    private static void extract() {
        List<DataExtractor> extractors = List.of(new ARExtractor());

        for (DataExtractor extractor : extractors) {
            extractor.extract();
        }
    }

    private static void preprocess() {
        Registry.register(
            new ParliamentarianReconciliation(),
            new CommissionInformation(),
            new ExtractVoting()
        );
        Registry.run();
    }

    private static Map<String, List<Path>> planMapping() throws IOException {
        MappingPairPlanner planner = new MappingPairPlanner();
        return planner.createMappingPairs();
    }

    private static void map(Map<String, List<Path>> mappingGroups)
        throws IOException, InterruptedException {
        for (Map.Entry<String, List<Path>> entry : mappingGroups.entrySet()) {
            String legislature = entry.getKey();
            List<Path> mappingFiles = entry.getValue();
            Path outputPath = Path.of(
                OUTPUT_DIR.toString(),
                "graph-" +
                    legislature +
                    "." +
                    OUTPUT_FORMAT.getDefaultFileExtension()
            );
            System.out.println(
                "Generating graph for legislature: " +
                    legislature +
                    " -> " +
                    outputPath
            );
            RDFMapper mapper = new RDFMapper(mappingFiles, outputPath);
            mapper.map();
        }
    }

    private static void deleteTmpDir() throws IOException {
        if (Files.exists(TMP_DIR)) {
            try (var paths = Files.walk(TMP_DIR)) {
                paths
                    .sorted(Comparator.reverseOrder())
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
