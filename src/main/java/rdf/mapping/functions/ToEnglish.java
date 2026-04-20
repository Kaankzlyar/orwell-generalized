package rdf.mapping.functions;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;

public class ToEnglish {

    private static final String POLIS_CORE_NS = "http://purl.org/polis/ar/core#";
    private static final String POLIS_MPACT_NS = "http://purl.org/polis/ar/mp-activity#";
    private static final HashMap<String, String> situationMap = new HashMap<>();
    private static final HashMap<String, String> dutyMap = new HashMap<>();
    private static final HashMap<String, String> schoolTypeMap = new HashMap<>();

    static {
        fillSituationMap();
        fillDutyMap();
        fillSchoolTypeMap();
    }

    private static void fillSituationMap() {
        situationMap.put("desistencia", POLIS_CORE_NS + "Withdrawal");
        situationMap.put("efetivo", POLIS_CORE_NS + "Incumbent");
        situationMap.put("efetivo definitivo", POLIS_CORE_NS + "PermanentIncumbent");
        situationMap.put("efetivo temporario", POLIS_CORE_NS + "TemporaryIncumbent");
        situationMap.put("falecido/a", POLIS_CORE_NS + "Deceased");
        situationMap.put("impedido", POLIS_CORE_NS + "Disqualified");
        situationMap.put("perda de mandato", POLIS_CORE_NS + "LossOfMandate");
        situationMap.put("renunciou", POLIS_CORE_NS + "Resigned");
        situationMap.put("suplente", POLIS_CORE_NS + "Alternate");
        situationMap.put("suspenso", POLIS_CORE_NS + "Suspended");
        situationMap.put("suspenso(eleito)", POLIS_CORE_NS + "Suspended");
        situationMap.put("suspenso(efet def)", POLIS_CORE_NS + "Suspended");
        situationMap.put("suspenso(nao eleito)", POLIS_CORE_NS + "Suspended");
    }

    private static void fillDutyMap() {
        dutyMap.put("presidente", POLIS_CORE_NS + "PAR");
        dutyMap.put("vice-presidente", POLIS_CORE_NS + "VicePAR");
        dutyMap.put("secretario", POLIS_CORE_NS + "Secretary");
        dutyMap.put("vice-secretario", POLIS_CORE_NS + "ViceSecretary");
    }

    private static void fillSchoolTypeMap() {
        schoolTypeMap.put("basico", POLIS_MPACT_NS + "Basic");
        schoolTypeMap.put("secundario", POLIS_MPACT_NS + "Secondary");
        schoolTypeMap.put("basico/secundario", POLIS_MPACT_NS + "BasicSecondary");
        schoolTypeMap.put("secundario/basico", POLIS_MPACT_NS + "BasicSecondary");
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

        return englishName;
    }

    private static String normalize(String input) {
        String trimmedInput = input.trim().toLowerCase(Locale.ROOT);
        String decomposedInput = Normalizer.normalize(trimmedInput, Normalizer.Form.NFD);

        return decomposedInput.replaceAll("\\p{M}+", "");
    }
}
