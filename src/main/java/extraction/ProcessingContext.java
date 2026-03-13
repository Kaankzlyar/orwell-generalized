package extraction;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProcessingContext {
    private final Path xmlInputPath;
    private final Map<String, Map<String, String>> lookupTables = new HashMap<>();

    public ProcessingContext(Path xmlInputPath) {
        this.xmlInputPath = xmlInputPath;
    }

    public void registerLookupTable(String name, Map<String, String> table) {
        lookupTables.put(name, table);
    }

    public Optional<Map<String, String>> getLookupTable(String name) {
        return Optional.ofNullable(lookupTables.get(name));
    }

    public Path getXmlInputPath() { return xmlInputPath; }
}