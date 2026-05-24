package preprocessing.hooks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import preprocessing.Hook;
import preprocessing.ProcessingContext;

public class RemoveEmptyXmlElements extends Hook {

    private static final Pattern SELF_CLOSING = Pattern.compile(
        "<[a-zA-Z_][a-zA-Z0-9_:.-]*[^>]*/>"
    );

    @Override
    public String getName() {
        return "Remove Empty XML Elements";
    }

    @Override
    public void execute(ProcessingContext context) {
        try (Stream<Path> paths = streamDocuments("ar/atividadedeputado")) {
            paths.forEach(this::processDocument);
        }
    }

    private void processDocument(Path path) {
        try {
            String content = Files.readString(path);
            String modified = SELF_CLOSING.matcher(content).replaceAll("");
            if (!modified.equals(content)) {
                Files.writeString(path, modified);
                log("Removed self-closing elements from " + path.getFileName());
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to process: " + path + ": " + e.getMessage(),
                e
            );
        }
    }
}
