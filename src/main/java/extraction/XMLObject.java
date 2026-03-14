package extraction;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.xml.sax.InputSource;
import java.io.IOException;

@Getter
@Setter
public class XMLObject {

    private Path defaultSavePath = Path.of("data/"); 
    private String fileName; // fileName must not include file extension

    private Element element;
    private String string;

    public XMLObject(Element element) {
        this.element = element;
        this.string = elementToString(element);
    }

    public XMLObject(String string) {
        this.element = toTreeElement(string);
        this.string = string;
    }

    public XMLObject(String string, boolean removeBom){
        this.string = removeBom ? removeBom(string) : string;
        this.element = toTreeElement(this.string);
    }

    private static Element toTreeElement(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlString)));
            return document.getDocumentElement();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing XML", e);
        }
    }

    public static void printElement(Element element) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(element);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            System.out.println(writer);
        } catch (Exception e) {
            System.err.println("Error printing element: " + e.getMessage());
        }
    }

    public static String removeBom(String xmlString) {
        if (xmlString == null || xmlString.length() < 3) {
            return xmlString;
        }
        if (xmlString.charAt(0) == '\uFEFF') {
            return xmlString.substring(1);
        }
        return xmlString;
    }

    public static String elementToString(Element el) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(el);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            return el.getTextContent();
        }
    }

    public void removeBom(){
        this.string = removeBom(this.string);
        this.element = toTreeElement(this.string);
    }

    public void printElement() {
        printElement(this.element);
    }

    public void printElementValues() {
        printElementValues(this.element);
    }

    private static void printElementValues(Element element) {
        if (element == null) {
            return;
        }
        String text = element.getTextContent();
        if (text != null) {
            String trimmed = text.trim();
            if (!trimmed.isEmpty()) {
                System.out.println(trimmed);
            }
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                printElementValues((Element) node);
            }
        }
    }

    public void saveToFile() throws IOException {
        this.saveToFile(defaultSavePath);
    }

    public void saveToFile(Path savePath) throws IOException {
        if (savePath == null) {
            throw new IllegalStateException("Cannot save XML: savePath is not set.");
        }

        String content = this.string != null ? this.string : elementToString();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Cannot save XML: XML content is empty.");
        }

        String resolvedFileName = (this.fileName == null || this.fileName.isBlank())
            ? UUID.randomUUID() + ".xml"
            : this.fileName + ".xml";

        if (Files.exists(savePath) && !Files.isDirectory(savePath)) {
            throw new IllegalArgumentException("savePath must be a directory path (for example, data/).");
        }

        Files.createDirectories(savePath);
        Path targetPath = savePath.resolve(resolvedFileName);
        Files.writeString(targetPath, content, StandardCharsets.UTF_8);
    }

    public String elementToString() {
        return elementToString(this.element);
    }

    public String getString() {
        if (this.string == null) {
            return null;
        }
        try {
            byte[] latin1Bytes = this.string.getBytes(StandardCharsets.ISO_8859_1);
            return new String(latin1Bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return this.string;
        }
    }

    
    public Element getTreeElement() {
        return this.element;
    }
    
    public String getText() {
        if (this.element == null) {
            return null;
        }
        String text = this.element.getTextContent();
        return text == null ? null : text.trim();
    }

    public Optional<String> getAttribute(String elementName){
        XMLObject object = findFirstElementByName(elementName);
        if (object == null) {
            return Optional.empty();
        }
        String text = object.getText();
        return text == null ? Optional.empty() : Optional.of(text.trim());
    }

    public XMLObject findFirstElementByName(String elementName) {
        try {
            NodeList nodeList = element.getElementsByTagName(elementName);
            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);
                if (node instanceof Element) {
                    return new XMLObject((Element) node);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("XML Parsing Error: " + e.getMessage());
            String elementStr = elementToString();
            if (elementStr != null) {
                System.err.println("First 100 characters of XML: " + (elementStr.length() > 100 ? elementStr.substring(0, 100) : elementStr));
            }
            return null;
        }
    }

    public List<XMLObject> findElementsByName(String elementName) {
        try {
            List<XMLObject> results = new ArrayList<>();
            NodeList nodeList = element.getElementsByTagName(elementName);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    results.add(new XMLObject((Element) node));
                }
            }
            return results;
        } catch (Exception e) {
            System.err.println("XML Parsing Error: " + e.getMessage());
            String elementStr = elementToString();
            if (elementStr != null) {
                System.err.println("First 100 characters of XML: " + (elementStr.length() > 100 ? elementStr.substring(0, 100) : elementStr));
            }
            return new ArrayList<>();
        }
    }
}
