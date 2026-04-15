package rdf.mapping.functions;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;

public class ToEnglish {

    private static final HashMap<String, String> situationMap = new HashMap<>();
    private static final HashMap<String, String> dutyMap = new HashMap<>();

    static {
        fillSituationMap();
        fillDutyMap();
    }

    private static void fillSituationMap() {
        situationMap.put("desistencia", "Withdrawal");
        situationMap.put("efetivo", "Incumbent");
        situationMap.put("efetivo definitivo", "PermanentIncumbent");
        situationMap.put("efetivo temporario", "TemporaryIncumbent");
        situationMap.put("falecido/a", "Deceased");
        situationMap.put("impedido", "Disqualified");
        situationMap.put("perda de mandato", "LossOfMandate");
        situationMap.put("renunciou", "Resigned");
        situationMap.put("suplente", "Alternate");
        situationMap.put("suspenso", "Suspended");
    }

    private static void fillDutyMap() {
        dutyMap.put("presidente", "PAR");
        dutyMap.put("vice-presidente", "VicePAR");
        dutyMap.put("secretario", "Secretary");
        dutyMap.put("vice-secretario", "ViceSecretary");
    }

    public static String toEnglish(String entityName, String className) {

        if (entityName == null || className == null) {
            return null;
        }

        String normalizedInput = normalize(entityName);

        if (className.equals("situation")) {
            return situationMap.getOrDefault(normalizedInput, normalizedInput);
        }
        else {
            if (className.equals("duty"))
                return dutyMap.getOrDefault(normalizedInput, normalizedInput);
        }

        return null;
    }

    private static String normalize(String input) {
        String trimmedInput = input.trim().toLowerCase(Locale.ROOT);
        String decomposedInput = Normalizer.normalize(trimmedInput, Normalizer.Form.NFD);

        return decomposedInput.replaceAll("\\p{M}+", "");
    }
}
