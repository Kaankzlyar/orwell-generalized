package rdf.mapping.functions;

public class StringConcat {

    public static String stringConcat(String leftPart, String rightPart) {
        String left = leftPart == null ? "" : leftPart;
        String right = rightPart == null ? "" : rightPart;

        return left + right;
    }
}