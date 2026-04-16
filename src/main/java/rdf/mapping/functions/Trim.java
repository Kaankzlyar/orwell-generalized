package rdf.mapping.functions;

public class Trim {

    public static String trim(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }
}