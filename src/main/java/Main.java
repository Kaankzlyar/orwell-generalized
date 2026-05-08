import static config.Config.*;

import extraction.ARExtractor;
import extraction.DataExtractor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import preprocessing.Registry;
import preprocessing.hooks.*;
import rdf.mapping.MappingPairPlanner;
import rdf.mapping.RDFMapper;
import rdf.validation.ShaclValidation;
import reconciliation.WikidataReconciliationService;
import utils.Benchmark;

public class Main {

    private static final String DISABLE_RECONCILIATION_FLAG = "-dr";
    private static final String DISABLE_EXTRACTION_FLAG = "-de";
    private static final String DISABLE_MAPPING_FLAG = "-dm";
    private static final String DISABLE_SHACL_FAILURE = "-ds";
    private static final String ENABLE_LOG_FLAG = "-l";

    public static void main(String[] args)
        throws IOException, InterruptedException {
        processArgs(args);

        Benchmark benchmark = new Benchmark();
        List<Path> generatedGraphPaths = new ArrayList<>();

        if (EXTRACTION_ENABLED) {
            benchmark.startTiming("Extraction");
            List<DataExtractor> extractors = List.of(new ARExtractor());

            for (DataExtractor extractor : extractors) {
                extractor.extract();
            }
            benchmark.endTiming();
        }

        benchmark.startTiming("Hooks");
        Registry.register(
            new ParliamentarianReconciliation(),
            new CommissionInformation(),
            new ExtractVoting()
        );
        Registry.run();
        benchmark.endTiming();
        try {
            if (MAPPING_ENABLED) {
                benchmark.startTiming("MappingPairPlanner");
                MappingPairPlanner planner = new MappingPairPlanner();
                Map<String, List<Path>> mappingGroups = planner.createMappingPairs();
                benchmark.endTiming();

                benchmark.startTiming("RDFMapper");
                for (Map.Entry<String, List<Path>> entry : mappingGroups.entrySet()) {
                    String legislature = entry.getKey();
                    List<Path> mappingFiles = entry.getValue();
                    Path outputPath = legislatureOutputPath(legislature);
                    System.out.println("Generating graph for legislature: " + legislature + " -> " + outputPath);
                    RDFMapper mapper = new RDFMapper(mappingFiles, outputPath);
                    mapper.map();
                    generatedGraphPaths.add(outputPath);
                }
                benchmark.endTiming();
            }

            if (SHACL_ENABLED) {
                benchmark.startTiming("SHACL Validation");
                new ShaclValidation().validate(generatedGraphPaths);
                benchmark.endTiming();
            }
        } finally {
            if (RECONCILIATION_ENABLED) {
                WikidataReconciliationService.persistCache();
            }

            if (DELETE_TMP) {
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
                case ENABLE_LOG_FLAG:
                    LOG_ENABLED = true;
                    System.out.println("Logging enabled.");
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
                            ENABLE_LOG_FLAG
                    );
            }
        }
    }

    public static Path legislatureOutputPath(String legislature) {
        return Path.of(
            "output",
            "graph-" +
                legislature +
                "." +
                OUTPUT_FORMAT.getDefaultFileExtension()
        );
    }
}
