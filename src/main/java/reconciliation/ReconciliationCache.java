package reconciliation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import config.Config;

final class ReconciliationCache {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    ReconciliationCache() {
        load();
    }

    Optional<String> get(String query) {
        String cached = cache.get(query);
        if (cached == null || cached.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(cached);
    }

    void put(String query, ReconciliationResult result) {
        if (result == null || result.id() == null || result.id().isBlank()) {
            return;
        }

        String id = result.id().trim();
        String existing = cache.get(query);
        if (!id.equals(existing)) {
            cache.put(query, id);
        }
    }

    void persist() {
        synchronized (lock) {
            Properties properties = new Properties();
            properties.putAll(cache);
            try (var writer = Files.newBufferedWriter(
                    Config.CACHE_PATH,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            )) {
                properties.store(writer, "Wikidata reconciliation cache");
            } catch (Exception e) {
                System.err.println("Failed to write reconciliation cache: " + e.getMessage());
            }
        }
    }

    private void load() {
        if (!Files.exists(Config.CACHE_PATH)) {
            return;
        }

        synchronized (lock) {
            Properties properties = new Properties();
            try (var reader = Files.newBufferedReader(Config.CACHE_PATH, StandardCharsets.UTF_8)) {
                properties.load(reader);
                for (String name : properties.stringPropertyNames()) {
                    String value = properties.getProperty(name);
                    if (value != null && !value.isBlank()) {
                        cache.put(name, value);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load reconciliation cache: " + e.getMessage());
            }
        }
    }
}
