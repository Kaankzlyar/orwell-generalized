package rdf.mapping.functions;

public class ToRequisition {

    private static final String MPACT_NS = "http://purl.org/polis/ar/mp-activity#";

    public static String toRequisition(String inputRequisition){
        if (inputRequisition == null) {
            return null;
        }

        String trimmedInput = inputRequisition.trim();

        switch (trimmedInput) {
            case "REQ":
                return MPACT_NS + "Request";
            case "PER":
                return MPACT_NS + "Inquiry";
            default:
                System.err.println("Warning: Unrecognized requisition type '" + inputRequisition + "'. Returning null.");
                return null;
        }
    }
}
