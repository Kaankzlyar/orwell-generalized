package rdf.mapping;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RDFMapper {
    
    private List<Path> mappingFiles;
    private static final Path OUTPUT_PATH = Path.of("output", "graph.ttl");
    private static final Path MAPPER_ENGINE_PATH = Path.of("lib", "rmlmapper-8.1.0-r380-all.jar");    
    private static final String OUTPUT_FORMAT = "turtle";

    public Path map() throws IOException, InterruptedException {
        if (mappingFiles == null || OUTPUT_FORMAT == null) {
            throw new IllegalStateException("Mapping files and output path must be set before running the mapper.");
        }

        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No mapping files provided to RDFMapper.");
        }

        if (OUTPUT_PATH.getParent() != null) {
            Files.createDirectories(OUTPUT_PATH.getParent());
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar"); command.add(MAPPER_ENGINE_PATH.toString());
        for (Path mappingFile : mappingFiles) {
            command.add("-m");
            command.add(mappingFile.toString());
        }
        command.add("-o");   command.add(OUTPUT_PATH.toString());
        command.add("-s");   command.add(OUTPUT_FORMAT);

        ProcessBuilder rmlmapper = new ProcessBuilder(command);
        rmlmapper.inheritIO();
        Process p = rmlmapper.start();
        int exitCode = p.waitFor();

        if (exitCode != 0) {
            throw new IOException("RMLMapper finished with non-zero exit code: " + exitCode);
        }

        System.out.println("RMLMapper finished successfully. Output graph: " + OUTPUT_PATH);

        return OUTPUT_PATH;
    }

}
