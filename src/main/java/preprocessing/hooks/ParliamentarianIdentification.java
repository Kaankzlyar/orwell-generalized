package preprocessing.hooks;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import preprocessing.Hook;
import preprocessing.ProcessingContext;
import utils.StringUtils;

public class ParliamentarianIdentification extends Hook {

    @Override
    public void execute(ProcessingContext context) {
        try (Stream<Path> paths = streamDocuments("ar/informacaobase")) {
            paths.forEach(path -> processDocument(context, path));
        }
    }

    @Override
    public String getName() {
        return "ParliamentarianIdentification";
    }

    private void processDocument(ProcessingContext context, Path xmlPath) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(
            XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
            false
        );
        String legislature = null;

        try (InputStream in = Files.newInputStream(xmlPath)) {
            XMLStreamReader reader = factory.createXMLStreamReader(in);

            boolean inDetalheLegislatura = false;
            boolean inDeputado = false;
            String depId = null;
            String depName = null;

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = reader.getLocalName();
                    if (name.equals("DetalheLegislatura")) {
                        inDetalheLegislatura = true;
                        continue;
                    }
                    if (
                        inDetalheLegislatura &&
                        name.equals("sigla") &&
                        legislature == null
                    ) {
                        legislature = readElementText(reader);
                        continue;
                    }
                    if (name.equals("DadosDeputadoOrgaoPlenario")) {
                        inDeputado = true;
                        depId = null;
                        depName = null;
                        continue;
                    }
                    if (inDeputado && name.equals("DepCadId")) {
                        depId = readElementText(reader);
                        continue;
                    }
                    if (inDeputado && name.equals("DepNomeParlamentar")) {
                        depName = readElementText(reader);
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String name = reader.getLocalName();
                    if (name.equals("DetalheLegislatura")) {
                        inDetalheLegislatura = false;
                        continue;
                    }
                    if (name.equals("DadosDeputadoOrgaoPlenario")) {
                        if (
                            legislature != null &&
                            depId != null &&
                            depName != null
                        ) {
                            String key =
                                legislature.toLowerCase() +
                                ":" +
                                StringUtils.normalize(depName);
                            registerLookupTable(context, key, depId.trim());
                        }
                        inDeputado = false;
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
