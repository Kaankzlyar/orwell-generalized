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
        Path resourceDir = dataDir.resolve("ar").resolve("iniciativas");
        Files.createDirectories(resourceDir);

        Files.writeString(resourceDir.resolve("first.xml"), """
            <?xml version="1.0" encoding="UTF-8"?>
            <ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
                <Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
                    <IniLeg>XVII</IniLeg>
                    <IniEventos>
                        <Pt_gov_ar_objectos_iniciativas_EventosOut>
                            <Comissao>
                                <Pt_gov_ar_objectos_iniciativas_ComissoesIniOut>
                                    <IdComissao>8454</IdComissao>
                                    <Nome> Comissão de Educação e Ciência </Nome>
                                </Pt_gov_ar_objectos_iniciativas_ComissoesIniOut>
                            </Comissao>
                        </Pt_gov_ar_objectos_iniciativas_EventosOut>
                    </IniEventos>
                </Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
            </ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
            """);

        Files.writeString(resourceDir.resolve("second.xml"), """
            <?xml version="1.0" encoding="UTF-8"?>
            <ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
                <Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
                    <IniLeg>XVII</IniLeg>
                    <IniEventos>
                        <Pt_gov_ar_objectos_iniciativas_EventosOut>
                            <Comissao>
                                <Pt_gov_ar_objectos_iniciativas_ComissoesIniOut>
                                    <IdComissao>8455</IdComissao>
                                    <Nome>Comissão de Saúde</Nome>
                                </Pt_gov_ar_objectos_iniciativas_ComissoesIniOut>
                            </Comissao>
                        </Pt_gov_ar_objectos_iniciativas_EventosOut>
                    </IniEventos>
                </Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
            </ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
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

        assertEquals("8454", hookTable.get("Comissão de Educação e Ciência:XVII"));
        assertEquals("8455", hookTable.get("Comissão de Saúde:XVII"));
    }

    @Test
    void executeTrimsCommissionNameWhitespace() throws Exception {
        Path resourceDir = Config.DATA_DIR.resolve("ar").resolve("iniciativas");
        Files.writeString(resourceDir.resolve("trim.xml"), """
            <?xml version="1.0" encoding="UTF-8"?>
            <ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
                <Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
                    <IniLeg>XVII</IniLeg>
                    <IniEventos>
                        <Pt_gov_ar_objectos_iniciativas_EventosOut>
                            <Comissao>
                                <Pt_gov_ar_objectos_iniciativas_ComissoesIniOut>
                                    <IdComissao>9000</IdComissao>
                                    <Nome>   Comissão de Orçamento, Finanças e Administração Pública   </Nome>
                                </Pt_gov_ar_objectos_iniciativas_ComissoesIniOut>
                            </Comissao>
                        </Pt_gov_ar_objectos_iniciativas_EventosOut>
                    </IniEventos>
                </Pt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
            </ArrayOfPt_gov_ar_objectos_iniciativas_DetalhePesquisaIniciativasOut>
            """);

        CommissionInformation hook = new CommissionInformation();
        ProcessingContext context = new ProcessingContext();

        hook.execute(context);

        Map<String, String> hookTable = context.getLookupTable("CommissionInformation").orElseThrow();

        assertEquals("9000", hookTable.get("Comissão de Orçamento, Finanças e Administração Pública:XVII"));
        assertFalse(hookTable.containsKey("   Comissão de Orçamento, Finanças e Administração Pública   :XVII"));
    }
}