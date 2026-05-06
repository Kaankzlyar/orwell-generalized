package preprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import config.Config;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import preprocessing.hooks.ExtractVoting;

class ExtractVotingTest {

    @TempDir
    Path tempDir;

    private Path originalDataDir;
    private Path resourceDir;

    @BeforeEach
    void setUp() throws Exception {
        originalDataDir = Config.DATA_DIR;

        Path dataDir = tempDir.resolve("data");
        resourceDir = dataDir.resolve("ar").resolve("iniciativas");
        Files.createDirectories(resourceDir);

        Config.DATA_DIR = dataDir;
    }

    @AfterEach
    void tearDown() {
        Config.DATA_DIR = originalDataDir;
    }

    @Test
    void extractVotesGroupsNamesByVotingCategory() {
        ExtractVoting hook = new ExtractVoting();

        Map<String, List<String>> votes = hook.extractVotes(
            "A Favor: <I>PS</I>, <I>PSD</I> " +
            "Contra: <I>CH</I> " +
            "Abstenção: <I>IL</I>, <I>Joana Mortágua (BE)</I>"
        );

        assertEquals(List.of("PS", "PSD"), votes.get("A Favor"));
        assertEquals(List.of("CH"), votes.get("Contra"));
        assertEquals(List.of("IL", "Joana Mortágua"), votes.get("Abstenção"));
    }

    @Test
    void executeReplacesDetalheWithGeneratedVotingXml() throws Exception {
        Path document = resourceDir.resolve("voting.xml");
        Files.writeString(document, """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
                <detalhe>A Favor: &lt;I&gt;PS&lt;/I&gt;, &lt;I&gt;Ana Catarina Mendes (PS)&lt;/I&gt; Contra: &lt;I&gt;CH&lt;/I&gt; Abstenção: &lt;I&gt;IL&lt;/I&gt;</detalhe>
            </root>
            """);

        ExtractVoting hook = new ExtractVoting();

        hook.execute(new ProcessingContext());

        String content = Files.readString(document);

        assertTrue(content.contains("<votings>"));
        assertTrue(content.contains("<parliamentaryGroup>"));
        assertTrue(content.contains("<aFavor>PS</aFavor>"));
        assertTrue(content.contains("<contra>CH</contra>"));
        assertTrue(content.contains("<abstencao>IL</abstencao>"));
        assertTrue(content.contains("<parliamentarian>"));
        assertTrue(content.contains("<aFavor>Ana Catarina Mendes</aFavor>"));
    }

    @Test
    void executeSkipsSingleWordVoteCounts() throws Exception {
        Path document = resourceDir.resolve("vote-count.xml");
        Files.writeString(document, """
            <?xml version="1.0" encoding="UTF-8"?>
            <root>
                <detalhe>Contra: &lt;I&gt;5-PS&lt;/I&gt;, &lt;I&gt;CH&lt;/I&gt;</detalhe>
            </root>
            """);

        ExtractVoting hook = new ExtractVoting();

        hook.execute(new ProcessingContext());

        String content = Files.readString(document);

        assertTrue(content.contains("<contra>CH</contra>"));
        assertFalse(content.contains("<contra>5-PS</contra>"));
    }
}
