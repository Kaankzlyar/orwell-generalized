package rdf.mapping.functions;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;

public class ToEnglish {

    private static final String POLIS_CORE_NS = "http://purl.org/polis/ar/core#";
    private static final HashMap<String, String> situationMap = new HashMap<>();
    private static final HashMap<String, String> dutyMap = new HashMap<>();
    private static final HashMap<String, String> schoolTypeMap = new HashMap<>();

    static {
        fillSituationMap();
        fillDutyMap();
        fillSchoolTypeMap();
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
        situationMap.put("suspenso(eleito)", "Suspended");
        situationMap.put("suspenso(efet def)", "Suspended");
        situationMap.put("suspenso(nao eleito)", "Suspended");
    }

    private static void fillDutyMap() {
        dutyMap.put("presidente", "PAR");
        dutyMap.put("vice-presidente", "VicePAR");
        dutyMap.put("secretario", "Secretary");
        dutyMap.put("vice-secretario", "ViceSecretary");
    }

    private static void fillSchoolTypeMap() {
        schoolTypeMap.put("basico", "Basic");
        schoolTypeMap.put("secundario", "Secondary");
        schoolTypeMap.put("basico/secundario", "BasicSecondary");
    }

    public static String toEnglish(String entityName, String className) {

        if (entityName == null || className == null) {
            return null;
        }

        String normalizedInput = normalize(entityName);

        String englishName;
        if (className.equals("situation")) {
            englishName = situationMap.getOrDefault(normalizedInput, normalizedInput);
        }
        else if (className.equals("duty")) {
            englishName = dutyMap.getOrDefault(normalizedInput, normalizedInput);
        }
        else if (className.equals("schoolType")) {
            englishName = schoolTypeMap.getOrDefault(normalizedInput, normalizedInput);
        }
        else {
            return null;
        }

        return POLIS_CORE_NS + englishName;
    }

    private static String normalize(String input) {
        String trimmedInput = input.trim().toLowerCase(Locale.ROOT);
        String decomposedInput = Normalizer.normalize(trimmedInput, Normalizer.Form.NFD);

        return decomposedInput.replaceAll("\\p{M}+", "");
    }
}
