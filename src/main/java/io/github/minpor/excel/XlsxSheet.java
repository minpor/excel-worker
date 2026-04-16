package io.github.minpor.excel;

import java.util.Collections;
import java.util.List;

/**
 * A worksheet with a stable name and rows in ascending row order (as present in the file).
 */
public record XlsxSheet(String name, List<XlsxRow> rows) {

    public XlsxSheet {
        rows = List.copyOf(rows);
    }
}
