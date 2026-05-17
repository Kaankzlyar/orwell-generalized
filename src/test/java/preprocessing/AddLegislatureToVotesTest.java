package preprocessing;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import config.Config;
import preprocessing.hooks.AddLegislatureToVotes;
import preprocessing.hooks.LegislatureInformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddLegislatureToVotesTest {

    @TempDir
    Path tempDir;

    private Path originalDataDir;

    @BeforeEach
    void setUp() throws Exception {
        originalDataDir = Config.DATA_DIR;

        Path dataDir = tempDir.resolve("data");

        Path infoDir = dataDir.resolve("ar").resolve("informacaobase");
        Files.createDirectories(infoDir);
        Files.writeString(infoDir.resolve("legislatures.xml"),
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<Legislatura xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
            + "<DetalheLegislatura><sigla>XVI</sigla><dtini>2024-03-26</dtini><dtfim>2025-06-02</dtfim></DetalheLegislatura>"
            + "<DetalheLegislatura><sigla>XVII</sigla><dtini>2025-06-03</dtini></DetalheLegislatura>"
            + "</Legislatura>"
        );

        Path iniciativasDir = dataDir.resolve("ar").resolve("iniciativas");
        Files.createDirectories(iniciativasDir);
        Files.writeString(iniciativasDir.resolve("votes.xml"),
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
            + "<Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
            + "<IniId>123</IniId>"
            + "<Pt_gov_ar_objectos_iniciativas_EventosOut>"
            + "<Votacao><pt_gov_ar_objectos_VotacaoOut><id>1</id><resultado>Aprovado</resultado><data>2024-05-01</data></pt_gov_ar_objectos_VotacaoOut></Votacao>"
            + "<Votacao><pt_gov_ar_objectos_VotacaoOut><id>2</id><resultado>Rejeitado</resultado><data>2025-07-15</data></pt_gov_ar_objectos_VotacaoOut></Votacao>"
            + "</Pt_gov_ar_objectos_iniciativas_EventosOut>"
            + "</Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
            + "</ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
        );

        Config.DATA_DIR = dataDir;
    }

    @AfterEach
    void tearDown() {
        Config.DATA_DIR = originalDataDir;
    }

    @Test
    void executeAddsLegislaturaToVotes() throws Exception {
        LegislatureInformation legHook = new LegislatureInformation();
        ProcessingContext context = new ProcessingContext();
        legHook.execute(context);

        AddLegislatureToVotes voteHook = new AddLegislatureToVotes();
        voteHook.execute(context);

        String result = Files.readString(tempDir.resolve("data").resolve("ar").resolve("iniciativas").resolve("votes.xml"));

        assertTrue(result.contains("<Votacao><pt_gov_ar_objectos_VotacaoOut><id>1</id><resultado>Aprovado</resultado><data>2024-05-01</data><legislatura>XVI</legislatura></pt_gov_ar_objectos_VotacaoOut></Votacao>"),
            "First vote should have legislatura XVI");
        assertTrue(result.contains("<Votacao><pt_gov_ar_objectos_VotacaoOut><id>2</id><resultado>Rejeitado</resultado><data>2025-07-15</data><legislatura>XVII</legislatura></pt_gov_ar_objectos_VotacaoOut></Votacao>"),
            "Second vote should have legislatura XVII");
    }

    @Test
    void executeDoesNotAddLegislaturaForDateOutsideAnyLegislature() throws Exception {
        Path iniciativasDir = tempDir.resolve("data").resolve("ar").resolve("iniciativas");
        Files.writeString(iniciativasDir.resolve("outside.xml"),
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
            + "<Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
            + "<Pt_gov_ar_objectos_iniciativas_EventosOut>"
            + "<Votacao><pt_gov_ar_objectos_VotacaoOut><id>1</id><resultado>Aprovado</resultado><data>2022-01-01</data></pt_gov_ar_objectos_VotacaoOut></Votacao>"
            + "</Pt_gov_ar_objectos_iniciativas_EventosOut>"
            + "</Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
            + "</ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
        );

        LegislatureInformation legHook = new LegislatureInformation();
        ProcessingContext context = new ProcessingContext();
        legHook.execute(context);

        AddLegislatureToVotes voteHook = new AddLegislatureToVotes();
        voteHook.execute(context);

        String result = Files.readString(iniciativasDir.resolve("outside.xml"));
        assertTrue(result.contains("<data>2022-01-01</data>"), "Date should remain unchanged");
        assertTrue(!result.contains("<legislatura>"), "No legislatura tag should be added");
    }

    @Test
    void executeIsIdempotentWhenLegislaturaAlreadyPresent() throws Exception {
        Path iniciativasDir = tempDir.resolve("data").resolve("ar").resolve("iniciativas");
        Files.writeString(iniciativasDir.resolve("already.xml"),
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
            + "<Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
            + "<Pt_gov_ar_objectos_iniciativas_EventosOut>"
            + "<Votacao><pt_gov_ar_objectos_VotacaoOut><id>1</id><resultado>Aprovado</resultado><data>2024-05-01</data><legislatura>XVI</legislatura></pt_gov_ar_objectos_VotacaoOut></Votacao>"
            + "</Pt_gov_ar_objectos_iniciativas_EventosOut>"
            + "</Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
            + "</ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
        );

        LegislatureInformation legHook = new LegislatureInformation();
        ProcessingContext context = new ProcessingContext();
        legHook.execute(context);

        AddLegislatureToVotes voteHook = new AddLegislatureToVotes();
        voteHook.execute(context);

        String result = Files.readString(iniciativasDir.resolve("already.xml"));

        int count = result.split("<legislatura>").length - 1;
        assertEquals(1, count, "Should have exactly one legislatura tag (no duplicate)");
    }

    @Test
    void executeSkipsWhenLegislatureInformationIsEmpty() throws Exception {
        ProcessingContext context = new ProcessingContext();

        AddLegislatureToVotes voteHook = new AddLegislatureToVotes();
        voteHook.execute(context);

        String result = Files.readString(tempDir.resolve("data").resolve("ar").resolve("iniciativas").resolve("votes.xml"));
        assertTrue(result.contains("<data>2024-05-01</data>"), "File should remain unchanged");
        assertTrue(!result.contains("<legislatura>"), "No legislatura tag should be added");
    }

    @Test
    void executeHandlesMultipleVotesInSameFile() throws Exception {
        Path iniciativasDir = tempDir.resolve("data").resolve("ar").resolve("iniciativas");
        Files.writeString(iniciativasDir.resolve("multi.xml"),
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut"
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
            + "<Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
            + "<Pt_gov_ar_objectos_iniciativas_EventosOut>"
            + "<Votacao><pt_gov_ar_objectos_VotacaoOut><id>1</id><data>2024-03-26</data></pt_gov_ar_objectos_VotacaoOut></Votacao>"
            + "<Votacao><pt_gov_ar_objectos_VotacaoOut><id>2</id><data>2025-06-02</data></pt_gov_ar_objectos_VotacaoOut></Votacao>"
            + "<Votacao><pt_gov_ar_objectos_VotacaoOut><id>3</id><data>2025-06-03</data></pt_gov_ar_objectos_VotacaoOut></Votacao>"
            + "<Votacao><pt_gov_ar_objectos_VotacaoOut><id>4</id><data>2026-01-30</data></pt_gov_ar_objectos_VotacaoOut></Votacao>"
            + "</Pt_gov_ar_objectos_iniciativas_EventosOut>"
            + "</Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
            + "</ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>"
        );

        LegislatureInformation legHook = new LegislatureInformation();
        ProcessingContext context = new ProcessingContext();
        legHook.execute(context);

        AddLegislatureToVotes voteHook = new AddLegislatureToVotes();
        voteHook.execute(context);

        String result = Files.readString(iniciativasDir.resolve("multi.xml"));

        assertTrue(result.contains("<data>2024-03-26</data><legislatura>XVI</legislatura>"));
        assertTrue(result.contains("<data>2025-06-02</data><legislatura>XVI</legislatura>"));
        assertTrue(result.contains("<data>2025-06-03</data><legislatura>XVII</legislatura>"));
        assertTrue(result.contains("<data>2026-01-30</data><legislatura>XVII</legislatura>"));
    }
}
