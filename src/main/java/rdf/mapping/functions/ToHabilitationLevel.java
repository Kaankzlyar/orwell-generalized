package rdf.mapping.functions;

import java.util.Map;

public class ToHabilitationLevel {

    private static final Map<String, String> habilitationLevelMap = Map.of(
        "9.0", "PrimarySchool",
        "10.0", "MiddleSchool",
        "11.0", "EarlyHighSchool",
        "12.0", "HighSchool",
        "13.0", "HigherEducation",
        "14.0", "Bachelor",
        "15.0", "Master",
        "16.0", "Postgrad"  
    );

    public static String toHabilitationLevel(String inputHabilitationLevel){
        return habilitationLevelMap.get(inputHabilitationLevel.trim());
    }

}
