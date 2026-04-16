package io.github.minpor.excel;

import java.util.List;
import java.util.Optional;

/** In-memory view of an .xlsx workbook (all sheets). */
public record XlsxWorkbook(List<XlsxSheet> sheets) {

    public XlsxWorkbook {
        sheets = List.copyOf(sheets);
    }

    /** First sheet with the given name, if any. */
    public Optional<XlsxSheet> sheet(String name) {
        for (XlsxSheet s : sheets) {
            if (s.name().equals(name)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }
}
