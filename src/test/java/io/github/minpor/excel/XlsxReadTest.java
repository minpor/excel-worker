package io.github.minpor.excel;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XlsxReadTest {

    @Test
    void readsTwoSheetsWithMixedTypesFromBytes() throws Exception {
        byte[] raw = MinimalXlsxZip.twoSheetsMixedTypes();
        XlsxWorkbook wb = Xlsx.read(raw);

        assertEquals(2, wb.sheets().size());
        assertEquals("First", wb.sheets().get(0).name());
        assertEquals("Second", wb.sheets().get(1).name());

        XlsxSheet first = wb.sheets().get(0);
        assertEquals(2, first.rows().size());

        XlsxRow r0 = first.rows().get(0);
        assertEquals(0, r0.rowIndex());
        assertInstanceOf(CellValue.Text.class, r0.cellValue(0));
        assertEquals("Hello", ((CellValue.Text) r0.cellValue(0)).value());
        assertInstanceOf(CellValue.NumberValue.class, r0.cellValue(1));
        assertEquals(42.5, ((CellValue.NumberValue) r0.cellValue(1)).value());

        XlsxRow r1 = first.rows().get(1);
        assertEquals(1, r1.rowIndex());
        assertInstanceOf(CellValue.BooleanValue.class, r1.cellValue(0));
        assertTrue(((CellValue.BooleanValue) r1.cellValue(0)).value());

        XlsxSheet second = wb.sheets().get(1);
        assertEquals(1, second.rows().size());
        assertEquals("World", ((CellValue.Text) second.rows().get(0).cellValue(0)).value());
    }

    @Test
    void convenienceApiSheetRowAndSparseColumn() throws Exception {
        byte[] raw = MinimalXlsxZip.twoSheetsMixedTypes();
        XlsxWorkbook wb = Xlsx.read(raw);

        assertTrue(wb.sheet("First").isPresent());
        assertTrue(wb.sheet("Nope").isEmpty());

        XlsxSheet first = wb.sheet("First").orElseThrow();
        assertTrue(first.firstRow().isPresent());
        assertTrue(first.row(0).isPresent());
        assertTrue(first.row(99).isEmpty());

        XlsxRow header = first.firstRow().orElseThrow();
        assertSame(CellValue.EMPTY, header.cellValue(2));

        XlsxRow secondDataRow = first.row(1).orElseThrow();
        assertSame(CellValue.EMPTY, secondDataRow.cellValue(1));
    }

    @Test
    void readsFromPath() throws Exception {
        byte[] raw = MinimalXlsxZip.twoSheetsMixedTypes();
        Path p = Files.createTempFile("excel-worker-test-", ".xlsx");
        try {
            Files.write(p, raw);
            XlsxWorkbook wb = Xlsx.read(p);
            assertEquals(2, wb.sheets().size());
        } finally {
            Files.deleteIfExists(p);
        }
    }

    @Test
    void numbersOnlyWithoutSharedStringsPart() throws Exception {
        XlsxWorkbook wb = Xlsx.read(MinimalXlsxZip.numbersOnlyNoSharedStrings());
        assertEquals(1, wb.sheets().size());
        CellValue v = wb.sheets().get(0).rows().get(0).cellValue(0);
        assertInstanceOf(CellValue.NumberValue.class, v);
        assertEquals(3.14, ((CellValue.NumberValue) v).value(), 1e-9);
    }

    @Test
    void inlineString() throws Exception {
        XlsxWorkbook wb = Xlsx.read(MinimalXlsxZip.inlineStringSheet());
        CellValue v = wb.sheets().get(0).rows().get(0).cellValue(0);
        assertInstanceOf(CellValue.Text.class, v);
        assertEquals("inline", ((CellValue.Text) v).value());
    }

}
