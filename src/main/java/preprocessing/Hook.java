package preprocessing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import utils.XMLObject;

public abstract class Hook {
    private Path dataDir;
    private static final String XML_EXTENSION = ".xml";

    /**
     * This method will be called before the mapping process begins.
    */
    public abstract void execute(ProcessingContext context);

    public abstract String getName();

    protected void registerLookupTable(ProcessingContext context, String key, String value) {
        String hookName = getName();
        Map<String, String> table = context.getLookupTable(hookName).orElse(new HashMap<>());
        table.put(key, value);
        context.registerLookupTable(hookName, table);
    }

    protected void setDataDir(Path dataDir) {
        if (dataDir == null || !Files.isDirectory(dataDir)) {
            throw new IllegalArgumentException("Invalid data directory: " + dataDir);
        }
        this.dataDir = dataDir;
    }

    protected void log(String message) {
        System.out.println("[" + getName() + "] " + message);
    }

    protected List<XMLObject> loadDocuments(String resourceName) {
        Path resourceDir = dataDir.resolve(resourceName);
        if (!Files.isDirectory(resourceDir)) {
            throw new IllegalStateException("Resource directory not found: " + resourceDir);
        }

        try (Stream<Path> paths = Files.list(resourceDir)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase().endsWith(XML_EXTENSION))
                .map(this::parseXml)
                .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load XML files from: " + resourceDir + ": " + e.getMessage(), e);
        }
    }

    protected XMLObject parseXml(Path xmlPath) {
        try {
            String raw = Files.readString(xmlPath);
            String cleaned = XMLObject.removeBom(raw);
            XMLObject xmlObject = new XMLObject(cleaned);
            return xmlObject;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse XML: " + xmlPath + ": " + e.getMessage(), e);
        }
    }

}
