package rdf.mapping.functions;

public class ToBoolean {

    public static Boolean toBoolean(String stringValue) {
        if (stringValue == null) {
            return null;
        }

        String normalized = stringValue.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return null;
        }

        switch (normalized) {
            case "s":
                return true;
            case "n":
                return false;
            default:
                return null;
        }
    }
}
