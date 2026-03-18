package preprocessing.hooks;

import preprocessing.Hook;
import preprocessing.ProcessingContext;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class ParliamentarianReconciliation extends Hook {
    @Override
    public void execute(ProcessingContext context) {
        try (Stream<Path> paths = streamDocuments("informacaobase")) {
            paths.forEach(path -> processDocument(context, path));
        }
    }

    @Override
    public String getName() {
        return "ParliamentarianReconciliation";
    }

    private void processDocument(ProcessingContext context, Path xmlPath) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
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
                    if ("DetalheLegislatura".equals(name)) {
                        inDetalheLegislatura = true;
                        continue;
                    }
                    if (inDetalheLegislatura && "sigla".equals(name) && legislature == null) {
                        legislature = readElementText(reader);
                        continue;
                    }
                    if ("DadosDeputadoOrgaoPlenario".equals(name)) {
                        inDeputado = true;
                        depId = null;
                        depName = null;
                        continue;
                    }
                    if (inDeputado && "DepCadId".equals(name)) {
                        depId = readElementText(reader);
                        continue;
                    }
                    if (inDeputado && "DepNomeParlamentar".equals(name)) {
                        depName = readElementText(reader);
                    }
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    String name = reader.getLocalName();
                    if ("DetalheLegislatura".equals(name)) {
                        inDetalheLegislatura = false;
                        continue;
                    }
                    if ("DadosDeputadoOrgaoPlenario".equals(name)) {
                        if (legislature != null && depId != null && depName != null) {
                            String key = legislature + ":" + depName.trim().toLowerCase();
                            registerLookupTable(context, key, depId.trim());
                        }
                        inDeputado = false;
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse XML: " + xmlPath + ": " + e.getMessage(), e);
        }
    }

    private static String readElementText(XMLStreamReader reader) {
        try {
            String text = reader.getElementText();
            return text == null ? null : text.trim();
        } catch (Exception e) {
            return null;
        }
    }
}
