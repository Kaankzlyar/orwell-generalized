package rdf.mapping;

import config.Config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class MappingRunner {

    private MappingRunner() {}

    public static void dispatch(Map<String, List<Path>> mappingGroups, boolean parallel) {
        dispatch(
            mappingGroups,
            parallel,
            (mappingFiles, outputPath) -> new RDFMapper(mappingFiles, outputPath)::map
        );
    }

    static void dispatch(Map<String, List<Path>> mappingGroups, boolean parallel, MapperFactory mapperFactory) {

        // Map sequentially if parallel flag has not been set
        if (!parallel || mappingGroups.size() <= 1) {
            mappingGroups.forEach((key, value) -> mapGroup(key, value, mapperFactory));
            return;
        }

        List<Future<?>> futures = new ArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            mappingGroups.forEach((key, value) ->
                futures.add(executor.submit(() -> {
                    mapGroup(key, value, mapperFactory);
                    return null;
                }))
            );

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("[RDFMapper] Interrupted", e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("[RDFMapper] Mapping failed", e.getCause());
                }
            }
        }
    }

    /**
    * Dispatch the RDFMapper for a mapping group
    * @param legislature the group identifier used to derive the output graph file name
    * @param mappingFiles the mapping definition files to process for this group
    * @param mapperFactory the factory used to create a mapper for the given input files and output path
 */
    private static void mapGroup(String legislature, List<Path> mappingFiles, MapperFactory mapperFactory) {
        Path outputPath = outputPathForGroup(legislature);
        System.out.println("[RDFMapper] Generating graph for legislature: "
            + legislature + " -> " + outputPath);
        try {
            mapperFactory.create(mappingFiles, outputPath).map();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Path outputPathForGroup(String groupName) {
        return Path.of(
            Config.OUTPUT_DIR.toString(),
            "graph-" + groupName + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
    }

    @FunctionalInterface
    interface MapperFactory {
        Mapper create(List<Path> mappingFiles, Path outputPath);
    }

    @FunctionalInterface
    interface Mapper {
        void map() throws Exception;
    }
}
