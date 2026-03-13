package extraction;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataExtractor{

    private final Path DATA_DIR;
    private static final Path SOURCE = Path.of("sources.json");

    private static final String XML_EXTENSION = ".xml";

    public DataExtractor(Path dataDir){
        this.DATA_DIR = dataDir;
    }

    public void extract() {
        Map<String, Map<String, URI>> sources = loadSources();
        storeData(sources);
    }

    private Map<String, Map<String, URI>> loadSources(){
        if (!Files.exists(SOURCE)) {
            throw new IllegalStateException("Source file does not exist: " + SOURCE);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(SOURCE.toFile());

            Map<String, Map<String, URI>> sources = new HashMap<>();
            root.properties().forEach(entry -> {
                
                String name = entry.getKey();
                JsonNode legislatures = entry.getValue();

                Map<String, URI> items = new HashMap<>();
                legislatures.properties().forEach(item -> {
                    String legislature = item.getKey();
                    JsonNode urlNode = item.getValue();
                    if (!urlNode.isTextual()) {
                        throw new IllegalStateException(
                                "Invalid sources.json: expected URL string for " + name + "/" + legislature
                        );
                    }
                    String url = urlNode.asText().trim();
                    if (url.isEmpty()) {
                        throw new IllegalStateException(
                                "Invalid sources.json: empty URL for " + name + "/" + legislature
                        );
                    }
                    items.put(legislature, URI.create(url));
                });

                sources.put(name, items);
            });
            return sources;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read sources.json: " + e.getMessage(), e);
        }
    }

    private void storeData(Map<String, Map<String, URI>> sources){
        try {
            Files.createDirectories(DATA_DIR);
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

            for (var datasetEntry : sources.entrySet()) {
                String dataset = datasetEntry.getKey();
                Path datasetDir = DATA_DIR.resolve(dataset);
                Files.createDirectories(datasetDir);

                for (var itemEntry : datasetEntry.getValue().entrySet()) {
                    String legislature = itemEntry.getKey();
                    URI uri = itemEntry.getValue();

                    String fileName = legislature + XML_EXTENSION;
                    Path target = datasetDir.resolve(fileName);

                    byte[] content = fetchXml(httpClient, uri);
                    Files.write(target, content);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store data: " + e.getMessage(), e);
        }
    }

    private static byte[] fetchXml(HttpClient httpClient, URI uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Failed to download " + uri + ": HTTP " + status);
            }
            return response.body();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to download " + uri + ": " + e.getMessage(), e);
        }
    }
}
