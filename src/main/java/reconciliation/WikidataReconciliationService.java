package reconciliation;

import static config.Config.LOG_PATH;

import cli.Options;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class WikidataReconciliationService {

    private static final int DEFAULT_LIMIT = 1;
    private static final String WIKIDATA_ENTITY = "Q35120"; // Q35120 represents anything in Wikidata
    private static final String BASE_URI = "http://www.wikidata.org/entity/";

    private static final ReconciliationCache CACHE = new ReconciliationCache();
    private static final Object LOG_LOCK = new Object();

    public static String reconciliate(
        String entityCandidate,
        String entityType
    ) {
        return reconciliate(entityCandidate, entityType, null);
    }

    public static String reconciliate(
        String entityCandidate,
        String entityType,
        String entityLimit
    ) {
        if (!Options.reconciliationEnabled()) return entityCandidate;

        //System.out.println("Reconciliating:" + entityCandidate);

        if (entityCandidate == null) {
            return null;
        }

        String query = entityCandidate.trim();
        if (query.isEmpty()) {
            return null;
        }

        String type = entityType == null ? WIKIDATA_ENTITY : entityType;
        int limit = parseLimit(entityLimit);

        Optional<String> cachedId = CACHE.get(query);
        if (cachedId.isPresent()) {
            ReconciliationResult cachedResult = new ReconciliationResult(
                cachedId.get(),
                "",
                "",
                ""
            );
            //System.out.println("Found result for query in cache: " + query);
            logReconciliation(query, type, limit, cachedResult);
            return BASE_URI + cachedId.get();
        }

        ReconciliationResult result = fetchEntity(query, type, limit);
        logReconciliation(query, type, limit, result);

        if (result == null || result.id() == null || result.id().isBlank()) {
            return null;
        }

        CACHE.put(query, result);
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

    private static ReconciliationResult fetchEntity(
        String query,
        String type,
        int limit
    ) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

            ReconciliationRequest reconciliationRequest =
                new ReconciliationRequest(query, type, limit);
            HttpRequest request = reconciliationRequest.toHttpRequest(
                objectMapper
            );

            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println(
                    "Wikidata reconciliation request failed with status " +
                        response.statusCode()
                );
                return null;
            }

            // Get the result object of the response
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode resultNode = root.path("q0").path("result");
            if (!resultNode.isArray() || resultNode.isEmpty()) {
                return null;
            }

            // Get the first result
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
            System.err.println(
                "Wikidata reconciliation call failed: " + e.getMessage()
            );
            return null;
        }
    }

    private static void logReconciliation(
        String query,
        String type,
        int limit,
        ReconciliationResult result
    ) {
        if (!Options.logEnabled()) {
            return;
        }

        String id = result == null ? "" : safe(result.id());
        String name = result == null ? "" : safe(result.name());
        String score = result == null ? "" : safe(result.score());

        String line =
            String.join(
                "\t",
                Instant.now().toString(),
                "query=" + safe(query),
                "type=" + safe(type),
                "limit=" + limit,
                " | ",
                "id=" + id,
                "score=" + score,
                "name=" + name
            ) +
            System.lineSeparator();

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
                System.err.println(
                    "Failed to write reconciliation log: " + e.getMessage()
                );
            }
        }
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\t", " ").replace("\n", " ").replace("\r", " ");
    }

    /**
     * Saves the cache to disk
     */
    public static void persistCache() {
        CACHE.persist();
    }
}
