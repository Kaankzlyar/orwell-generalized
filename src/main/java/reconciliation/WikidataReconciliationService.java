package reconciliation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class WikidataReconciliationService{
    
    private static final String WIKIDATA_ENDPOINT = "https://wikidata.reconci.link/en/api";
    private static final String BASE_URI = "http://www.wikidata.org/entity/";

    public static String reconciliate(String entityCandidate){
        if (entityCandidate == null) {
            return null;
        }

        String query = entityCandidate.trim();
        if (query.isEmpty()) {
            return null;
        }

        String id = fetchEntity(query);
        if (id == null || id.isBlank()) {
            return null;
        }

        return BASE_URI + id;
    }

    private static String fetchEntity(String query) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            JsonNode queryNode = objectMapper.createObjectNode()
                    .put("query", query)
                    .put("limit", 1);
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

            String id = resultNode.get(0).path("id").asText("");
            return id.isBlank() ? null : id;
        } catch (Exception e) {
            System.err.println("Wikidata reconciliation call failed: " + e.getMessage());
            return null;
        }
    }
}
