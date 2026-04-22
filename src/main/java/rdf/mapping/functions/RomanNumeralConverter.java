package rdf.mapping.functions;

import java.util.Map;

public class RomanNumeralConverter {

    private static final Map<Character, Integer> ROMAN_VALUES = Map.of(
            'I', 1,
            'V', 5,
            'X', 10,
            'L', 50,
            'C', 100,
            'D', 500,
            'M', 1000
    );

    public static Integer romanToDecimal(String romanNumeral) {
        if (romanNumeral == null) {
            return null;
        }

        String normalized = romanNumeral.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return null;
        }

        int total = 0;
        int previous = 0;

        for (int i = normalized.length() - 1; i >= 0; i--) {
            char symbol = normalized.charAt(i);
            Integer value = ROMAN_VALUES.get(symbol);
            if (value == null) {
                return null;
            }

            if (value < previous) {
                total -= value;
            }
            else {
                total += value;
                previous = value;
            }
        }

        return total;
    }
}
