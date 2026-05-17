package preprocessing.hooks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import preprocessing.Hook;
import preprocessing.ProcessingContext;
import utils.LegislatureUtils;

public class AddLegislatureToVotes extends Hook {

    private static final Pattern VOTACAO_PATTERN = Pattern.compile(
        "<Votacao>(.*?)</Votacao>",
        Pattern.DOTALL
    );
    private static final Pattern DATA_PATTERN = Pattern.compile(
        "<data>(.*?)</data>"
    );

    @Override
    public String getName() {
        return "AddLegislatureToVotes";
    }

    @Override
    public void execute(ProcessingContext context) {
        Map<String, String> legislatureTable = context.getLookupTable("LegislatureInformation")
            .orElse(Map.of());

        if (legislatureTable.isEmpty()) {
            log("No legislature information available, skipping");
            return;
        }

        try (Stream<Path> paths = streamDocuments("ar/iniciativas")) {
            paths.forEach(path -> processDocument(path, legislatureTable));
        }
    }

    private void processDocument(Path path, Map<String, String> legislatureTable) {
        try {
            String content = Files.readString(path);
            boolean modified = false;

            StringBuffer sb = new StringBuffer();
            Matcher matcher = VOTACAO_PATTERN.matcher(content);

            while (matcher.find()) {
                String votacao = matcher.group(1);

                if (votacao.contains("<legislatura>")) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
                    continue;
                }

                Matcher dataMatcher = DATA_PATTERN.matcher(votacao);
                if (!dataMatcher.find()) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
                    continue;
                }

                String date = dataMatcher.group(1).trim();
                String legislature = LegislatureUtils.findLegislatureByDate(legislatureTable, date);
                if (legislature == null) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
                    continue;
                }

                String replacement = votacao.replace(
                    "</pt_gov_ar_objectos_VotacaoOut>",
                    "<legislatura>" + legislature.toUpperCase() + "</legislatura></pt_gov_ar_objectos_VotacaoOut>"
                );

                matcher.appendReplacement(sb, Matcher.quoteReplacement("<Votacao>" + replacement + "</Votacao>"));
                modified = true;
            }
            matcher.appendTail(sb);

            if (modified) {
                Files.writeString(path, sb.toString());
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to process: " + path + ": " + e.getMessage(),
                e
            );
        }
    }
}
