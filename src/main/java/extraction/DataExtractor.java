package extraction;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static config.Config.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class DataExtractor {

    public void extract() {
        Path sourcePath = SOURCE_PATH();
        List<SourceNode> sources = parseSources(sourcePath);
        storeData(sources);
    }

    protected abstract List<SourceNode> parseSources(Path sourcePath);

    protected abstract Path SOURCE_PATH();

    private void storeData(List<SourceNode> sources) {
        try {
            Files.createDirectories(DATA_DIR);
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

            for (SourceNode node : sources) {
                storeNode(httpClient, DATA_DIR, node);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store data: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a directory if the node is an object, downloads the file if the node is a key - value pair.
     * @param httpClient
     * @param parentDir
     * @param node
     * @throws Exception
     */
    private void storeNode(HttpClient httpClient, Path parentDir, SourceNode node) throws Exception {
        switch (node) {
            case SourceNode.SourceValue sourceValue -> {
                // Download the file
                HttpResponse<byte[]> response = fetchData(httpClient, sourceValue.uri());
                String format = inferFormat(response);
                Path target = parentDir.resolve(sourceValue.key() + format);
                Files.write(target, response.body());
            }
            case SourceNode.SourceObject sourceObject -> {
                // Create a directory
                Path dir = parentDir.resolve(sourceObject.key());
                Files.createDirectories(dir);
                for (SourceNode child : sourceObject.children()) {
                    storeNode(httpClient, dir, child);
                }
            }
        }
    }

    /**
     * Infers the file format by the Content-Type http header
     * @param response
     * @return
     */
    private String inferFormat(HttpResponse<byte[]> response) {
        String contentType = response.headers().firstValue("Content-Type").orElse("");
        return switch (contentType) {
            case String ct when ct.contains("application/xml") || ct.contains("text/xml") -> ".xml";
            case String ct when ct.contains("text/csv") -> ".csv";
            case String ct when ct.contains("application/json") -> ".json";
            default -> "";
        };
    }

    private static HttpResponse<byte[]> fetchData(HttpClient httpClient, URI uri) {
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
            return response;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to download " + uri + ": " + e.getMessage(), e);
        }
    }
}
