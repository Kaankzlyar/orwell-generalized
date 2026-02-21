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
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.InputSource;

public class XMLObject {
    private final Element element;
    private final String string;

    public XMLObject(Element element, String string) {
        this.element = element;
        this.string = string;
    }

    public XMLObject(Element element) {
        this.element = element;
        this.string = elementToString(element);
    }

    public XMLObject(String string) {
        this.element = toTreeElement(string);
        this.string = string;
    }

    private static Element toTreeElement(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlString)));
            return document.getDocumentElement();
        } catch (Exception e) {
            System.err.println("Error parsing XML: " + e.getMessage());
            return null;
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
        return xmlString.substring(3);
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

    public void printElement() {
        printElement(this.element);
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
