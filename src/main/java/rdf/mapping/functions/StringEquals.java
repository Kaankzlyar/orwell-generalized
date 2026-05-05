package rdf.mapping.functions;

public class StringEquals {

    public static Boolean stringEquals(String string1, String string2) {
        if (string1 == null || string2 == null) {
            return false;
        }

        return string1.equals(string2);
    }
}
