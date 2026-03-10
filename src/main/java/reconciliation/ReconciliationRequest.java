package reconciliation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
final class ReconciliationRequest {

    private static final String WIKIDATA_ENDPOINT = "https://wikidata.reconci.link/en/api";

    private final String query;
    private final String type;
    private final int limit;

    HttpRequest toHttpRequest(ObjectMapper objectMapper) throws Exception {
        String formBody = "queries=" + URLEncoder.encode(buildQueriesJson(objectMapper), StandardCharsets.UTF_8);
        return HttpRequest.newBuilder(URI.create(WIKIDATA_ENDPOINT))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();
    }

    private String buildQueriesJson(ObjectMapper objectMapper) throws Exception {
        JsonNode queryNode = objectMapper.createObjectNode()
                .put("query", query)
                .put("type", type)
                .put("limit", limit);
        JsonNode queriesNode = objectMapper.createObjectNode()
                .set("q0", queryNode);
        return objectMapper.writeValueAsString(queriesNode);
    }
}
