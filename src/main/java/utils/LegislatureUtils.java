package utils;

import java.util.Map;

public class LegislatureUtils {

    private static final String DELIMITER = "\\|";

    /**
     * Finds the legislature associated with a given date.
     *
     * @param legislatureTable the table mapping legislature names to their date ranges
     * @param date the date to search for
     * @return the name of the legislature associated with the date, or null if not found
     */  public static String findLegislatureByDate(Map<String, String> legislatureTable, String date) {
        if (date == null || legislatureTable == null) return null;

        for (Map.Entry<String, String> entry : legislatureTable.entrySet()) {
            String[] parts = entry.getValue().split(DELIMITER);
            if (parts.length == 0) continue;

            String startDate = parts[0];
            if (date.compareTo(startDate) < 0) continue;

            boolean hasEndDate = parts.length > 1 && !parts[1].isEmpty();
            if (hasEndDate && date.compareTo(parts[1]) > 0) continue;

            return entry.getKey();
        }

        return null;
    }
}
