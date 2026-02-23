import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import rdfmapping.MappingPairPlanner;
import rdfmapping.RDFMapper;

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
        Files.walk(TMP_DIR)
            .map(Path::toFile)
            .forEach(File::delete);

        // SHACL Validation

        // Link the RDF graph to external datasets
    }
}
