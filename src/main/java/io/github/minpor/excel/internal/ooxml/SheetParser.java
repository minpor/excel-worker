package io.github.minpor.excel.internal.ooxml;

import io.github.minpor.excel.CellValue;
import io.github.minpor.excel.XlsxCell;
import io.github.minpor.excel.XlsxRow;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Parses {@code xl/worksheets/sheet*.xml} {@code sheetData} into rows.
 */
final class SheetParser {

    private SheetParser() {}

    static List<XlsxRow> parse(InputStream in, List<String> sharedStrings) throws XMLStreamException {
        XMLInputFactory f = XMLInputFactory.newDefaultFactory();
        f.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        XMLStreamReader r = f.createXMLStreamReader(in);

        TreeMap<Integer, TreeMap<Integer, XlsxCell>> rowMap = new TreeMap<>();
        Integer currentRowIndex = null;
        int lastRowIndex = -1;
        int lastColInRow = -1;

        while (r.hasNext()) {
            int ev = r.next();
            if (ev != XMLStreamReader.START_ELEMENT) {
                continue;
            }
            if ("row".equals(r.getLocalName())) {
                String rr = XmlNames.attr(r, "r");
                if (rr != null && !rr.isEmpty()) {
                    currentRowIndex = Integer.parseInt(rr) - 1;
                } else if (lastRowIndex < 0) {
                    currentRowIndex = 0;
                } else {
                    currentRowIndex = lastRowIndex + 1;
                }
                lastRowIndex = currentRowIndex;
                lastColInRow = -1;
            } else if ("c".equals(r.getLocalName()) && currentRowIndex != null) {
                String ref = XmlNames.attr(r, "r");
                String t = XmlNames.attr(r, "t");
                int colIndex;
                if (ref != null && !ref.isEmpty()) {
                    int[] rc = CellReference.parseRowCol(ref);
                    colIndex = rc[1];
                    lastColInRow = colIndex;
                } else {
                    lastColInRow++;
                    colIndex = lastColInRow;
                }
                CellValue value = readCellValue(r, t, sharedStrings);
                rowMap
                        .computeIfAbsent(currentRowIndex, k -> new TreeMap<>())
                        .put(colIndex, new XlsxCell(colIndex, value));
            }
        }

        List<XlsxRow> rows = new ArrayList<>();
        for (var e : rowMap.entrySet()) {
            rows.add(new XlsxRow(e.getKey(), e.getValue()));
        }
        return rows;
    }

    private static CellValue readCellValue(XMLStreamReader r, String cellType, List<String> sharedStrings)
            throws XMLStreamException {
        String vText = null;
        String inlineText = null;
        while (r.hasNext()) {
            int ev = r.next();
            if (ev == XMLStreamReader.END_ELEMENT && "c".equals(r.getLocalName())) {
                break;
            }
            if (ev != XMLStreamReader.START_ELEMENT) {
                continue;
            }
            String ln = r.getLocalName();
            if ("v".equals(ln)) {
                vText = readTextUntilEnd(r);
            } else if ("is".equals(ln)) {
                inlineText = readInlineString(r);
            } else {
                skipSubtree(r);
            }
        }

        if (inlineText != null) {
            return new CellValue.Text(inlineText);
        }
        if (vText == null || vText.isEmpty()) {
            return CellValue.EMPTY;
        }

        if ("s".equals(cellType)) {
            int idx = Integer.parseInt(vText);
            if (idx < 0 || idx >= sharedStrings.size()) {
                throw new XMLStreamException("Shared string index out of range: " + idx);
            }
            return new CellValue.Text(sharedStrings.get(idx));
        }
        if ("b".equals(cellType)) {
            return new CellValue.BooleanValue(!"0".equals(vText.trim()));
        }
        if ("str".equals(cellType)) {
            return new CellValue.Text(vText);
        }
        if ("e".equals(cellType)) {
            return new CellValue.Text("#ERROR:" + vText);
        }
        // default numeric (n) or absent
        double d = Double.parseDouble(vText);
        return new CellValue.NumberValue(d);
    }

    private static String readTextUntilEnd(XMLStreamReader r) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        int depth = 1;
        while (r.hasNext()) {
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

    private static String readInlineString(XMLStreamReader r) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        int depth = 1;
        while (r.hasNext()) {
            int ev = r.next();
            if (ev == XMLStreamReader.START_ELEMENT) {
                if ("t".equals(r.getLocalName())) {
                    sb.append(readTextUntilEnd(r));
                } else {
                    depth++;
                }
            } else if (ev == XMLStreamReader.END_ELEMENT) {
                if ("is".equals(r.getLocalName())) {
                    break;
                }
                depth--;
            }
        }
        return sb.toString();
    }

    private static void skipSubtree(XMLStreamReader r) throws XMLStreamException {
        int depth = 1;
        while (r.hasNext() && depth > 0) {
            int ev = r.next();
            if (ev == XMLStreamReader.START_ELEMENT) {
                depth++;
            } else if (ev == XMLStreamReader.END_ELEMENT) {
                depth--;
            }
        }
    }
}
