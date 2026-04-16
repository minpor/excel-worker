package io.github.minpor.excel;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * One logical row. Cells are keyed by 0-based column index (sparse).
 */
public record XlsxRow(int rowIndex, NavigableMap<Integer, XlsxCell> cellsByColumn) {

    public XlsxRow {
        cellsByColumn = Collections.unmodifiableNavigableMap(new TreeMap<>(cellsByColumn));
    }
}
