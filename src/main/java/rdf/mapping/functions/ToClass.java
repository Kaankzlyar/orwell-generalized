package rdf.mapping.functions;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;

public class ToClass {

    private static final String POLIS_CORE_NS = "http://purl.org/polis/ar/core#";
    private static final String POLIS_MPACT_NS = "http://purl.org/polis/ar/mp-activity#";
    private static final String POLIS_BIO_NS = "http://purl.org/polis/ar/biographic#";
    private static final String POLIS_INI_NS = "http://purl.org/polis/ar/initiatives#";
    private static final String POLIS_GRAPH_NS = "http://purl.org/polis/ar/graph#";

    private static final HashMap<String, String> situationMap = new HashMap<>();
    private static final HashMap<String, String> dutyMap = new HashMap<>();
    private static final HashMap<String, String> schoolTypeMap = new HashMap<>();
    private static final HashMap<String, String> eventTypeMap = new HashMap<>();
    private static final HashMap<String, String> delegationScopeMap = new HashMap<>();
    private static final HashMap<String, String> habilitationLevelMap = new HashMap<>();
    private static final HashMap<String, String> requisitionMap = new HashMap<>();
    private static final HashMap<String, String> initiativeMap = new HashMap<>();
    private static final HashMap<String, String> otherProponentMap = new HashMap<>();
    private static final HashMap<String, String> darPublicationMap = new HashMap<>();

    static {
        fillSituationMap();
        fillDutyMap();
        fillSchoolTypeMap();
        fillEventTypeMap();
        fillDelegationScopeMap();
        fillHabilitationLevelMap();
        fillRequisitionMap();
        fillInitiativeMap();
        fillOtherProponentMap();
        fillDarPublicationMap();
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
        eventTypeMap.put("coloquio", POLIS_MPACT_NS + "Colloquium");
        eventTypeMap.put("exposicoes", POLIS_MPACT_NS + "Exhibitions");
        eventTypeMap.put("audicao publica", POLIS_MPACT_NS + "PublicAudition");
        eventTypeMap.put("seminario", POLIS_MPACT_NS + "Seminar");
        eventTypeMap.put("congresso", POLIS_MPACT_NS + "Congress");
        eventTypeMap.put("sessao solene", POLIS_MPACT_NS + "SolemnSession");
        eventTypeMap.put("jornadas", POLIS_MPACT_NS + "Journeys");
        eventTypeMap.put("outros", POLIS_MPACT_NS + "Others");
    }

    private static void fillDelegationScopeMap() {
        delegationScopeMap.put("nacional", POLIS_MPACT_NS + "NationalDelegation");
        delegationScopeMap.put("internacional", POLIS_MPACT_NS + "InternationalDelegation");
    }

    private static void fillHabilitationLevelMap() {
        habilitationLevelMap.put("0.0", POLIS_BIO_NS + "HighestLevelNotInformed");
        habilitationLevelMap.put("9.0", POLIS_BIO_NS + "PrimarySchool");
        habilitationLevelMap.put("10.0", POLIS_BIO_NS + "MiddleSchool");
        habilitationLevelMap.put("11.0", POLIS_BIO_NS + "EarlyHighSchool");
        habilitationLevelMap.put("12.0", POLIS_BIO_NS + "HighSchool");
        habilitationLevelMap.put("13.0", POLIS_BIO_NS + "HigherEducation");
        habilitationLevelMap.put("14.0", POLIS_BIO_NS + "Bachelor");
        habilitationLevelMap.put("15.0", POLIS_BIO_NS + "Master");
        habilitationLevelMap.put("16.0", POLIS_BIO_NS + "Postgrad");
    }

    private static void fillRequisitionMap() {
        requisitionMap.put("req", POLIS_MPACT_NS + "Request");
        requisitionMap.put("per", POLIS_MPACT_NS + "Inquiry");
    }

    private static void fillInitiativeMap() {
        initiativeMap.put("proposta de lei", POLIS_INI_NS + "LawProposal");
        initiativeMap.put("projeto de lei", POLIS_INI_NS + "LawProject");
        initiativeMap.put("proposta de resolucao", POLIS_INI_NS + "ResolutionProposal");
        initiativeMap.put("projeto de resolucao", POLIS_INI_NS + "ResolutionProject");
        initiativeMap.put("ratificacao", POLIS_INI_NS + "Ratification");
        initiativeMap.put("projeto de deliberacao", POLIS_INI_NS + "DeliberationProject");
        initiativeMap.put("apreciacao parlamentar", POLIS_INI_NS + "ParliamentaryAppreciation");
        initiativeMap.put("inquerito parlamentar", POLIS_INI_NS + "ParliamentaryInquiry");
        initiativeMap.put("iniciativa popular de referendo", POLIS_INI_NS + "PopularReferendumInitiative");
        initiativeMap.put("projeto de revisao constitucional", POLIS_INI_NS + "ConstitutionalRevisionProject");
        initiativeMap.put("projeto de regimento", POLIS_INI_NS + "RulesOfProcedureProject");
    }

    private static void fillOtherProponentMap() {
        otherProponentMap.put("par", POLIS_CORE_NS + "PresidentOfTheAssemblyOfTheRepublic");
        otherProponentMap.put("cidadaos", POLIS_INI_NS + "CitizenGroup");
    }

    private static void fillDarPublicationMap() {
        darPublicationMap.put("a", POLIS_GRAPH_NS + "DARSeries2A");
        darPublicationMap.put("b", POLIS_GRAPH_NS + "DARSeries2B");
        darPublicationMap.put("c", POLIS_GRAPH_NS + "DARSeries2C");
        darPublicationMap.put("d", POLIS_GRAPH_NS + "DARSeries1");
        darPublicationMap.put("h", POLIS_GRAPH_NS + "DARSeries2E");
        darPublicationMap.put("i", POLIS_GRAPH_NS + "DARSeries2CRC");
        darPublicationMap.put("k", POLIS_GRAPH_NS + "DARSeries2");
        darPublicationMap.put("l", POLIS_GRAPH_NS + "DARSeries2B");
        darPublicationMap.put("m", POLIS_GRAPH_NS + "DARSeries1");
        darPublicationMap.put("o", POLIS_GRAPH_NS + "DARSeries2CGOPOE");
        darPublicationMap.put("q", POLIS_GRAPH_NS + "DARSeries2SCOE");
        darPublicationMap.put("r", POLIS_GRAPH_NS + "DARSeries1A");
        darPublicationMap.put("s", POLIS_GRAPH_NS + "DARSeparata");
        darPublicationMap.put("t", POLIS_GRAPH_NS + "DARSeries2CCEI");
        darPublicationMap.put("v", POLIS_GRAPH_NS + "DARSeries2SOE");
    }

    public static String toClass(String entityName, String className) {

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
            case "habilitationLevel":
                return habilitationLevelMap.getOrDefault(normalizedInput, normalizedInput);
            case "requisitionType":
                return requisitionMap.getOrDefault(normalizedInput, normalizedInput);
            case "initiativeType":
                return initiativeMap.getOrDefault(normalizedInput, normalizedInput);
            case "otherProponent":
                return otherProponentMap.getOrDefault(normalizedInput, null);
            case "darPublication" :
                    return darPublicationMap.getOrDefault(normalizedInput, null);
            case "regionalLegislativeAssembly":

                // Get first 4 words and check if they match "assembleia legislativa da regiao"
                String[] words = normalizedInput.split("\\s+");
                if (words.length >= 4) {
                    String firstFourWords = String.join(" ", words[0], words[1], words[2], words[3]);
                    if (!firstFourWords.equals("assembleia legislativa da regiao")) {
                        return null;
                    }
                    // Extract the region name (the last word) and return the corresponding class
                    String regionName = words[words.length - 1];
                    return POLIS_GRAPH_NS + "RegionalLegislativeAssembly_" + regionName;
                }

                return null;
            default:
                System.out.println("Unknown className: " + className + " for entityName: " + entityName);
                return null;
        }
    }

    private static String normalize(String input) {
        String trimmedInput = input.trim().toLowerCase(Locale.ROOT);
        String decomposedInput = Normalizer.normalize(trimmedInput, Normalizer.Form.NFD);

        return decomposedInput.replaceAll("\\p{M}+", "");
    }
}
