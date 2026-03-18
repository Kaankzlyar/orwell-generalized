package rdf.mapping;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
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
    private static final String SOURCE_PATTERN = "rml:source\\s+\"[^\"]*\"\\s*;";

    public List<Path> createMappingPairs() throws IOException {
        validateDirectories();
        Files.createDirectories(Config.TMP_DIR);
        Path tmpMappingsDir = Files.createDirectories(Config.TMP_DIR.resolve(Config.MAPPINGS_DIR));

        List<Path> createdMappings = new ArrayList<>();
        List<Path> mappingFiles = listFilesWithExtension(Config.MAPPINGS_DIR, TTL_EXTENSION);
        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No mapping files found in: " + Config.MAPPINGS_DIR);
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
        if (!Files.isDirectory(Config.DATA_DIR)) {
            throw new IllegalStateException("Data directory does not exist: " + Config.DATA_DIR);
        }
        if (!Files.isDirectory(Config.MAPPINGS_DIR)) {
            throw new IllegalStateException("Mappings directory does not exist: " + Config.MAPPINGS_DIR);
        }
    }

    private List<Path> createPairsForMapping(Path mappingFile, Path tmpMappingsDir) throws IOException {
        List<Path> createdMappings = new ArrayList<>();
        String mappingName = stripExtension(mappingFile.getFileName().toString());
        Path mappingDataDir = Config.DATA_DIR.resolve(mappingName);

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
            System.out.println("Created mapping file: " + createdMapping);
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

        String xmlSource = xmlPath.toAbsolutePath().normalize().toString().replace("\\", "/");
        String mappingContent = mappingTemplate.replaceAll(
            SOURCE_PATTERN,
            Matcher.quoteReplacement("rml:source \"" + xmlSource + "\" ;")
        );
        mappingContent = applyUniqueBase(mappingContent, mappingName, baseName);
        if (!Config.RECONCILIATION_ENABLED) {
            mappingContent = stripFunctionBasedPredicateObjectMaps(mappingContent);
        }

        Path tempMappingPath = tempMappingSubDir.resolve(baseName + TTL_EXTENSION);
        Files.writeString(tempMappingPath, mappingContent);
        return tempMappingPath;
    }

    /**
     * This removes the function predicates for dynamically loading Java functions into RMLMapper
     * It is used to generate tmp mapping that do not contain the function calls, for when the reconcilication is disabled
     * @param mappingContent Initial Mapping
     * @return String Mapping with no function predicates
     */
    private String stripFunctionBasedPredicateObjectMaps(String mappingContent) {
        final String marker = "rr:predicateObjectMap [";
        StringBuilder output = new StringBuilder(mappingContent.length());
        int cursor = 0;

        while (true) {
            int blockStart = mappingContent.indexOf(marker, cursor);
            if (blockStart < 0) {
                output.append(mappingContent, cursor, mappingContent.length());
                break;
            }

            output.append(mappingContent, cursor, blockStart);

            int firstBracket = mappingContent.indexOf('[', blockStart);
            if (firstBracket < 0) {
                output.append(mappingContent.substring(blockStart));
                break;
            }

            int depth = 0;
            int index = firstBracket;
            for (; index < mappingContent.length(); index++) {
                char ch = mappingContent.charAt(index);
                if (ch == '[') {
                    depth++;
                } else if (ch == ']') {
                    depth--;
                    if (depth == 0) {
                        index++;
                        break;
                    }
                }
            }

            if (depth != 0) {
                output.append(mappingContent.substring(blockStart));
                break;
            }

            int blockEnd = index;
            while (blockEnd < mappingContent.length() && Character.isWhitespace(mappingContent.charAt(blockEnd))) {
                blockEnd++;
            }

            char terminator = '\0';
            if (blockEnd < mappingContent.length()) {
                char c = mappingContent.charAt(blockEnd);
                if (c == ';' || c == '.') {
                    terminator = c;
                    blockEnd++;
                }
            }

            String block = mappingContent.substring(blockStart, blockEnd);
            if (block.contains("fnml:functionValue")) {
                if (terminator == '.') {
                    output.append('.');
                }
            } else {
                output.append(block);
            }

            cursor = blockEnd;
        }

        return output.toString();
    }

    // TODO: I think rmlmapper has a CLI argument "-b" that sets the base IRI. Maybe solves this?
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
