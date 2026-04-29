package preprocessing.hooks;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import preprocessing.Hook;
import preprocessing.ProcessingContext;

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
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to parse XML: " + xmlPath + ": " + e.getMessage(), e);
        }
    }

}
