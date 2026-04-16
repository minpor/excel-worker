package io.github.minpor.excel;

import java.util.List;
import java.util.Optional;

/**
 * A worksheet with a stable name and rows in ascending row order (as present in the file).
 */
public record XlsxSheet(String name, List<XlsxRow> rows) {

    public XlsxSheet {
        rows = List.copyOf(rows);
    }

    /** First row in this sheet, if the sheet is not empty. */
    public Optional<XlsxRow> firstRow() {
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Row by position in {@link #rows()} (0-based list index), not Excel row number.
     */
    public Optional<XlsxRow> row(int listIndex) {
        if (listIndex < 0 || listIndex >= rows.size()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(listIndex));
    }
}
