package preprocessing.hooks;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import preprocessing.Hook;
import preprocessing.ProcessingContext;
import utils.NormalizeString;

// This class was created because the "atividadedeputado" dataset does not contain the commission ID, but only the name and legislature.
public class CommissionInformation extends Hook {
    @Override
    public void execute(ProcessingContext context) {
        try (Stream<Path> paths = streamDocuments("ar/iniciativas")) {
            paths.forEach(path -> processDocument(context, path));
        }
    }

    @Override
    public String getName() {
        return "CommissionInformation";
    }

    // Maps a commission name and legislature to its unique ID
    private void processDocument(ProcessingContext context, Path xmlPath) {
        XMLInputFactory factory = XMLInputFactory.newInstance();

        try (InputStream in = Files.newInputStream(xmlPath)) {
            XMLStreamReader reader = factory.createXMLStreamReader(in);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

            boolean inComissao = false;
            String legislature = null;
            String comissaoId = null;
            String comissaoNome = null;

            while(reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamReader.START_ELEMENT) {
                    String name = reader.getLocalName();
                    if (name.equals("Pt_gov_ar_objectos_iniciativas_ComissoesIniOut")) {
                        inComissao = true;
                        comissaoId = null;
                        comissaoNome = null;
                        continue;
                    }
                    if (inComissao && name.equals("IdComissao")) {
                        comissaoId = readElementText(reader);
                        continue;
                    }
                    if (inComissao && name.equals("Nome")) {
                        comissaoNome = readElementText(reader);
                        continue;
                    }
                    if (name.equals("IniLeg") && legislature == null) {
                        legislature = readElementText(reader);
                        continue;
                    }
                } else if (event == XMLStreamReader.END_ELEMENT) {
                    String name = reader.getLocalName();
                    if (name.equals("Pt_gov_ar_objectos_iniciativas_ComissoesIniOut")) {
                        inComissao = false;
                        if (comissaoId != null && comissaoNome != null && legislature != null) {
                            String key = NormalizeString.normalize(comissaoNome) + ":" + legislature;
                            registerLookupTable(context, key, comissaoId);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to parse XML: " + xmlPath + ": " + e.getMessage(), e);
        }
    }

}
