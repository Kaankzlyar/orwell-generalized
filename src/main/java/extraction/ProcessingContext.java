package extraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProcessingContext {
    private final Map<String, Map<String, String>> lookupTables = new HashMap<>();

    public void registerLookupTable(String name, Map<String, String> table) {
        lookupTables.put(name, table);
    }

    public Optional<Map<String, String>> getLookupTable(String name) {
        return Optional.ofNullable(lookupTables.get(name));
    }
}
