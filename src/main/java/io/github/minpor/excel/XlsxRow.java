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

    /**
     * Value at {@code columnIndex}, or {@link CellValue#EMPTY} if that column has no cell in this row
     * (sparse row).
     */
    public CellValue cellValue(int columnIndex) {
        XlsxCell c = cellsByColumn.get(columnIndex);
        return c == null ? CellValue.EMPTY : c.value();
    }
}
