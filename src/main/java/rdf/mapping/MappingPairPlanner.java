package rdf.mapping;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import config.Config;

@Getter
@Setter
@NoArgsConstructor
public class MappingPairPlanner {

    private static final String XML_EXTENSION = ".xml";
    private static final String TTL_EXTENSION = ".ttl";
    private static final Pattern SOURCE_PATTERN = Pattern.compile("rml:source\\s+\"([^\"]+)\"\\s*;");

    public Map<String, List<Path>> createMappingPairs() throws IOException {
        validateDirectories();
        Files.createDirectories(Config.TMP_DIR);
        Path tmpMappingsDir = Files.createDirectories(Config.TMP_DIR.resolve(Config.MAPPINGS_DIR));

        Map<String, List<Path>> mappingGroups = new LinkedHashMap<>();
        List<Path> extractorDirs = listExtractorDirectories(Config.MAPPINGS_DIR);
        if (extractorDirs.isEmpty()) {
            throw new IllegalStateException("No extractor directories found in: " + Config.MAPPINGS_DIR);
        }

        for (Path extractorDir : extractorDirs) {
            String extractorName = extractorDir.getFileName().toString();
            List<Path> mappingFiles = listFilesWithExtension(extractorDir, TTL_EXTENSION);

            for (Path mappingFile : mappingFiles) {
                for (var entry : createPairsForMapping(mappingFile, tmpMappingsDir, extractorName).entrySet()) {
                    String legislature = entry.getKey();
                    mappingGroups.computeIfAbsent(legislature, k -> new ArrayList<>()).addAll(entry.getValue());
                }
            }
        }

        if (mappingGroups.isEmpty()) {
            throw new IllegalStateException("No temporary mapping files were created in: " + tmpMappingsDir);
        }
        return mappingGroups;
    }

    private void validateDirectories() {
        if (!Files.isDirectory(Config.DATA_DIR)) {
            throw new IllegalStateException("Data directory does not exist: " + Config.DATA_DIR);
        }
        if (!Files.isDirectory(Config.MAPPINGS_DIR)) {
            throw new IllegalStateException("Mappings directory does not exist: " + Config.MAPPINGS_DIR);
        }
    }

    private Map<String, List<Path>> createPairsForMapping(Path mappingFile, Path tmpMappingsDir, String extractorName) throws IOException {
        Map<String, List<Path>> mappingGroups = new LinkedHashMap<>();
        String mappingName = stripExtension(mappingFile.getFileName().toString());
        String mappingTemplate = Files.readString(mappingFile);

        List<String> sourceNames = extractSourceNames(mappingTemplate);
        if (sourceNames.isEmpty()) {
            System.out.println("Skipping mapping " + mappingName + ": no rml:source declarations found");
            return mappingGroups;
        }

        Map<String, Map<String, Path>> filesBySource = new LinkedHashMap<>();
        for (String sourceName : sourceNames) {
            Path dataDir = Config.DATA_DIR.resolve(extractorName).resolve(sourceName);
            if (!Files.isDirectory(dataDir)) {
                System.out.println("Skipping mapping " + mappingName + ": data directory not found at " + dataDir);
                return mappingGroups;
            }
            List<Path> xmlFiles = listFilesWithExtension(dataDir, XML_EXTENSION);
            if (xmlFiles.isEmpty()) {
                System.out.println("Skipping mapping " + mappingName + ": no XML files in " + dataDir);
                return mappingGroups;
            }
            Map<String, Path> filesByName = new LinkedHashMap<>();
            for (Path file : xmlFiles) {
                filesByName.put(stripExtension(file.getFileName().toString()), file);
            }
            filesBySource.put(sourceName, filesByName);
        }

        Set<String> commonNames = new TreeSet<>(filesBySource.get(sourceNames.get(0)).keySet());
        for (int i = 1; i < sourceNames.size(); i++) {
            commonNames.retainAll(filesBySource.get(sourceNames.get(i)).keySet());
        }

        if (commonNames.isEmpty()) {
            System.out.println("Skipping mapping " + mappingName + ": no common XML filenames across source directories");
            return mappingGroups;
        }

        Path tempMappingSubDir = tmpMappingsDir.resolve(extractorName).resolve(mappingName);
        Files.createDirectories(tempMappingSubDir);

        for (String baseName : commonNames) {
            if (Config.DISABLED_LEGISLATURES.contains(baseName)) {
                System.out.println("Skipping disabled legislature: " + baseName);
                continue;
            }

            Map<String, String> replacements = new LinkedHashMap<>();
            for (String sourceName : sourceNames) {
                Path xmlPath = filesBySource.get(sourceName).get(baseName);
                replacements.put(sourceName, xmlPath.toAbsolutePath().normalize().toString().replace("\\", "/"));
            }
            Path createdMapping = createMappingFile(mappingName, mappingTemplate, replacements, tempMappingSubDir, extractorName, baseName);
            mappingGroups.computeIfAbsent(baseName, k -> new ArrayList<>()).add(createdMapping);
            System.out.println("Created mapping file: " + createdMapping);
        }
        return mappingGroups;
    }

    private Path createMappingFile(
        String mappingName,
        String mappingTemplate,
        Map<String, String> sourceReplacements,
        Path tempMappingSubDir,
        String extractorName,
        String baseName
    ) throws IOException {
        String mappingContent = mappingTemplate;
        for (Map.Entry<String, String> entry : sourceReplacements.entrySet()) {
            String literal = "rml:source \"" + entry.getKey() + "\" ;";
            String replacement = "rml:source \"" + entry.getValue() + "\" ;";
            mappingContent = mappingContent.replace(literal, replacement);
        }
        mappingContent = applyUniqueBase(mappingContent, extractorName, mappingName, baseName);

        Path tempMappingPath = tempMappingSubDir.resolve(baseName + TTL_EXTENSION);
        Files.writeString(tempMappingPath, mappingContent);
        return tempMappingPath;
    }

    private String applyUniqueBase(String mappingContent, String extractorName, String mappingName, String baseName) {
        String uniqueBase = "http://example.org/mappings/" + extractorName + "/" + mappingName + "/" + baseName + "/";
        String baseLine = "@base <" + uniqueBase + "> .";
        String withoutBase = mappingContent.replaceAll("(?m)^@base\\s+<[^>]+>\\s*\\.\\s*$\\R?", "");
        return baseLine + System.lineSeparator() + withoutBase;
    }

    private List<String> extractSourceNames(String mappingTemplate) {
        List<String> names = new ArrayList<>();
        Matcher matcher = SOURCE_PATTERN.matcher(mappingTemplate);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names.stream().distinct().collect(Collectors.toList());
    }

    private List<Path> listFilesWithExtension(Path dir, String extension) throws IOException {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(dir)) {
            stream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(extension))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .forEach(files::add);
        }
        return files;
    }

    private List<Path> listExtractorDirectories(Path parentDir) throws IOException {
        List<Path> dirs = new ArrayList<>();
        try (Stream<Path> stream = Files.list(parentDir)) {
            stream
                .filter(Files::isDirectory)
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .forEach(dirs::add);
        }
        return dirs;
    }

    private String stripExtension(String fileName) {
        return fileName.replaceFirst("\\.[^.]+$", "");
    }
}
