package rdfmapping;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MappingPairPlanner {

    private static final String XML_EXTENSION = ".xml";
    private static final String TTL_EXTENSION = ".ttl";
    private static final String SOURCE_PATTERN = "rml:source\\s+\"[^\"]*\"\\s*;";

    private final Path dataRootDir;
    private final Path mappingsDir;
    private final Path outputDir;
    private final Path tempMappingsDir;

    public MappingPairPlanner(Path dataRootDir, Path mappingsDir, Path outputDir) {
        this.dataRootDir = dataRootDir;
        this.mappingsDir = mappingsDir;
        this.outputDir = outputDir;
        this.tempMappingsDir = outputDir.resolve("tmp-mappings");
    }

    public void createMappingPairs() throws IOException {
        validateDirectories();
        Files.createDirectories(outputDir);
        Files.createDirectories(tempMappingsDir);

        List<Path> mappingFiles = listFilesWithExtension(mappingsDir, TTL_EXTENSION);
        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No mapping files found in: " + mappingsDir);
        }

        for (Path mappingFile : mappingFiles) {
            createPairsForMapping(mappingFile);
        }
    }

    private void validateDirectories() {
        if (!Files.isDirectory(dataRootDir)) {
            throw new IllegalStateException("Data directory does not exist: " + dataRootDir);
        }
        if (!Files.isDirectory(mappingsDir)) {
            throw new IllegalStateException("Mappings directory does not exist: " + mappingsDir);
        }
    }

    private void createPairsForMapping(Path mappingFile) throws IOException {
        String mappingName = stripExtension(mappingFile.getFileName().toString());
        Path mappingDataDir = dataRootDir.resolve(mappingName);
        if (!Files.isDirectory(mappingDataDir)) {
            System.out.println("Skipping mapping " + mappingName + ": no data directory at " + mappingDataDir);
            return;
        }

        List<Path> xmlFiles = listFilesWithExtension(mappingDataDir, XML_EXTENSION);
        if (xmlFiles.isEmpty()) {
            System.out.println("Skipping mapping " + mappingName + ": no XML files in " + mappingDataDir);
            return;
        }

        String mappingTemplate = Files.readString(mappingFile);
        Path tempMappingSubDir = tempMappingsDir.resolve(mappingName);
        Files.createDirectories(tempMappingSubDir);

        for (Path xmlPath : xmlFiles) {
            createMappingFile(mappingName, mappingTemplate, xmlPath, tempMappingSubDir);
        }
    }

    private void createMappingFile(
        String mappingName,
        String mappingTemplate,
        Path xmlPath,
        Path tempMappingSubDir
    ) throws IOException {
        String xmlFileName = xmlPath.getFileName().toString();
        String baseName = stripExtension(xmlFileName);

        String xmlSource = xmlPath.toString().replace("\\", "/");
        String mappingContent = mappingTemplate.replaceFirst(
            SOURCE_PATTERN,
            "rml:source \"" + xmlSource + "\" ;"
        );
        mappingContent = applyUniqueBase(mappingContent, mappingName, baseName);

        Path tempMappingPath = tempMappingSubDir.resolve(baseName + TTL_EXTENSION);
        Files.writeString(tempMappingPath, mappingContent);
    }

    // This is needed due to passing multiple mapping files to RMLMapper.
    private String applyUniqueBase(String mappingContent, String mappingName, String baseName) {
        String uniqueBase = "http://example.org/mappings/" + mappingName + "/" + baseName + "/";
        String baseLine = "@base <" + uniqueBase + "> .";
        String withoutBase = mappingContent.replaceAll("(?m)^@base\\s+<[^>]+>\\s*\\.\\s*$\\R?", "");
        return baseLine + System.lineSeparator() + withoutBase;
    }

    private List<Path> listFilesWithExtension(Path dir, String extension) throws IOException {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(extension))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .forEach(files::add);
        }
        return files;
    }

    private String stripExtension(String fileName) {
        return fileName.replaceFirst("\\.[^.]+$", "");
    }
}
