package utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern MARKS        = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALNUM    = Pattern.compile("[^\\p{Alnum}\\s]");
    private static final Pattern WHITESPACE   = Pattern.compile("\\s+");

    public static String normalize(String input) {
        return normalize(input, "-");
    }

    public static String normalize(String input, String delimiter) {
        if (input == null) return null;

        String decomposed = Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return WHITESPACE.matcher(
                NON_ALNUM.matcher(
                    MARKS.matcher(decomposed).replaceAll("")
                ).replaceAll("")
            ).replaceAll(delimiter);

    }

    public static String getLastWord(String input) {
        if (input == null || input.isEmpty()) return null;

        String[] words = input.split("\\s+");
        return words[words.length - 1];
    }

    public static String[] getFirstNWords(String input, int n) {
        if (input == null || n <= 0) return null;

        String[] words = input.split("\\s+");
        if (words.length < n) return words;

        String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            result[i] = words[i];
        }
        return result;
    }
}
