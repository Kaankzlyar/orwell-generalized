package preprocessing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import config.Config;
import preprocessing.hooks.CommissionInformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CommissionInformationTest {

    @TempDir
    Path tempDir;

    private Path originalDataDir;

    @BeforeEach
    void setUp() throws Exception {
        originalDataDir = Config.DATA_DIR;

        Path dataDir = tempDir.resolve("data");
        Path resourceDir = dataDir.resolve("ar").resolve("composicaodeorgaos");
        Files.createDirectories(resourceDir);

        Files.writeString(resourceDir.resolve("first.xml"), """
            <?xml version="1.0" encoding="UTF-8"?>
            <OrganizacaoAR>
                <siglaLegislatura>XVII</siglaLegislatura>
                <Comissoes>
                    <OrgaoBase>
                        <DetalheOrgao>
                            <idOrgao>8454</idOrgao>
                            <nomeSigla>Comissão de Educação e Ciência</nomeSigla>
                        </DetalheOrgao>
                    </OrgaoBase>
                </Comissoes>
            </OrganizacaoAR>
            """);

        Files.writeString(resourceDir.resolve("second.xml"), """
            <?xml version="1.0" encoding="UTF-8"?>
            <OrganizacaoAR>
                <siglaLegislatura>XVII</siglaLegislatura>
                <Comissoes>
                    <OrgaoBase>
                        <DetalheOrgao>
                            <idOrgao>8455</idOrgao>
                            <nomeSigla>Comissão de Saúde</nomeSigla>
                        </DetalheOrgao>
                    </OrgaoBase>
                </Comissoes>
            </OrganizacaoAR>
            """);

        Config.DATA_DIR = dataDir;
    }

    @AfterEach
    void tearDown() {
        Config.DATA_DIR = originalDataDir;
    }

    @Test
    void hookHasCorrectName() {
        CommissionInformation hook = new CommissionInformation();

        assertEquals("CommissionInformation", hook.getName());
    }

    @Test
    void executeRegistersCommissionLookupEntries() {
        CommissionInformation hook = new CommissionInformation();
        ProcessingContext context = new ProcessingContext();

        hook.execute(context);

        Map<String, String> hookTable = context.getLookupTable("CommissionInformation").orElseThrow();

        assertEquals("8454", hookTable.get("comissao-de-educacao-e-ciencia:xvii"));
        assertEquals("8455", hookTable.get("comissao-de-saude:xvii"));
    }

    @Test
    void executeTrimsCommissionNameWhitespace() throws Exception {
        Path resourceDir = Config.DATA_DIR.resolve("ar").resolve("composicaodeorgaos");
        Files.writeString(resourceDir.resolve("trim.xml"), """
            <?xml version="1.0" encoding="UTF-8"?>
            <OrganizacaoAR>
                <siglaLegislatura>XVII</siglaLegislatura>
                <Comissoes>
                    <OrgaoBase>
                        <DetalheOrgao>
                            <idOrgao>9000</idOrgao>
                            <nomeSigla>   Comissão de Orçamento, Finanças e Administração Pública   </nomeSigla>
                        </DetalheOrgao>
                    </OrgaoBase>
                </Comissoes>
            </OrganizacaoAR>
            """);

        CommissionInformation hook = new CommissionInformation();
        ProcessingContext context = new ProcessingContext();

        hook.execute(context);

        Map<String, String> hookTable = context.getLookupTable("CommissionInformation").orElseThrow();

        assertEquals("9000", hookTable.get("comissao-de-orcamento-financas-e-administracao-publica:xvii"));
        assertFalse(hookTable.containsKey("comissao-de-orcamento-financas-e-administracao-publica   :xvii"));
    }

    @Test
    void executeRegistersMultipleCommissionsFromSingleFile() throws Exception {
        Path resourceDir = Config.DATA_DIR.resolve("ar").resolve("composicaodeorgaos");
        Files.writeString(resourceDir.resolve("multi.xml"), """
            <?xml version="1.0" encoding="UTF-8"?>
            <OrganizacaoAR>
                <siglaLegislatura>XVII</siglaLegislatura>
                <Comissoes>
                    <OrgaoBase>
                        <DetalheOrgao>
                            <idOrgao>100</idOrgao>
                            <nomeSigla>Comissão A</nomeSigla>
                        </DetalheOrgao>
                    </OrgaoBase>
                    <OrgaoBase>
                        <DetalheOrgao>
                            <idOrgao>200</idOrgao>
                            <nomeSigla>Comissão B</nomeSigla>
                        </DetalheOrgao>
                    </OrgaoBase>
                    <OrgaoBase>
                        <DetalheOrgao>
                            <idOrgao>300</idOrgao>
                            <nomeSigla>Comissão C</nomeSigla>
                        </DetalheOrgao>
                    </OrgaoBase>
                </Comissoes>
            </OrganizacaoAR>
            """);

        CommissionInformation hook = new CommissionInformation();
        ProcessingContext context = new ProcessingContext();

        hook.execute(context);

        Map<String, String> hookTable = context.getLookupTable("CommissionInformation").orElseThrow();

        assertEquals("100", hookTable.get("comissao-a:xvii"));
        assertEquals("200", hookTable.get("comissao-b:xvii"));
        assertEquals("300", hookTable.get("comissao-c:xvii"));
    }
}