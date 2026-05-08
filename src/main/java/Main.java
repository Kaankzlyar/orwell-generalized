import static config.Config.*;
import static rdf.validation.ShaclValidation.*;

import cli.Options;
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

    public static void main(String[] args)
        throws IOException, InterruptedException {
        Options.parse(args);

        Benchmark benchmark = new Benchmark();

        // Extract data
        if (Options.extractionEnabled()) {
            benchmark.startTiming("Extraction");
            extract();
            benchmark.endTiming();
        }

        // Preprocess data
        benchmark.startTiming("Preprocessing");
        preprocess();
        benchmark.endTiming();

        try {
            if (Options.mappingEnabled()) {
                // Plan mapping pairs
                benchmark.startTiming("MappingPairPlanner");
                var mappingGroups = planMapping();
                benchmark.endTiming();

                // Map data
                benchmark.startTiming("RDFMapper");
                map(mappingGroups);
                benchmark.endTiming();
            }

            // Load model
            benchmark.startTiming("Load Model");
            Model finalGraph = GraphLoader.loadGraph();
            benchmark.endTiming();

            if (Options.shaclEnabled()) {
                // SHACL validation
                benchmark.startTiming("SHACL Validation");
                validate(finalGraph);
                benchmark.endTiming();
            }
        } finally {
            // Persist reconciliation cache
            if (Options.reconciliationEnabled()) {
                WikidataReconciliationService.persistCache();
            }

            // Delete temporary files
            if (!Options.keepTmp()) {
                deleteTmpDir();
            }
        }

        benchmark.printTimingSummary();
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
            // TODO: This can be parallelized
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
