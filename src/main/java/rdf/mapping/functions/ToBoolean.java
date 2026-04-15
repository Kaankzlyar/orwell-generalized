package rdf.mapping.functions;

public class ToBoolean {

    public static Boolean toBoolean(String stringValue) {
        
        if (stringValue == null) {
            return null;
        }

        String normalized = stringValue.trim().toLowerCase();

        // Add more cases if needed
        switch (normalized) {
            case "s":
                return true;
            case "n":
                return false;
            default:
                throw new IllegalArgumentException("Cannot convert '" + stringValue + "' to boolean.");
        }
    }
}
