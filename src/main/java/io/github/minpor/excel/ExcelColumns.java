package io.github.minpor.excel;

import io.github.minpor.excel.internal.ooxml.CellReference;

/**
 * Excel-style column letters ({@code A}, {@code B}, …, {@code AA}, …) mapped to 0-based column indices
 * ({@code A} → 0), matching OOXML cell references.
 */
public final class ExcelColumns {

    private ExcelColumns() {}

    /**
     * @param letters upper-case column letters only ({@code A}-{@code Z})
     * @return 0-based column index
     */
    public static int columnIndex(String letters) {
        return CellReference.columnIndex(letters);
    }
}
