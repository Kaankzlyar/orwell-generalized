package rdfmapping;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RDFMapper {
    
    private Path mappingsDir;
    private Path outputPath;
    private static final Path MAPPER_ENGINE_PATH = Path.of("lib", "rmlmapper-8.1.0-r380-all.jar");    
    private static final String OUTPUT_FORMAT = "turtle";

    public void map() throws IOException, InterruptedException {
        if (mappingsDir == null || outputPath == null) {
            throw new IllegalStateException("Mappings directory and output path must be set before running the mapper.");
        }

        if (!Files.isDirectory(mappingsDir)) {
            throw new IllegalStateException("Mappings directory does not exist: " + mappingsDir);
        }

        List<Path> mappingFiles = listMappingFiles(mappingsDir);
        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No mapping files found in: " + mappingsDir);
        }

        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-jar"); command.add(MAPPER_ENGINE_PATH.toString());
        for (Path mappingFile : mappingFiles) {
            command.add("-m");
            command.add(mappingFile.toString());
        }
        command.add("-o");   command.add(outputPath.toString());
        command.add("-s");   command.add(OUTPUT_FORMAT);

        ProcessBuilder rmlmapper = new ProcessBuilder(command);
        rmlmapper.inheritIO();
        Process p = rmlmapper.start();
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new IOException("RMLMapper finished with non-zero exit code: " + exitCode);
        }
    }

    private List<Path> listMappingFiles(Path dir) throws IOException {
        List<Path> mappingFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(dir)) {
            paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".ttl"))
                .sorted(Comparator.comparing(Path::toString))
                .forEach(mappingFiles::add);
        }
        return mappingFiles;
    }
}
