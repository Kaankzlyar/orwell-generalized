import static core.config.Config.TMP_DIR;
import static core.rdf.validation.ShaclValidation.validate;

import core.cli.Options;
import core.config.Config;
import core.config.ManifestLoader;
import core.config.UseCaseManifest;
import core.extraction.DataExtractor;
import core.extraction.HttpFileSourceAdapter;
import core.preprocessing.Hook;
import core.preprocessing.Registry;
import core.rdf.GraphLoader;
import core.rdf.mapping.MappingPairPlanner;
import core.rdf.mapping.MappingRunner;
import core.reconciliation.WikidataReconciliationService;
import core.utils.Benchmark;
import core.utils.FileUtils;
import usecase.arparliament.hooks.UseCaseHookFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Model;

/**
 * Entry point for the generalized pipeline. The only use-case-specific code
 * left here is the switch in {@link #extract(UseCaseManifest)} — hook
 * resolution is delegated to the active use case's own factory, and
 * everything else is generic and driven entirely by the active use case's
 * {@code dataset.yml}.
 */
public class Main {

    public static void main(String[] args)
        throws IOException, InterruptedException {
        Options.parse(args);

        String useCaseId = System.getenv().getOrDefault("ORWELL_USECASE", "ar-parliament");
        Path useCaseRoot = Path.of("usecases", useCaseId);
        UseCaseManifest manifest = ManifestLoader.load(useCaseRoot);
        ManifestLoader.applyTo(useCaseRoot, manifest);

        Benchmark benchmark = new Benchmark();

        Path originalDataDir = Config.DATA_DIR;

        // Extract data
        if (Options.extractionEnabled()) {
            benchmark.startTiming("Extraction");
            extract(manifest);
            benchmark.endTiming();
            Config.DATA_DIR = TMP_DIR.resolve(Path.of("data"));
        }

        if (Options.mappingEnabled()){
            // Preprocess data
            benchmark.startTiming("Preprocessing");
            preprocess(manifest);
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

    private static void extract(UseCaseManifest manifest) {
        DataExtractor extractor = switch (manifest.source().kind()) {
            case "http-file" -> new HttpFileSourceAdapter(
                Path.of(manifest.source().sourcesFile()),
                manifest.domain()
            );
            default -> throw new IllegalStateException(
                "Unsupported source kind: " + manifest.source().kind()
            );
        };
        extractor.extract();
    }

    private static void preprocess(UseCaseManifest manifest) {
        List<Hook> hooks = UseCaseHookFactory.resolve(manifest.preprocessing());
        Registry.register(hooks.toArray(new Hook[0]));
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
