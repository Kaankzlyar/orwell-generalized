import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://app.parlamento.pt/webutils/docs/doc.xml?path=N3jRPJFnnPw07UGV%2f4k0mwzgbG2Jl3wjBTiC8TM1giRsecyPDp4yf3k8FPfa4jKxK8cK%2bHg9qZBa2M5gomJKy%2fQtnk5tb5Z5r8BVFPJMhxvRcyvUOUPpY%2bt87ZuUHz9BvAd789KDSyluIGUKEnTFo9Dpn1Ykljq4eeqISeeUTJT5D1DUC%2f%2biDqKA553hfRTTsEe86A1ObPNZtwCqvv05dxSywmi%2ftOnvfCrhUXy1NfIbEKmFTBEf0i6Ucx0Up0bYMVphP5VomK8C%2bxF7onHEFOpMyRuY9v81e%2bKpmj%2fFjokJsrEnU%2baxPEeUTE9%2bmeOp2g6zC2kPSEZAn7%2fJWt4sioTjDjQ20aK58Q4QBUcFIkw%3d&fich=InformacaoBaseXV.xml&Inline=true"))
                .GET()
                .build();

        // Keep remote fetch code for later use:
        // HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Path dataRootDir = Path.of("data");
        Path mappingsDir = Path.of("mappings");
        Path outputDir = Path.of("output");
        Path tempMappingsDir = outputDir.resolve("tmp-mappings");

        if (!Files.isDirectory(dataRootDir)) {
            throw new IllegalStateException("Data directory does not exist: " + dataRootDir);
        }
        if (!Files.isDirectory(mappingsDir)) {
            throw new IllegalStateException("Mappings directory does not exist: " + mappingsDir);
        }

        Files.createDirectories(outputDir);
        Files.createDirectories(tempMappingsDir);

        List<Path> mappingFiles = new ArrayList<>();
        try (Stream<Path> files = Files.list(mappingsDir)) {
            files
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".ttl"))
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .forEach(mappingFiles::add);
        }

        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No mapping files found in: " + mappingsDir);
        }

        List<Path> failedFiles = new ArrayList<>();
        for (Path mappingFile : mappingFiles) {
            String mappingName = mappingFile.getFileName().toString().replaceFirst("\\.[^.]+$", "");
            Path mappingDataDir = dataRootDir.resolve(mappingName);
            if (!Files.isDirectory(mappingDataDir)) {
                System.out.println("Skipping mapping " + mappingName + ": no data directory at " + mappingDataDir);
                continue;
            }

            String mappingTemplate = Files.readString(mappingFile);
            List<Path> xmlFiles = new ArrayList<>();
            try (Stream<Path> files = Files.list(mappingDataDir)) {
                files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".xml"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(xmlFiles::add);
            }

            if (xmlFiles.isEmpty()) {
                System.out.println("Skipping mapping " + mappingName + ": no XML files in " + mappingDataDir);
                continue;
            }

            Path tempMappingSubDir = tempMappingsDir.resolve(mappingName);
            Path outputSubDir = outputDir.resolve(mappingName);
            Files.createDirectories(tempMappingSubDir);
            Files.createDirectories(outputSubDir);

            for (Path xmlPath : xmlFiles) {
                String xmlFileName = xmlPath.getFileName().toString();
                String baseName = xmlFileName.replaceFirst("\\.[^.]+$", "");

                String xmlSource = xmlPath.toString().replace("\\", "/");
                String mappingContent = mappingTemplate.replaceFirst(
                    "rml:source\\s+\"[^\"]*\"\\s*;",
                    "rml:source \"" + xmlSource + "\" ;"
                );

                Path tempMappingPath = tempMappingSubDir.resolve(baseName + ".ttl");
                Files.writeString(tempMappingPath, mappingContent);

                Path outputPath = outputSubDir.resolve(baseName + ".ttl");
                RDFMapper mapper = new RDFMapper(tempMappingPath, outputPath);

                try {
                    mapper.map();
                    System.out.println("Mapped [" + mappingName + "]: " + xmlFileName + " -> " + outputPath);
                } catch (IOException | InterruptedException e) {
                    failedFiles.add(xmlPath);
                    System.err.println("Failed mapping [" + mappingName + "] " + xmlFileName + ": " + e.getMessage());
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (!failedFiles.isEmpty()) {
            throw new IllegalStateException("Mapping failed for " + failedFiles.size() + " file(s). See errors above.");
        }

        // Guardar XML files todos em data folder
        // Processar todos ao mesmo tempo com:
        // java -jar rmlmapper.jar -m mappings/ -o output/output.ttl
        // rml:source tem que ser folder com XML -> data/
    }
}
