package rdf.mapping.functions;

import java.text.Normalizer;

public class Normalize {

    public static String normalize(String input) {
        if (input == null) {
            return null;
        }

        // Trim whitespace
        String trimmed = input.trim();

        // Convert to lowercase
        String lowercase = trimmed.toLowerCase();

        // Remove diacritics using NFD normalization
        String nfd = Normalizer.normalize(lowercase, Normalizer.Form.NFD);
        String normalized = nfd.replaceAll("\\p{M}", "");

        return normalized;
    }
}
