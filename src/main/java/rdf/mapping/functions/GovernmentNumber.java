package rdf.mapping.functions;

public class GovernmentNumber {

    public static Integer governmentNumber(String inputString) {
        if (inputString == null || inputString.trim().isEmpty()) {
            return null;
        }

        String firstWord = inputString.trim().split("\\s+")[0];
        return RomanNumeralConverter.romanToDecimal(firstWord);
    }
}
