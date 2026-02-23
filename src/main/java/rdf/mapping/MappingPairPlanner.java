package rdf.mapping;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MappingPairPlanner {

    private static final String XML_EXTENSION = ".xml";
    private static final String TTL_EXTENSION = ".ttl";
    private static final String SOURCE_PATTERN = "rml:source\\s+\"[^\"]*\"\\s*;";
    
    private static final Path DATA_DIR = Path.of("data");
    private static final Path MAPPINGS_DIR = Path.of("mappings");
    private final Path TMP_DIR;

    public MappingPairPlanner(Path tmpDir) {
        this.TMP_DIR = tmpDir;
    }

    public List<Path> createMappingPairs() throws IOException {
        validateDirectories();
        Files.createDirectories(TMP_DIR);
        Path tmpMappingsDir = Files.createDirectories(TMP_DIR.resolve(MAPPINGS_DIR));

        List<Path> createdMappings = new ArrayList<>();
        List<Path> mappingFiles = listFilesWithExtension(MAPPINGS_DIR, TTL_EXTENSION);
        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No mapping files found in: " + MAPPINGS_DIR);
        }

        for (Path mappingFile : mappingFiles) {
            createdMappings.addAll(createPairsForMapping(mappingFile, tmpMappingsDir));
        }
        if (createdMappings.isEmpty()) {
            throw new IllegalStateException("No temporary mapping files were created in: " + tmpMappingsDir);
        }
        return createdMappings;
    }

    private void validateDirectories() {
        if (!Files.isDirectory(DATA_DIR)) {
            throw new IllegalStateException("Data directory does not exist: " + DATA_DIR);
        }
        if (!Files.isDirectory(MAPPINGS_DIR)) {
            throw new IllegalStateException("Mappings directory does not exist: " + MAPPINGS_DIR);
        }
    }

    private List<Path> createPairsForMapping(Path mappingFile, Path tmpMappingsDir) throws IOException {
        List<Path> createdMappings = new ArrayList<>();
        String mappingName = stripExtension(mappingFile.getFileName().toString());
        Path mappingDataDir = DATA_DIR.resolve(mappingName);

        if (!Files.isDirectory(mappingDataDir)) {
            System.out.println("Skipping mapping " + mappingName + ": no data directory at " + mappingDataDir);
            return createdMappings;
        }

        List<Path> xmlFiles = listFilesWithExtension(mappingDataDir, XML_EXTENSION);
        if (xmlFiles.isEmpty()) {
            System.out.println("Skipping mapping " + mappingName + ": no XML files in " + mappingDataDir);
            return createdMappings;
        }

        String mappingTemplate = Files.readString(mappingFile);
        Path tempMappingSubDir = tmpMappingsDir.resolve(mappingName);
        Files.createDirectories(tempMappingSubDir);

        for (Path xmlPath : xmlFiles) {
            Path createdMapping = createMappingFile(mappingName, mappingTemplate, xmlPath, tempMappingSubDir);
            createdMappings.add(createdMapping);
        }
        return createdMappings;
    }

    private Path createMappingFile(
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
        return tempMappingPath;
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
