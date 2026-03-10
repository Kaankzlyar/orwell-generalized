package reconciliation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class WikidataReconciliationService{
    
    private static final int DEFAULT_LIMIT = 1;
    private static final String WIKIDATA_ENTITY = "Q35120"; // Q35120 represents anything in Wikidata
    private static final String BASE_URI = "http://www.wikidata.org/entity/";
    
    private static final Path CACHE_PATH = Path.of("reconciliation-cache.properties");
    private static final Object CACHE_LOCK = new Object();
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private static final Path LOG_PATH = Path.of("log.txt");
    private static final Object LOG_LOCK = new Object();
    private static final boolean LOG_ENABLED =
            Boolean.parseBoolean(System.getProperty("orwell.reconciliation.log.enabled", "true"));

    static {
        loadCache();
    }

    public static String reconciliate(String entityCandidate, String entityType){
        return reconciliate(entityCandidate, entityType, null);
    }

    public static String reconciliate(String entityCandidate, String entityType, String entityLimit){

        System.out.println("Reconciliating:" + entityCandidate);

        if (entityCandidate == null) {
            return null;
        }

        String type = entityType == null ? WIKIDATA_ENTITY : entityType;
        int limit = parseLimit(entityLimit);

        String query = entityCandidate.trim();
        if (query.isEmpty()) {
            return null;
        }

        String cachedId = getCachedId(query);
        if (cachedId != null) {
            ReconciliationResult cachedResult = new ReconciliationResult(cachedId, "", "", "");
            System.out.println("Found result for query in cache: " + query);
            logReconciliation(query, type, limit, cachedResult);
            return BASE_URI + cachedId;
        }

        ReconciliationResult result = fetchEntity(query, type, limit);
        logReconciliation(query, type, limit, result);

        if (result == null || result.id() == null || result.id().isBlank()) {
            return null;
        }

        cacheResult(query, result);
        return BASE_URI + result.id();
    }

    private static int parseLimit(String entityLimit) {
        if (entityLimit == null || entityLimit.isBlank()) {
            return DEFAULT_LIMIT;
        }

        try {
            int parsed = Integer.parseInt(entityLimit.trim());
            return parsed > 0 ? parsed : DEFAULT_LIMIT;
        } catch (NumberFormatException e) {
            return DEFAULT_LIMIT;
        }
    }

    private static ReconciliationResult fetchEntity(String query, String type, int limit) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            ReconciliationRequest reconciliationRequest = new ReconciliationRequest(query, type, limit);
            HttpRequest request = reconciliationRequest.toHttpRequest(objectMapper);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println("Wikidata reconciliation request failed with status " + response.statusCode());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode resultNode = root.path("q0").path("result");
            if (!resultNode.isArray() || resultNode.isEmpty()) {
                return null;
            }

            JsonNode firstResult = resultNode.get(0);
            String id = firstResult.path("id").asText("");
            if (id.isBlank()) {
                return null;
            }

            String name = firstResult.path("name").asText("");
            String score = firstResult.path("score").asText("");
            String matched = firstResult.path("match").asText("");
            return new ReconciliationResult(id, name, score, matched);
        } catch (Exception e) {
            System.err.println("Wikidata reconciliation call failed: " + e.getMessage());
            return null;
        }
    }

    private static void logReconciliation(String query, String type, int limit, ReconciliationResult result) {
        if (!LOG_ENABLED) {
            return;
        }

        String id = result == null ? "" : safe(result.id());
        String name = result == null ? "" : safe(result.name());
        String score = result == null ? "" : safe(result.score());

        String line = String.join("\t",
                Instant.now().toString(),
                "query=" + safe(query),
                "type=" + safe(type),
                "limit=" + limit,
                " | ",
                "id=" + id,
                "score=" + score,
                "name=" + name
        ) + System.lineSeparator();

        synchronized (LOG_LOCK) {
            try {
                Files.writeString(
                        LOG_PATH,
                        line,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (Exception e) {
                System.err.println("Failed to write reconciliation log: " + e.getMessage());
            }
        }
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\t", " ").replace("\n", " ").replace("\r", " ");
    }

    private static String getCachedId(String query) {
        String cached = CACHE.get(query);
        if (cached == null || cached.isBlank()) {
            return null;
        }
        return cached;
    }

    private static void cacheResult(String query, ReconciliationResult result) {
        if (result == null || result.id() == null || result.id().isBlank()) {
            return;
        }

        String id = result.id().trim();
        String existing = CACHE.get(query);
        
        if (!id.equals(existing)) {
            CACHE.put(query, id);
        }
    }

    private static void loadCache() {
        if (!Files.exists(CACHE_PATH)) {
            return;
        }

        synchronized (CACHE_LOCK) {
            Properties properties = new Properties();
            try (var reader = Files.newBufferedReader(CACHE_PATH, StandardCharsets.UTF_8)) {
                properties.load(reader);
                for (String name : properties.stringPropertyNames()) {
                    String value = properties.getProperty(name);
                    if (value != null && !value.isBlank()) {
                        CACHE.put(name, value);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load reconciliation cache: " + e.getMessage());
            }
        }
    }

    /**
     * Saves the cache to disk
     */
    public static void persistCache() {
        synchronized (CACHE_LOCK) {
            Properties properties = new Properties();
            properties.putAll(CACHE);
            try (var writer = Files.newBufferedWriter(
                    CACHE_PATH,
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
}
