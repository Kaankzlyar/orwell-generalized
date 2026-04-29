package rdf.mapping.functions;

public class StripDecimal {
    public static String stripDecimal(String value) {
        if (value == null) {
            return null;
        }
        if (value.endsWith(".0")) {
            return value.substring(0, value.length() - 2);
        }
        return value;
    }
}
