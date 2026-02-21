import java.io.IOException;
import java.nio.file.Path;

import rdfmapping.MappingPairPlanner;
import rdfmapping.RDFMapper;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        Path outputDir = Path.of("output");
        Path tempMappingsDir = outputDir.resolve("tmp-mappings");

        MappingPairPlanner planner = new MappingPairPlanner(Path.of("data"), Path.of("mappings"), outputDir);

        // TODO: Planner should return the paths to each mapping fie so the RDFMapper does not waste time on that
        planner.createMappingPairs();

        RDFMapper mapper = new RDFMapper(tempMappingsDir, outputDir.resolve("graph.ttl"));
        mapper.map();
    }
}
