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
 * Reads {@code xl/workbook.xml} sheet list (name + relationship id).
 */
final class WorkbookParser {

    record SheetEntry(String name, String relationshipId) {}

    private WorkbookParser() {}

    static List<SheetEntry> parse(ZipFile zip) throws IOException, XMLStreamException {
        ZipEntry entry = zip.getEntry("xl/workbook.xml");
        if (entry == null) {
            throw new IOException("Missing xl/workbook.xml");
        }
        try (InputStream in = zip.getInputStream(entry)) {
            return parse(in);
        }
    }

    static List<SheetEntry> parse(InputStream in) throws XMLStreamException {
        XMLInputFactory f = XMLInputFactory.newDefaultFactory();
        f.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        XMLStreamReader r = f.createXMLStreamReader(in);
        List<SheetEntry> sheets = new ArrayList<>();
        while (r.hasNext()) {
            r.next();
            if (XmlNames.isStart(r, "sheet")) {
                String name = XmlNames.attr(r, "name");
                String rid = XmlNames.relationshipId(r);
                if (name == null || rid == null) {
                    throw new XMLStreamException("sheet without name or r:id");
                }
                sheets.add(new SheetEntry(name, rid));
            }
        }
        return sheets;
    }
}
