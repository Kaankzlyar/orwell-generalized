package usecase.unescowhs.functions;

import java.util.Map;

public class ToVersion {
    private static final Map<String, Integer> SUFFIX_VERSION = Map.of(
        "rev", 1, "bis", 2, "ter", 3, "quater", 4, "quinquies", 5);
    
    private static final Map<String, String> OVERRIDES = Map.of(
            "893", "rev", "205", "", "155", "");
        
    public static Integer toVersion(String revBis, String idNo) {
        String id = idNo == null ? "" : idNo.trim();
        String text = OVERRIDES.containsKey(id)
                ? OVERRIDES.get(id)
                : revBis == null ? "" : revBis.trim().toLowerCase();
        return SUFFIX_VERSION.get(text);
    }
    
}
