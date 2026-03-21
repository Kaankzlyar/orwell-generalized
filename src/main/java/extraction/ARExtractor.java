package extraction;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class ARExtractor extends DataExtractor {

    protected Path SOURCE_PATH() {
        return Path.of("sources", "ar.json");
    }

    protected String getFileExtension() {
        return ".xml";
    }

    protected Map<String, Map<String, URI>> parseConfig(Path sourcePath) {
        if (!Files.exists(sourcePath)) {
            throw new IllegalStateException("Sources file does not exist: " + sourcePath);
        }

        try {
            JsonNode root = objectMapper.readTree(sourcePath.toFile());
            Map<String, Map<String, URI>> sources = new HashMap<>();

            root.properties().forEach(entry -> {
                // AR Resource name such as "iniciativas"
                String dataset = entry.getKey();
                JsonNode value = entry.getValue();

                Map<String, URI> items = parseUriMap(value, dataset);
                sources.put(dataset, items);
            });

            return sources;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read source config: " + e.getMessage(), e);
        }
    }

}
