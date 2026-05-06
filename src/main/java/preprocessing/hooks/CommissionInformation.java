package preprocessing.hooks;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import preprocessing.Hook;
import preprocessing.ProcessingContext;
import utils.StringUtils;

// This class was created because the "atividadedeputado" dataset does not contain the commission ID, but only the name and legislature.
public class CommissionInformation extends Hook {

    @Override
    public void execute(ProcessingContext context) {
        try (Stream<Path> paths = streamDocuments("ar/composicaodeorgaos")) {
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
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(
            XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
            false
        );

        try (InputStream in = Files.newInputStream(xmlPath)) {
            XMLStreamReader reader = factory.createXMLStreamReader(in);

            boolean inComissoes = false;
            String legislature = null;
            String comissaoId = null;
            String comissaoNome = null;

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamReader.START_ELEMENT) {
                    String name = reader.getLocalName();
                    if (name.equals("Comissoes")) {
                        inComissoes = true;
                        continue;
                    }
                    if (inComissoes && name.equals("OrgaoBase")) {
                        comissaoId = null;
                        comissaoNome = null;
                    }
                    if (inComissoes && name.equals("idOrgao")) {
                        comissaoId = readElementText(reader);
                        continue;
                    }
                    if (inComissoes && name.equals("nomeSigla")) {
                        comissaoNome = readElementText(reader);
                        continue;
                    }
                    if (
                        name.equals("siglaLegislatura") && legislature == null
                    ) {
                        legislature = readElementText(reader);
                        continue;
                    }
                } else if (event == XMLStreamReader.END_ELEMENT) {
                    String name = reader.getLocalName();
                    if (inComissoes && name.equals("OrgaoBase")) {
                        if (
                            comissaoId != null &&
                            comissaoNome != null &&
                            legislature != null
                        ) {
                            String normalizedName = StringUtils.normalize(
                                comissaoNome
                            );
                            String normalizedLegislature =
                                legislature.toLowerCase();
                            String key =
                                normalizedName + ":" + normalizedLegislature;
                            registerLookupTable(context, key, comissaoId);
                        }
                    }
                    if (name.equals("Comissoes")) {
                        inComissoes = false;
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to parse XML: " + xmlPath + ": " + e.getMessage(),
                e
            );
        }
    }
}
