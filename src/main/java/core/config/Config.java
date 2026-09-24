package core.config;

import java.nio.file.Path;
import java.util.Set;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.util.Collections;
import java.util.List;

public final class Config {

    public static final RDFFormat OUTPUT_FORMAT = RDFFormat.TURTLE;

    public static Path DATA_DIR = Path.of("data");
    public static Path MAPPINGS_DIR = Path.of("mappings");
    public static List<Path> FUNCTIONS_DIRS = List.of(Path.of("functions"));
    public static Path TMP_DIR = Path.of("tmp");
    public static Path GENERATED_MAPPINGS_BASE_DIR = Path.of("tmp", "mappings");
    public static Path SHACL_DIR = Path.of("shacl");
    public static Path OUTPUT_DIR = Path.of("output");

    public static Path OUTPUT_PATH = Path.of(
        OUTPUT_DIR.toString(),
        "graph." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
    );
    public static Path CACHE_PATH = Path.of("reconciliation-cache.properties");
    public static Path LOG_PATH = Path.of("log.txt");

    public static String FUSEKI_URL = envOrDefault(
        "FUSEKI_URL",
        String.format(
            "http://localhost:%s/ds",
            envOrDefault("FUSEKI_PORT", "3030")
        )
    );
    public static Set<String> DISABLED_PARTITIONS = Collections.emptySet();

    private static String envOrDefault(String key, String fallback) {
        String val = System.getenv(key);
        return val != null && !val.isBlank() ? val : fallback;
    }
}
