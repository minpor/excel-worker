package io.github.minpor.excel;

import java.util.List;

/** In-memory view of an .xlsx workbook (all sheets). */
public record XlsxWorkbook(List<XlsxSheet> sheets) {

    public XlsxWorkbook {
        sheets = List.copyOf(sheets);
    }
}
