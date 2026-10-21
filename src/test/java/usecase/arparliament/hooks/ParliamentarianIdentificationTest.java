package usecase.arparliament.hooks;

import static org.junit.jupiter.api.Assertions.*;

import core.preprocessing.ProcessingContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParliamentarianIdentificationTest {

    @TempDir
    Path tempDir;

    private Path dataDir;

    @BeforeEach
    void setUp() throws Exception {
        dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        Path resourceDir = dataDir.resolve("ar").resolve("informacaobase");
        Files.createDirectories(resourceDir);

        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Legislatura>
                <DetalheLegislatura>
                    <sigla>XVII</sigla>
                    <CirculosEleitorais>
                        <pt_ar_wsgode_objectos_DadosCirculoEleitoralList>
                            <cpDes>Porto</cpDes>
                        </pt_ar_wsgode_objectos_DadosCirculoEleitoralList>
                    </CirculosEleitorais>
                    <DadosDeputadoOrgaoPlenario>
                        <DepCadId>9008</DepCadId>
                        <DepNomeParlamentar>John Doe</DepNomeParlamentar>
                    </DadosDeputadoOrgaoPlenario>
                    <DadosDeputadoOrgaoPlenario>
                        <DepCadId>9009</DepCadId>
                        <DepNomeParlamentar>Jane Smith</DepNomeParlamentar>
                    </DadosDeputadoOrgaoPlenario>
                </DetalheLegislatura>
            </Legislatura>
            """;
        Files.writeString(resourceDir.resolve("XVII.xml"), xmlContent);
    }

    @Test
    void hookHasCorrectName() {
        ParliamentarianIdentification hook =
            new ParliamentarianIdentification();
        assertEquals("ParliamentarianIdentification", hook.getName());
    }

    @Test
    void executePopulatesLookupTable() {
        ParliamentarianIdentification hook =
            new ParliamentarianIdentification();
        ProcessingContext context = new ProcessingContext(dataDir, Set.of());

        hook.execute(context);

        Map<String, Map<String, String>> lookupTable = context.getLookupTable();
        assertFalse(lookupTable.isEmpty());
        assertTrue(lookupTable.containsKey("ParliamentarianIdentification"));
    }

    @Test
    void executeRegistersDeputiesWithCorrectFormat() {
        ParliamentarianIdentification hook =
            new ParliamentarianIdentification();
        ProcessingContext context = new ProcessingContext(dataDir, Set.of());

        hook.execute(context);

        Map<String, String> hookTable = context
            .getLookupTable("ParliamentarianIdentification")
            .orElseThrow();

        assertEquals("9008", hookTable.get("xvii:john-doe"));
        assertEquals("9009", hookTable.get("xvii:jane-smith"));
    }

    @Test
    void executeHandlesMultipleDocuments() throws Exception {
        Path resourceDir = dataDir.resolve("ar").resolve(
            "informacaobase"
        );
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Legislatura>
                <DetalheLegislatura>
                    <sigla>XVI</sigla>
                    <CirculosEleitorais>
                        <pt_ar_wsgode_objectos_DadosCirculoEleitoralList>
                            <cpDes>Lisboa</cpDes>
                        </pt_ar_wsgode_objectos_DadosCirculoEleitoralList>
                    </CirculosEleitorais>
                    <DadosDeputadoOrgaoPlenario>
                        <DepCadId>1234</DepCadId>
                        <DepNomeParlamentar>Test User</DepNomeParlamentar>
                    </DadosDeputadoOrgaoPlenario>
                </DetalheLegislatura>
            </Legislatura>
            """;
        Files.writeString(resourceDir.resolve("XVI.xml"), xmlContent);

        ParliamentarianIdentification hook =
            new ParliamentarianIdentification();
        ProcessingContext context = new ProcessingContext(dataDir, Set.of());

        hook.execute(context);

        Map<String, String> hookTable = context
            .getLookupTable("ParliamentarianIdentification")
            .orElseThrow();

        assertEquals("9008", hookTable.get("xvii:john-doe"));
        assertEquals("1234", hookTable.get("xvi:test-user"));
    }

    @Test
    void executeTrimsWhitespace() {
        ParliamentarianIdentification hook =
            new ParliamentarianIdentification();
        ProcessingContext context = new ProcessingContext(dataDir, Set.of());

        hook.execute(context);

        Map<String, String> hookTable = context
            .getLookupTable("ParliamentarianIdentification")
            .orElseThrow();

        assertTrue(hookTable.containsKey("xvii:john-doe"));
        assertFalse(hookTable.containsKey("XVII: john doe "));
    }

    @Test
    void executeHandlesEmptyDocument() throws Exception {
        Path resourceDir = dataDir.resolve("ar").resolve(
            "informacaobase"
        );
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Legislatura>
                <DetalheLegislatura>
                    <sigla>EMPTY</sigla>
                </DetalheLegislatura>
            </Legislatura>
            """;
        Files.writeString(resourceDir.resolve("EMPTY.xml"), xmlContent);

        ParliamentarianIdentification hook =
            new ParliamentarianIdentification();
        ProcessingContext context = new ProcessingContext(dataDir, Set.of());

        assertDoesNotThrow(() -> hook.execute(context));
    }
}
