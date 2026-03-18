package preprocessing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import preprocessing.hooks.ParliamentarianReconciliation;
import config.Config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParliamentarianReconciliationTest {

    @TempDir
    Path tempDir;

    private Path originalDataDir;

    @BeforeEach
    void setUp() throws Exception {
        originalDataDir = Config.DATA_DIR;
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        Path resourceDir = dataDir.resolve("informacaobase");
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
        
        Config.DATA_DIR = dataDir;
    }

    @AfterEach
    void tearDown() {
        Config.DATA_DIR = originalDataDir;
    }

    @Test
    void hookHasCorrectName() {
        ParliamentarianReconciliation hook = new ParliamentarianReconciliation();
        assertEquals("ParliamentarianReconciliation", hook.getName());
    }

    @Test
    void executePopulatesLookupTable() {
        ParliamentarianReconciliation hook = new ParliamentarianReconciliation();
        ProcessingContext context = new ProcessingContext();
        
        hook.execute(context);
        
        Map<String, Map<String, String>> lookupTable = context.getLookupTable();
        assertFalse(lookupTable.isEmpty());
        assertTrue(lookupTable.containsKey("ParliamentarianReconciliation"));
    }

    @Test
    void executeRegistersDeputiesWithCorrectFormat() {
        ParliamentarianReconciliation hook = new ParliamentarianReconciliation();
        ProcessingContext context = new ProcessingContext();
        
        hook.execute(context);
        
        Map<String, String> hookTable = context.getLookupTable("ParliamentarianReconciliation").orElseThrow();
        
        assertEquals("9008", hookTable.get("XVII:john doe"));
        assertEquals("9009", hookTable.get("XVII:jane smith"));
    }

    @Test
    void executeHandlesMultipleDocuments() throws Exception {
        Path resourceDir = Config.DATA_DIR.resolve("informacaobase");
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
        
        ParliamentarianReconciliation hook = new ParliamentarianReconciliation();
        ProcessingContext context = new ProcessingContext();
        
        hook.execute(context);
        
        Map<String, String> hookTable = context.getLookupTable("ParliamentarianReconciliation").orElseThrow();
        
        assertEquals("9008", hookTable.get("XVII:john doe"));
        assertEquals("1234", hookTable.get("XVI:test user"));
    }

    @Test
    void executeTrimsWhitespace() {
        ParliamentarianReconciliation hook = new ParliamentarianReconciliation();
        ProcessingContext context = new ProcessingContext();
        
        hook.execute(context);
        
        Map<String, String> hookTable = context.getLookupTable("ParliamentarianReconciliation").orElseThrow();
        
        assertTrue(hookTable.containsKey("XVII:john doe"));
        assertFalse(hookTable.containsKey("XVII: john doe "));
    }

    @Test
    void executeHandlesEmptyDocument() throws Exception {
        Path resourceDir = Config.DATA_DIR.resolve("informacaobase");
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Legislatura>
                <DetalheLegislatura>
                    <sigla>EMPTY</sigla>
                </DetalheLegislatura>
            </Legislatura>
            """;
        Files.writeString(resourceDir.resolve("EMPTY.xml"), xmlContent);
        
        ParliamentarianReconciliation hook = new ParliamentarianReconciliation();
        ProcessingContext context = new ProcessingContext();
        
        assertDoesNotThrow(() -> hook.execute(context));
    }
}
