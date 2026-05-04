package rdf.mapping.functions;

public class StripParty {

    public static String stripParty(String value) {
        if (value == null) {
            return null;
        }
        int idx = value.indexOf(" (");
        String name = idx > 0 ? value.substring(0, idx) : value;
        return name;
    }
}
