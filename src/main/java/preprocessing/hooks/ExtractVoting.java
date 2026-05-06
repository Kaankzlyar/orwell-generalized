package preprocessing.hooks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;
import javax.xml.stream.XMLInputFactory;
import preprocessing.Hook;
import preprocessing.ProcessingContext;

public class ExtractVoting extends Hook {

    private static final Pattern CATEGORY_PATTERN = Pattern.compile(
        "([\\w\\u00C0-\\u024F]+ ?[\\w\\u00C0-\\u024F]*):((?:\\s*<I>.*?</I>,?)*)"
    );
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "<I>\\s*([^<]*)\\s*</I>"
    );
    private static final Pattern DETALHE_PATTERN = Pattern.compile(
        "<detalhe>(.*?)</detalhe>",
        Pattern.DOTALL
    );
    private static final Set<String> CATEGORIES = Set.of(
        "A Favor",
        "Contra",
        "Abstenção"
    );

    private static final XMLInputFactory INPUT_FACTORY =
        XMLInputFactory.newInstance();

    static {
        INPUT_FACTORY.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        INPUT_FACTORY.setProperty(
            XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
            false
        );
    }

    @Override
    public String getName() {
        return "Voting Extractor";
    }

    @Override
    public void execute(ProcessingContext context) {
        try (Stream<Path> paths = streamDocuments("ar/iniciativas")) {
            paths.parallel().forEach(path -> processDocument(context, path));
        }
    }

    public Map<String, List<String>> extractVotes(String text) {
        Map<String, List<String>> voteResults = new HashMap<>();
        Matcher matcher = CATEGORY_PATTERN.matcher(text);
        while (matcher.find()) {
            String category = matcher.group(1).trim();
            if (CATEGORIES.contains(category)) {
                String parties = matcher.group(2);
                Matcher nameMatcher = NAME_PATTERN.matcher(parties);
                List<String> names = new ArrayList<>();
                while (nameMatcher.find()) {
                    String name = nameMatcher
                        .group(1)
                        .trim()
                        .replaceAll("\\s*\\(.*?\\)\\s*$", "")
                        .trim();
                    names.add(name);
                }
                voteResults.put(category, names);
            }
        }
        return voteResults;
    }

    private void processDocument(ProcessingContext context, Path path) {
        try {
            String content = Files.readString(path);

            if (!content.contains("<detalhe>")) return;

            String newContent = DETALHE_PATTERN.matcher(content).replaceAll(
                match -> {
                    String detalheContent = match
                        .group(1)
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&amp;", "&");
                    Map<String, List<String>> votes = extractVotes(
                        detalheContent
                    );
                    return buildVotingsXml(votes);
                }
            );

            Files.writeString(path, newContent);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to process: " + path + ": " + e.getMessage(),
                e
            );
        }
    }

    private String buildVotingsXml(Map<String, List<String>> votes) {
        StringBuilder sb = new StringBuilder("<votings>");

        // Parliamentary groups (single-word names)
        sb.append("<parliamentaryGroup>");
        for (String category : CATEGORIES) {
            String elementName = elementNameForCategory(category);
            for (String vote : votes.getOrDefault(category, List.of())) {
                if (!vote.contains(" ")) {
                    // Check if it has a '-' and if so, split it into two parts and check if left side is a number
                    // If it is a number, don't create the element
                    String[] parts = vote.split("-");
                    if (parts.length == 2 && parts[0].matches("\\d+")) {
                        continue;
                    }
                    sb
                        .append("<")
                        .append(elementName)
                        .append(">")
                        .append(vote)
                        .append("</")
                        .append(elementName)
                        .append(">");
                }
            }
        }
        sb.append("</parliamentaryGroup>");

        // Parliamentarians (multi-word names)
        sb.append("<parliamentarian>");
        for (String category : CATEGORIES) {
            String elementName = elementNameForCategory(category);
            for (String vote : votes.getOrDefault(category, List.of())) {
                if (vote.contains(" ")) {
                    sb
                        .append("<")
                        .append(elementName)
                        .append(">")
                        .append(vote)
                        .append("</")
                        .append(elementName)
                        .append(">");
                }
            }
        }
        sb.append("</parliamentarian>");

        sb.append("</votings>");
        return sb.toString();
    }

    private String elementNameForCategory(String category) {
        return switch (category) {
            case "A Favor" -> "aFavor";
            case "Contra" -> "contra";
            case "Abstenção" -> "abstencao";
            default -> throw new IllegalArgumentException(
                "Unknown voting category: " + category
            );
        };
    }
}
