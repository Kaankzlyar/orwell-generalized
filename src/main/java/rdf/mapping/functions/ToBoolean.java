package rdf.mapping.functions;

public class ToBoolean {

    public static Boolean toBoolean(String stringValue) {
        
        System.out.println("Received input: '" + stringValue + "'"); // Debugging output
        if (stringValue == null) {
            return null;
        }

        String normalized = stringValue.trim().toLowerCase();

        System.out.println("Normalized input: '" + normalized + "'"); // Debugging output

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
