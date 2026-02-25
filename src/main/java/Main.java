import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import rdf.mapping.MappingPairPlanner;
import rdf.mapping.RDFMapper;
import rdf.validation.ShaclValidation;

public class Main {

    private static final Path TMP_DIR = Path.of("tmp");

    public static void main(String[] args) throws IOException, InterruptedException {

        // Extract the data from the source and store it in a temporary directory

        // Generate the tmp mapping files
        MappingPairPlanner planner = new MappingPairPlanner(TMP_DIR);
        List<Path> mappingFiles = planner.createMappingPairs();
        
        // Map the data to RDF using the generated mapping files
        RDFMapper mapper = new RDFMapper(mappingFiles);
        Path graph = mapper.map();

        // SHACL Validation
        ShaclValidation validator = new ShaclValidation();
        validator.validate(graph);
        
        // Link the RDF graph to external datasets
        
        // Clean up the tmp directory
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
