import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rdf.mapping.MappingPairPlanner;
import rdf.mapping.RDFMapper;

public class Main {

    private static final Path TMP_DIR = Path.of("tmp");

    public static void main(String[] args) throws IOException, InterruptedException {

        // Extract the data from the source and store it in a temporary directory

        // Generate the tmp mapping files
        MappingPairPlanner planner = new MappingPairPlanner(TMP_DIR);
        List<Path> mappingFiles = planner.createMappingPairs();
        
        // Map the data to RDF using the generated mapping files
        RDFMapper mapper = new RDFMapper(mappingFiles);
        mapper.map();

        // Clean up the tmp directory
        if (Files.exists(TMP_DIR)) {
            try (var paths = Files.walk(TMP_DIR)) {
                paths
                    .sorted((left, right) -> right.compareTo(left))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    });
            }
        }

        // SHACL Validation

        // Link the RDF graph to external datasets
    }
}
