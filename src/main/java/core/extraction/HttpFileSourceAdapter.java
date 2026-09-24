package core.extraction;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import core.config.Config;

public class HttpFileSourceAdapter extends DataExtractor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path sourcesFile;
    private final String domain;

    public HttpFileSourceAdapter(Path sourcesFile, String domain) {
        this.sourcesFile = sourcesFile;
        this.domain = domain;
    }

    protected Path SOURCE_PATH() {
        return sourcesFile;
    }

    protected String getName(){
        return domain;
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
                    String partition = item.getKey();

                    if (Config.DISABLED_PARTITIONS.contains(partition)) {
                        System.out.println("[" + getName() + " Extractor] Skipping disabled partition: " + partition);
                        return;
                    }

                    JsonNode urlNode = item.getValue();

                    if (!urlNode.isTextual()) {
                        throw new IllegalStateException(
                                "Invalid source config: expected URL string for " + dataset + "/" + partition
                        );
                    }
                    String url = urlNode.asText().trim();
                    if (url.isEmpty()) {
                        throw new IllegalStateException(
                                "Invalid source config: empty URL for " + dataset + "/" + partition
                        );
                    }
                    children.add(new SourceNode.SourceValue(partition, URI.create(url)));
                });

                config.add(new SourceNode.SourceObject(dataset, children));
            });

            return config;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read source config: " + e.getMessage(), e);
        }
    }
}
