package reconciliation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;

public class WikidataReconciliationService{
    
    private static final String WIKIDATA_ENDPOINT = "https://wikidata.reconci.link/en/api";
    private static final String WIKIDATA_ENTITY = "Q35120";
    private static final String BASE_URI = "http://www.wikidata.org/entity/";
    private static final int DEFAULT_LIMIT = 1;
    private static final Path LOG_PATH = Path.of("log.txt");
    private static final Object LOG_LOCK = new Object();

    public static String reconciliate(String entityCandidate, String entityType){
        return reconciliate(entityCandidate, entityType, null);
    }

    public static String reconciliate(String entityCandidate, String entityType, String entityLimit){

        System.out.println("Reconciliating:" + entityCandidate);

        if (entityCandidate == null) {
            return null;
        }

        // Q35120 represents anything in Wikidata
        String type = entityType == null ? WIKIDATA_ENTITY : entityType;
        int limit = parseLimit(entityLimit);

        String query = entityCandidate.trim();
        if (query.isEmpty()) {
            return null;
        }

        ReconciliationResult result = fetchEntity(query, type, limit);
        logReconciliation(query, type, limit, result);

        if (result == null || result.id() == null || result.id().isBlank()) {
            return null;
        }

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

            JsonNode queryNode = objectMapper.createObjectNode()
                    .put("query", query)
                    .put("type", type)
                    .put("limit", limit);
            JsonNode queriesNode = objectMapper.createObjectNode()
                    .set("q0", queryNode);
            String queriesJson = objectMapper.writeValueAsString(queriesNode);

            String formBody = "queries=" + URLEncoder.encode(queriesJson, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(WIKIDATA_ENDPOINT))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .build();

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
        String id = result == null ? "" : safe(result.id());
        String name = result == null ? "" : safe(result.name());
        String score = result == null ? "" : safe(result.score());
        String matched = result == null ? "" : safe(result.matched());

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
}
