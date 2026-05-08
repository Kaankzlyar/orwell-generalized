package rdf.mapping;

import config.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MappingPairPlanner {

    private static final String XML_EXTENSION = ".xml";
    private static final Pattern SOURCE_PATTERN = Pattern.compile(
        "rml:source\\s+\"([^\"]+)\"\\s*;"
    );

    public Map<String, List<Path>> createMappingPairs() throws IOException {
        validateDirectories();
        Files.createDirectories(Config.TMP_DIR);
        Path tmpMappingsDir = Files.createDirectories(
            Config.TMP_DIR.resolve(Config.MAPPINGS_DIR.getFileName())
        );
        logDisabledLegislatures();

        Map<String, List<Path>> mappingGroups = new LinkedHashMap<>();
        List<Path> domainDirs = listDomainDirectories();
        if (domainDirs.isEmpty()) {
            throw new IllegalStateException(
                "No extractor directories found in: " + Config.MAPPINGS_DIR
            );
        }

        // A domain is like 'AR' or 'BASE'
        for (Path domainDir : domainDirs) {
            String domainName = domainDir.getFileName().toString();
            List<Path> mappingFiles = listFilesWithExtension(
                domainDir,
                "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
            );
            for (Path mappingFile : mappingFiles) {
                addMappingPairs(
                    mappingGroups,
                    createPairsForMapping(
                        mappingFile,
                        tmpMappingsDir,
                        domainDir,
                        domainName
                    )
                );
            }
        }

        if (mappingGroups.isEmpty()) {
            throw new IllegalStateException(
                "No temporary mapping files were created in: " + tmpMappingsDir
            );
        }
        return mappingGroups;
    }

    private void validateDirectories() {
        if (!Files.isDirectory(Config.DATA_DIR)) {
            throw new IllegalStateException(
                "Data directory does not exist: " + Config.DATA_DIR
            );
        }
        if (!Files.isDirectory(Config.MAPPINGS_DIR)) {
            throw new IllegalStateException(
                "Mappings directory does not exist: " + Config.MAPPINGS_DIR
            );
        }
    }

    private void logDisabledLegislatures() {
        if (!Config.DISABLED_LEGISLATURES.isEmpty()) {
            System.out.println(
                "Disabled legislatures: " +
                    String.join(", ", Config.DISABLED_LEGISLATURES)
            );
        }
    }

    private void addMappingPairs(
        Map<String, List<Path>> mappingGroups,
        Map<String, List<Path>> mappingPairs
    ) {
        for (var entry : mappingPairs.entrySet()) {
            mappingGroups
                .computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                .addAll(entry.getValue());
        }
    }

    private Map<String, List<Path>> createPairsForMapping(
        Path mappingFile,
        Path tmpMappingsDir,
        Path domainDir,
        String domainName
    ) throws IOException {
        Map<String, List<Path>> mappingGroups = new LinkedHashMap<>();
        String mappingId = mappingId(domainDir, mappingFile);
        String mappingTemplate = Files.readString(mappingFile);

        List<String> sourceNames = extractSourceNames(mappingTemplate);
        if (sourceNames.isEmpty()) {
            System.out.println(
                "Skipping mapping " +
                    mappingId +
                    ": no rml:source declarations found"
            );
            return mappingGroups;
        }

        Map<String, Map<String, Path>> filesBySource = filesBySourceName(
            sourceNames,
            domainName,
            mappingId
        );
        if (filesBySource.isEmpty()) {
            return mappingGroups;
        }

        List<String> commonNames = commonXmlBaseNames(
            sourceNames,
            filesBySource
        );

        if (commonNames.isEmpty()) {
            System.out.println(
                "Skipping mapping " +
                    mappingId +
                    ": no common XML filenames across source directories"
            );
            return mappingGroups;
        }

        Path tempMappingSubDir = tmpMappingsDir
            .resolve(domainName)
            .resolve(mappingId);
        Files.createDirectories(tempMappingSubDir);

        for (String baseName : commonNames) {
            if (Config.DISABLED_LEGISLATURES.contains(baseName)) {
                continue;
            }

            Map<String, String> replacements = sourceReplacements(
                sourceNames,
                filesBySource,
                baseName
            );
            Path createdMapping = createMappingFile(
                mappingId,
                mappingTemplate,
                replacements,
                tempMappingSubDir,
                domainName,
                baseName
            );
            mappingGroups
                .computeIfAbsent(baseName, k -> new ArrayList<>())
                .add(createdMapping);
            System.out.println(
                "[Mapping Pair Planner] Created mapping file: " + createdMapping
            );
        }
        return mappingGroups;
    }

    private Map<String, Map<String, Path>> filesBySourceName(
        List<String> sourceNames,
        String domainName,
        String mappingId
    ) throws IOException {
        Map<String, Map<String, Path>> filesBySource = new LinkedHashMap<>();
        for (String sourceName : sourceNames) {
            Path dataDir = Config.DATA_DIR.resolve(domainName).resolve(
                sourceName
            );
            if (!Files.isDirectory(dataDir)) {
                System.out.println(
                    "Skipping mapping " +
                        mappingId +
                        ": data directory not found at " +
                        dataDir
                );
                return Map.of();
            }

            Map<String, Path> filesByName = filesByBaseName(dataDir);
            if (filesByName.isEmpty()) {
                System.out.println(
                    "Skipping mapping " +
                        mappingId +
                        ": no XML files in " +
                        dataDir
                );
                return Map.of();
            }
            filesBySource.put(sourceName, filesByName);
        }
        return filesBySource;
    }

    private Map<String, Path> filesByBaseName(Path dataDir) throws IOException {
        Map<String, Path> filesByName = new LinkedHashMap<>();
        for (Path file : listFilesWithExtension(dataDir, XML_EXTENSION)) {
            filesByName.put(
                stripExtension(file.getFileName().toString()),
                file
            );
        }
        return filesByName;
    }

    private List<String> commonXmlBaseNames(
        List<String> sourceNames,
        Map<String, Map<String, Path>> filesBySource
    ) {
        List<String> commonNames = new ArrayList<>();
        Set<String> commonNameSet = new HashSet<>(
            filesBySource.get(sourceNames.get(0)).keySet()
        );

        for (int i = 1; i < sourceNames.size(); i++) {
            commonNameSet.retainAll(
                filesBySource.get(sourceNames.get(i)).keySet()
            );
        }

        for (String baseName : filesBySource.get(sourceNames.get(0)).keySet()) {
            if (commonNameSet.contains(baseName)) {
                commonNames.add(baseName);
            }
        }
        return commonNames;
    }

    private Map<String, String> sourceReplacements(
        List<String> sourceNames,
        Map<String, Map<String, Path>> filesBySource,
        String baseName
    ) {
        Map<String, String> replacements = new LinkedHashMap<>();
        for (String sourceName : sourceNames) {
            Path xmlPath = filesBySource.get(sourceName).get(baseName);
            replacements.put(sourceName, normalizePath(xmlPath));
        }
        return replacements;
    }

    private Path createMappingFile(
        String mappingId,
        String mappingTemplate,
        Map<String, String> sourceReplacements,
        Path tempMappingSubDir,
        String domainName,
        String baseName
    ) throws IOException {
        String mappingContent = mappingTemplate;
        for (Map.Entry<String, String> entry : sourceReplacements.entrySet()) {
            String literal = "rml:source \"" + entry.getKey() + "\" ;";
            String replacement = "rml:source \"" + entry.getValue() + "\" ;";
            mappingContent = mappingContent.replace(literal, replacement);
        }
        mappingContent = applyUniqueBase(
            mappingContent,
            domainName,
            mappingId,
            baseName
        );

        Path tempMappingPath = tempMappingSubDir.resolve(
            baseName + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.writeString(tempMappingPath, mappingContent);
        return tempMappingPath;
    }

    private String applyUniqueBase(
        String mappingContent,
        String domainName,
        String mappingId,
        String baseName
    ) {
        String uniqueBase =
            "http://example.org/mappings/" +
            domainName +
            "/" +
            mappingId +
            "/" +
            baseName +
            "/";
        String baseLine = "@base <" + uniqueBase + "> .";
        String withoutBase = mappingContent.replaceAll(
            "(?m)^@base\\s+<[^>]+>\\s*\\.\\s*$\\R?",
            ""
        );
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

    private List<Path> listFilesWithExtension(Path dir, String extension)
        throws IOException {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(dir)) {
            stream
                .filter(Files::isRegularFile)
                .filter(path ->
                    path
                        .getFileName()
                        .toString()
                        .toLowerCase()
                        .endsWith(extension)
                )
                .forEach(files::add);
        }
        return files;
    }

    private List<Path> listDomainDirectories() throws IOException {
        List<Path> dirs = new ArrayList<>();
        try (Stream<Path> stream = Files.list(Config.MAPPINGS_DIR)) {
            stream.filter(Files::isDirectory).forEach(dirs::add);
        }
        return dirs;
    }

    private String mappingId(Path domainDir, Path mappingFile) {
        Path relativePath = domainDir.relativize(mappingFile);
        String relativeName = stripExtension(relativePath.toString());
        return relativeName.replace("\\", "/");
    }

    private String normalizePath(Path path) {
        return path.toAbsolutePath().normalize().toString().replace("\\", "/");
    }

    private String stripExtension(String fileName) {
        int extensionStart = fileName.lastIndexOf('.');
        if (extensionStart == -1) {
            return fileName;
        }
        return fileName.substring(0, extensionStart);
    }
}
