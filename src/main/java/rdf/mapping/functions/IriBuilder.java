package rdf.mapping.functions;

public class IriBuilder {

    private static final String PARLIAMENTARY_ACTIVITY_PREFIX = "http://purl.org/polis/ar/graph#ParliamentaryActivity_";

    public static String prefixedIri(String prefix, String value) {
        if (prefix == null || prefix.isBlank() || value == null) {
            return null;
        }

        String normalizedValue = value.trim();
        if (normalizedValue.isEmpty()) {
            return null;
        }

        return prefix + normalizedValue;
    }

    public static String parliamentaryActivityIri(String activityType, String activityId, String legislature) {
        String normalizedType = normalize(activityType);
        String normalizedId = normalize(activityId);
        String normalizedLegislature = normalize(legislature);

        if (normalizedType == null || normalizedId == null || normalizedLegislature == null) {
            return null;
        }

        return PARLIAMENTARY_ACTIVITY_PREFIX
                + normalizedType
                + "_"
                + normalizedId
                + "_of_"
                + normalizedLegislature;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
