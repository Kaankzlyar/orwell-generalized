import static config.Config.OUTPUT_DIR;
import static config.Config.OUTPUT_FORMAT;
import static config.Config.QUERY_DIR;
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
import org.apache.jena.http.sys.RegistryRequestModifier;
import org.apache.jena.rdf.model.Model;
import preprocessing.Registry;
import preprocessing.hooks.AddLegislatureToVotes;
import preprocessing.hooks.CommissionInformation;
import preprocessing.hooks.ExtractVoting;
import preprocessing.hooks.LegislatureInformation;
import preprocessing.hooks.ParliamentarianIdentification;
import query.QueryRunner;
import rdf.GraphLoader;
import rdf.mapping.MappingPairPlanner;
import rdf.mapping.RDFMapper;
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

        // Querying
        if (Options.queriesEnabled() && finalGraph != null) {
            // Configure HTTP for SPARQL SERVICE calls (User-Agent required by Wikidata)
            configureServiceHttp();

            benchmark.startTiming("SPARQL Queries");
            QueryRunner.execute(
                finalGraph,
                Path.of(QUERY_DIR.toString(), "q6.rq")
            );
            benchmark.endTiming();
        }

        // Push to Fuseki
        if (Options.fusekiEnabled() && finalGraph != null) {
            GraphLoader.pushToFuseki(finalGraph);
        }

        benchmark.printTimingSummary();
    } 

    private static void configureServiceHttp() {
        RegistryRequestModifier.get().addPrefix(
            "https://query.wikidata.org/",
            (params, headers) ->
                headers.put(
                    "User-Agent",
                    "Orwell/1.0 (https://github.com/politrackpt/orwell)"
                )
        );
    }

    private static void extract() {
        List<DataExtractor> extractors = List.of(new ARExtractor());

        for (DataExtractor extractor : extractors) {
            extractor.extract();
        }
    }

    private static void preprocess() {
        Registry.register(
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
                "[RDFMapper] Generating graph for legislature: " +
                    legislature +
                    " -> " +
                    outputPath
            );
            // TODO: This can be parallelized
            RDFMapper mapper = new RDFMapper(mappingFiles, outputPath);
            mapper.map();
        }
    }

}
