package io.github.minpor.excel.internal.ooxml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads {@code xl/sharedStrings.xml} into a flat string table (simple and rich text flattened).
 */
final class SharedStringsParser {

    private SharedStringsParser() {}

    static List<String> parseOptional(ZipFile zip) throws IOException, XMLStreamException {
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) {
            return List.of();
        }
        try (InputStream in = zip.getInputStream(entry)) {
            return parse(in);
        }
    }

    static List<String> parse(InputStream in) throws XMLStreamException {
        XMLInputFactory f = XMLInputFactory.newDefaultFactory();
        f.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        XMLStreamReader r = f.createXMLStreamReader(in);
        List<String> out = new ArrayList<>();
        while (r.hasNext()) {
            r.next();
            if (XmlNames.isStart(r, "si")) {
                out.add(readSi(r));
            }
        }
        return out;
    }

    private static String readSi(XMLStreamReader r) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        int depth = 1;
        while (r.hasNext()) {
            int ev = r.next();
            if (ev == XMLStreamReader.START_ELEMENT) {
                if ("t".equals(r.getLocalName())) {
                    sb.append(readPlainTextElement(r));
                } else {
                    depth++;
                }
            } else if (ev == XMLStreamReader.END_ELEMENT) {
                depth--;
                if (depth == 0) {
                    break;
                }
            }
        }
        return sb.toString();
    }

    private static String readPlainTextElement(XMLStreamReader r) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        int depth = 1;
        while (depth > 0 && r.hasNext()) {
            int ev = r.next();
            if (ev == XMLStreamReader.CHARACTERS || ev == XMLStreamReader.CDATA) {
                sb.append(r.getText());
            } else if (ev == XMLStreamReader.START_ELEMENT) {
                depth++;
            } else if (ev == XMLStreamReader.END_ELEMENT) {
                depth--;
                if (depth == 0) {
                    break;
                }
            }
        }
        return sb.toString();
    }
}
