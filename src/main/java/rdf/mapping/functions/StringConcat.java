package rdf.mapping.functions;

public class StringConcat {

    public static String stringConcat(String leftPart, String rightPart) {
        if (leftPart == null || rightPart == null) {
            return null;
        }

        return leftPart + rightPart;
    }
}