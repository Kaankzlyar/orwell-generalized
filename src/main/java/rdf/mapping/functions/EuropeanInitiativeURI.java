package rdf.mapping.functions;

import java.net.URI;
import java.util.Arrays;

public class EuropeanInitiativeURI {

    private static final String PREFIX =
        "http://purl.org/polis/ar/graph#EuropeanInitiative_";

    public static String getURI(String url) {
        String query = URI.create(url).getQuery();
        if (query == null) {
            System.err.println("No query string found in URL: " + url);
            return null;
        }

        return Arrays.stream(query.split("&"))
            .filter(p -> p.startsWith("BID="))
            .map(p -> p.split("=", 2)[1])
            .findFirst()
            .map(bid -> PREFIX + bid)
            .orElseGet(() -> {
                System.err.println("No BID parameter found in URL: " + url);
                return null;
            });
    }
}
