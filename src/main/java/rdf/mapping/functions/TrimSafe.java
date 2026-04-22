package rdf.mapping.functions;

public class TrimSafe {

    public static String trimSafe(String value) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }
}
