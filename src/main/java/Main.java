import static config.Config.TMP_DIR;
import static rdf.validation.ShaclValidation.validate;

import cli.Options;
import config.Config;
import extraction.ARExtractor;
import extraction.DataExtractor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import preprocessing.Registry;
import preprocessing.hooks.AddLegislatureToVotes;
import preprocessing.hooks.CommissionInformation;
import preprocessing.hooks.ExtractVoting;
import preprocessing.hooks.LegislatureInformation;
import preprocessing.hooks.ParliamentarianIdentification;
import preprocessing.hooks.RemoveEmptyXmlElements;
import rdf.GraphLoader;
import rdf.mapping.MappingPairPlanner;
import rdf.mapping.MappingRunner;
import reconciliation.WikidataReconciliationService;
import utils.Benchmark;
import utils.FileUtils;

public class Main {

    public static void main(String[] args)
        throws IOException, InterruptedException {
        Options.parse(args);

        Benchmark benchmark = new Benchmark();

        Path originalDataDir = Config.DATA_DIR;

        // Extract data
        if (Options.extractionEnabled()) {
            benchmark.startTiming("Extraction");
            extract();
            benchmark.endTiming();
            Config.DATA_DIR = TMP_DIR.resolve(Path.of("data"));
        }

        if (Options.mappingEnabled()){
            // Preprocess data
            benchmark.startTiming("Preprocessing");
            preprocess();
            benchmark.endTiming();

			try {
                // Plan mapping pairs
                benchmark.startTiming("MappingPairPlanner");
                var mappingGroups = planMapping();
                benchmark.endTiming();

                // Map data
                benchmark.startTiming("RDFMapper");
                map(mappingGroups);
                benchmark.endTiming();
            } finally {

                if (Options.reconciliationEnabled())
                    WikidataReconciliationService.persistCache();

                if (!Options.keepTmp())
                    FileUtils.deleteTmpDir();
            }
        }

        // Load model
        benchmark.startTiming("Load Model");
        Model finalGraph = GraphLoader.loadGraph();
        benchmark.endTiming();

        // SHACL validation
        boolean conforms = false;
        if (Options.shaclEnabled()) {
            benchmark.startTiming("SHACL Validation");
            conforms = validate(finalGraph);
            benchmark.endTiming();
        }

        // Move data from tmp to real data directory if SHACL validation passed (or if SHACL validation is disabled)
        if(conforms || !Options.shaclEnabled()) {
            FileUtils.moveTmpDataToData(originalDataDir);
        }

        // Push to Fuseki
        if (Options.fusekiEnabled() && finalGraph != null) {
            GraphLoader.pushToFuseki(finalGraph);
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
            new RemoveEmptyXmlElements(),
            new ParliamentarianIdentification(),
            new CommissionInformation(),
            new LegislatureInformation(),
            new ExtractVoting(),
            new AddLegislatureToVotes()
        );
        Registry.run();
    }

    private static Map<String, List<Path>> planMapping() throws IOException {
        MappingPairPlanner planner = new MappingPairPlanner();
        return planner.createMappingPairs();
    }

    private static void map(Map<String, List<Path>> mappingGroups) {
        MappingRunner.dispatch(mappingGroups, Options.parallelMappingEnabled());
    }

}
