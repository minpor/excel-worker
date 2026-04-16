package io.github.minpor.excel.internal.ooxml;

import io.github.minpor.excel.XlsxSheet;
import io.github.minpor.excel.XlsxWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLStreamException;

/** Orchestrates ZIP + OOXML parts into {@link XlsxWorkbook}. */
public final class XlsxLoader {

    private XlsxLoader() {}

    public static XlsxWorkbook load(java.nio.file.Path path) throws IOException {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            return load(zip);
        } catch (XMLStreamException e) {
            throw new IOException("Invalid OOXML", e);
        }
    }

    static XlsxWorkbook load(ZipFile zip) throws IOException, XMLStreamException {
        List<WorkbookParser.SheetEntry> entries = WorkbookParser.parse(zip);
        Map<String, String> rels = RelationshipsParser.parseWorkbookRels(zip);
        List<String> shared = SharedStringsParser.parseOptional(zip);

        List<XlsxSheet> sheets = new ArrayList<>();
        for (WorkbookParser.SheetEntry se : entries) {
            String target = rels.get(se.relationshipId());
            if (target == null) {
                throw new IOException("Missing relationship for sheet: " + se.relationshipId());
            }
            String entryName = normalizeWorksheetEntry(target);
            ZipEntry sheetEntry = zip.getEntry(entryName);
            if (sheetEntry == null) {
                throw new IOException("Missing worksheet part: " + entryName);
            }
            try (InputStream in = zip.getInputStream(sheetEntry)) {
                sheets.add(new XlsxSheet(se.name(), SheetParser.parse(in, shared)));
            }
        }
        return new XlsxWorkbook(sheets);
    }

    /**
     * Targets in workbook relationships are relative to {@code xl/}; some tools emit leading {@code /}
     * or backslashes.
     */
    static String normalizeWorksheetEntry(String target) {
        String t = target.replace('\\', '/');
        if (t.startsWith("/")) {
            t = t.substring(1);
        }
        if (t.startsWith("xl/")) {
            return t;
        }
        return "xl/" + t;
    }
}
