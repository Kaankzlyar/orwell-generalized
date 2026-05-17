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

public class LegislatureInformation extends Hook {

    @Override
    public void execute(ProcessingContext context) {
        try (Stream<Path> paths = streamDocuments("ar/informacaobase")) {
            paths.forEach(path -> processDocument(context, path));
        }
    }

    @Override
    public String getName() {
        return "LegislatureInformation";
    }

    // Maps a legislature name to its start and end date
    private void processDocument(ProcessingContext context, Path xmlPath) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(
            XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
            false
        );

        try (InputStream in = Files.newInputStream(xmlPath)) {
            XMLStreamReader reader = factory.createXMLStreamReader(in);

			boolean inDetalheLegislatura = false;
			String legislatureName = null;
			String startDate = null;
			String endDate = null;

			while (reader.hasNext()) {
				int event = reader.next();

				if (event == XMLStreamConstants.START_ELEMENT) {
					String name = reader.getLocalName();
					if (name.equals("DetalheLegislatura")) {
						inDetalheLegislatura = true;
						continue;
					}
					if (inDetalheLegislatura && name.equals("sigla") && legislatureName == null) {
						legislatureName = readElementText(reader);
						continue;
					}
					if (inDetalheLegislatura && name.equals("dtini") && startDate == null) {
						startDate = readElementText(reader);
						continue;
					}
					if (inDetalheLegislatura && name.equals("dtfim") && endDate == null) {
						endDate = readElementText(reader);
						continue;
					}
				} else if (event == XMLStreamConstants.END_ELEMENT) {
					String name = reader.getLocalName();
					if (name.equals("DetalheLegislatura")) {
						if (legislatureName != null && startDate != null) {
							String value = startDate + "|" + (endDate != null ? endDate : "");
							registerLookupTable(context, legislatureName.toLowerCase(), value);
						}
						inDetalheLegislatura = false;
						legislatureName = null;
						startDate = null;
						endDate = null;
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
