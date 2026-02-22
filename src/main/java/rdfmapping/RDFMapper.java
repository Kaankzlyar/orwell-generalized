package rdfmapping;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RDFMapper {
    
    private List<Path> mappingFiles;
    private Path outputPath;
    private static final Path MAPPER_ENGINE_PATH = Path.of("lib", "rmlmapper-8.1.0-r380-all.jar");    
    private static final String OUTPUT_FORMAT = "turtle";

    public void map() throws IOException, InterruptedException {
        if (mappingFiles == null || outputPath == null) {
            throw new IllegalStateException("Mapping files and output path must be set before running the mapper.");
        }

        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No mapping files provided to RDFMapper.");
        }

        if (outputPath.getParent() != null) {
            java.nio.file.Files.createDirectories(outputPath.getParent());
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

}
