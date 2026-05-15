package extraction;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import config.Config;

public class ARExtractor extends DataExtractor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    protected Path SOURCE_PATH() {
        return Path.of("sources", "ar.json");
    }

    protected String getName(){
        return "AR";
    }

    protected List<SourceNode> parseSources(Path sourcePath) {
        if (!Files.exists(sourcePath)) {
            throw new IllegalStateException("Sources file does not exist: " + sourcePath);
        }

        try {
            JsonNode root = objectMapper.readTree(sourcePath.toFile());
            List<SourceNode> config = new ArrayList<>();

            root.properties().forEach(entry -> {
                String dataset = entry.getKey();
                JsonNode value = entry.getValue();

                List<SourceNode> children = new ArrayList<>();
                value.properties().forEach(item -> {
                    String legislature = item.getKey();

                    if (Config.DISABLED_LEGISLATURES.contains(legislature)) {
                        System.out.println("[AR Extractor] Skipping disabled legislature: " + legislature);
                        return;
                    }

                    JsonNode urlNode = item.getValue();

                    if (!urlNode.isTextual()) {
                        throw new IllegalStateException(
                                "Invalid source config: expected URL string for " + dataset + "/" + legislature
                        );
                    }
                    String url = urlNode.asText().trim();
                    if (url.isEmpty()) {
                        throw new IllegalStateException(
                                "Invalid source config: empty URL for " + dataset + "/" + legislature
                        );
                    }
                    children.add(new SourceNode.SourceValue(legislature, URI.create(url)));
                });

                config.add(new SourceNode.SourceObject(dataset, children));
            });

            return config;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read source config: " + e.getMessage(), e);
        }
    }
}
