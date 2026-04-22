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
    private static final HashMap<String, String> eventTypeMap = new HashMap<>();
    private static final HashMap<String, String> delegationScopeMap = new HashMap<>();

    static {
        fillSituationMap();
        fillDutyMap();
        fillSchoolTypeMap();
        fillEventTypeMap();
        fillDelegationScopeMap();

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

    private static void fillEventTypeMap() {
        eventTypeMap.put("cerimonia", POLIS_MPACT_NS + "Cerimony");
        eventTypeMap.put("conferencia", POLIS_MPACT_NS + "Conference");
        eventTypeMap.put("debate", POLIS_MPACT_NS + "Debate");
        eventTypeMap.put("outros", POLIS_MPACT_NS + "Others");
    }

    private static void fillDelegationScopeMap() {
        delegationScopeMap.put("nacional", POLIS_MPACT_NS + "NationalDelegation");
        delegationScopeMap.put("internacional", POLIS_MPACT_NS + "InternationalDelegation");
    }

    public static String toEnglish(String entityName, String className) {

        if (entityName == null || className == null) {
            return null;
        }

        String normalizedInput = normalize(entityName);

        switch (className) {
            case "situation":
                return situationMap.getOrDefault(normalizedInput, normalizedInput);
            case "duty":
                return dutyMap.getOrDefault(normalizedInput, normalizedInput);
            case "schoolType":
                return schoolTypeMap.getOrDefault(normalizedInput, normalizedInput);
            case "eventType":
                return eventTypeMap.getOrDefault(normalizedInput, normalizedInput);
            case "delegationScope":
                return delegationScopeMap.getOrDefault(normalizedInput, normalizedInput);
            default:
                return null;
        }
    }

    private static String normalize(String input) {
        String trimmedInput = input.trim().toLowerCase(Locale.ROOT);
        String decomposedInput = Normalizer.normalize(trimmedInput, Normalizer.Form.NFD);

        return decomposedInput.replaceAll("\\p{M}+", "");
    }
}
