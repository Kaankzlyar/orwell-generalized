package rdf.mapping.functions;

public class DatePeriodFormatter {

    public static String toIso8601Period(String startDate, String endDate) {
        if (startDate == null) {
            return null;
        }

        String normalizedStart = startDate.trim();
        if (normalizedStart.isEmpty()) {
            return null;
        }

        if (endDate == null) {
            return normalizedStart;
        }

        String normalizedEnd = endDate.trim();
        if (normalizedEnd.isEmpty()) {
            return normalizedStart;
        }

        return normalizedStart + "/" + normalizedEnd;
    }
}
