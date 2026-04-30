package rdf.mapping.functions;

import utils.StringUtils;

public class RegionalAssemblyLocation {
    public static String getRegionalAssemblyLocation(String input) {

        String normalizedInput = StringUtils.normalize(input, " ");
        String[] words = StringUtils.getFirstNWords(normalizedInput, 4);

        if(words.length < 4) return null;

        String firstFourWords = String.join(" ", words[0], words[1], words[2], words[3]);
        if (!firstFourWords.equals("assembleia legislativa da regiao")) {
            return null;
        }

        String lastWord = StringUtils.getLastWord(normalizedInput);

        return lastWord;
    }
}
