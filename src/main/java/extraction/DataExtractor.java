package extraction;

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

import config.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class DataExtractor {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    public void extract() {
        Path sourcePath = SOURCE_PATH();
        Map<String, Map<String, URI>> sources = parseConfig(sourcePath);
        storeData(sources);
    }

    protected abstract Map<String, Map<String, URI>> parseConfig(Path sourcePath);

    protected abstract Path SOURCE_PATH();

    protected abstract String getFileExtension();

    /**
     * Store the data locally according to the structure of the source json, under the DATA_DIR specified in the configuration
     * @param sources
     */
    protected void storeData(Map<String, Map<String, URI>> sources) {
        try {
            Files.createDirectories(Config.DATA_DIR);
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

            for (var datasetEntry : sources.entrySet()) {
                String dataset = datasetEntry.getKey();
                Path datasetDir = Config.DATA_DIR.resolve(dataset);
                Files.createDirectories(datasetDir);

                for (var itemEntry : datasetEntry.getValue().entrySet()) {
                    String identifier = itemEntry.getKey();
                    URI uri = itemEntry.getValue();

                    String fileName = identifier + getFileExtension();
                    Path target = datasetDir.resolve(fileName);

                    byte[] content = fetchData(httpClient, uri);
                    Files.write(target, content);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store data: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a map of source identifier to the respective URI
     * Example:
     * "XVII": "www.parlamento.com/.../xvii.xml"
     * @param node
     * @param parentKey
     * @return
     */
    protected Map<String, URI> parseUriMap(JsonNode node, String parentKey) {
        Map<String, URI> items = new HashMap<>();
        node.properties().forEach(entry -> {
            String key = entry.getKey();
            JsonNode urlNode = entry.getValue();
            if (!urlNode.isTextual()) {
                throw new IllegalStateException(
                        "Invalid source config: expected URL string for " + parentKey + "/" + key
                );
            }
            String url = urlNode.asText().trim();
            if (url.isEmpty()) {
                throw new IllegalStateException(
                        "Invalid source config: empty URL for " + parentKey + "/" + key
                );
            }
            items.put(key, URI.create(url));
        });
        return items;
    }

    private static byte[] fetchData(HttpClient httpClient, URI uri) {
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
