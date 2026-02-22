import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import rdfmapping.MappingPairPlanner;
import rdfmapping.RDFMapper;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        Path outputDir = Path.of("output");
        MappingPairPlanner planner = new MappingPairPlanner(Path.of("data"), Path.of("mappings"), outputDir);

        List<Path> mappingFiles = planner.createMappingPairs();

        RDFMapper mapper = new RDFMapper(mappingFiles, outputDir.resolve("graph.ttl"));
        mapper.map();
    }
}
