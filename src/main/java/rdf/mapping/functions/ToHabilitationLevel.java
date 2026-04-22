package rdf.mapping.functions;

import java.util.Map;

public class ToHabilitationLevel {

    private static final String BIO_NS = "http://purl.org/polis/ar/biographic#";

    private static final Map<String, String> habilitationLevelMap = Map.of(
        "9.0", BIO_NS + "PrimarySchool",
        "10.0", BIO_NS + "MiddleSchool",
        "11.0", BIO_NS + "EarlyHighSchool",
        "12.0", BIO_NS + "HighSchool",
        "13.0", BIO_NS + "HigherEducation",
        "14.0", BIO_NS + "Bachelor",
        "15.0", BIO_NS + "Master",
        "16.0", BIO_NS + "Postgrad"  
    );

    public static String toHabilitationLevel(String inputHabilitationLevel){
        if (inputHabilitationLevel == null) {
            return null;
        }

        String normalized = inputHabilitationLevel.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        return habilitationLevelMap.get(normalized);
    }

}
