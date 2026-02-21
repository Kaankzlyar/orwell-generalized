import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RDFMapper {
    
    private Path mappingPath;
    private Path outputPath;


    public void map() throws IOException, InterruptedException {
        if (mappingPath == null || outputPath == null) {
            throw new IllegalStateException("Mapping path and output path must be set before running the mapper.");
        }

        // Build the process with correct args
        ProcessBuilder rmlmapper = new ProcessBuilder(
            "java", "-jar", "lib/rmlmapper.jar",
            "-m", mappingPath.toString(),
            "-o", outputPath.toString(),
            "-s", "turtle"
        );

        rmlmapper.inheritIO();
        Process p = rmlmapper.start();
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new IOException("RMLMapper finished with non-zero exit code: " + exitCode);
        }
    }
}
