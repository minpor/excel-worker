package io.github.minpor.excel.internal.ooxml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Parses OPC relationships (e.g. {@code xl/_rels/workbook.xml.rels}).
 */
final class RelationshipsParser {

    private RelationshipsParser() {}

    static Map<String, String> parseWorkbookRels(ZipFile zip) throws IOException, XMLStreamException {
        ZipEntry entry = zip.getEntry("xl/_rels/workbook.xml.rels");
        if (entry == null) {
            throw new IOException("Missing xl/_rels/workbook.xml.rels");
        }
        try (InputStream in = zip.getInputStream(entry)) {
            return parse(in);
        }
    }

    static Map<String, String> parse(InputStream in) throws XMLStreamException {
        XMLInputFactory f = XMLInputFactory.newDefaultFactory();
        XMLStreamReader r = f.createXMLStreamReader(in);
        Map<String, String> idToTarget = new HashMap<>();
        while (r.hasNext()) {
            r.next();
            if (XmlNames.isStart(r, "Relationship")) {
                String id = XmlNames.attr(r, "Id");
                String target = XmlNames.attr(r, "Target");
                if (id != null && target != null) {
                    idToTarget.put(id, target);
                }
            }
        }
        return idToTarget;
    }
}
