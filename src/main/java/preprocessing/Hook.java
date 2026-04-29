package preprocessing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamReader;

import config.Config;

public abstract class Hook {

    private static final String XML_EXTENSION = ".xml";

    /**
     * This method will be called before the mapping process begins.
    */
    public abstract void execute(ProcessingContext context);

    public abstract String getName();

    /**
     * Adds a key-value pair to a lookup table in the ProcessingContext specific to this hook.
     * @param context
     * @param key
     * @param value
     */
    protected void registerLookupTable(ProcessingContext context, String key, String value) {
        String hookName = getName();
        Map<String, String> table = context.getLookupTable(hookName).orElse(new HashMap<>());
        table.put(key, value);
        context.registerLookupTable(hookName, table);
    }

    protected void log(String message) {
        System.out.println("[" + getName() + "] " + message);
    }

    public static String readElementText(XMLStreamReader reader) {
        try {
            String text = reader.getElementText();
            return text == null ? null : text.trim();
        } catch (Exception e) {
            return null;
        }
    }

    protected Stream<Path> streamDocuments(String resourceName) {
        Path resourceDir = Config.DATA_DIR.resolve(resourceName);
        if (!Files.isDirectory(resourceDir)) {
            throw new IllegalStateException("Resource directory not found: " + resourceDir);
        }
        try {
            return Files.list(resourceDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(XML_EXTENSION));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list XML files from: " + resourceDir + ": " + e.getMessage(), e);
        }
    }
}
