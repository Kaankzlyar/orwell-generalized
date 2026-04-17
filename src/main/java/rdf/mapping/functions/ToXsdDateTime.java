package rdf.mapping.functions;

public class ToXsdDateTime {

    public static String toXsdDateTime(String inputDateTime) {
        if (inputDateTime == null) {
            return null;
        }

        String normalized = inputDateTime.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() >= 10 && normalized.charAt(4) == '-' && normalized.charAt(7) == '-') {
            if (normalized.length() == 10) {
                return normalized + "T00:00:00";
            }

            if (normalized.length() > 10 && normalized.charAt(10) == ' ') {
                return normalized.substring(0, 10) + "T" + normalized.substring(11);
            }
        }

        return normalized;
    }
}