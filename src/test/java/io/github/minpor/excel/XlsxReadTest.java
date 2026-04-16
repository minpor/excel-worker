package io.github.minpor.excel;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
        assertInstanceOf(CellValue.Text.class, r0.cellsByColumn().get(0).value());
        assertEquals("Hello", ((CellValue.Text) r0.cellsByColumn().get(0).value()).value());
        assertInstanceOf(CellValue.NumberValue.class, r0.cellsByColumn().get(1).value());
        assertEquals(42.5, ((CellValue.NumberValue) r0.cellsByColumn().get(1).value()).value());

        XlsxRow r1 = first.rows().get(1);
        assertEquals(1, r1.rowIndex());
        assertInstanceOf(CellValue.BooleanValue.class, r1.cellsByColumn().get(0).value());
        assertTrue(((CellValue.BooleanValue) r1.cellsByColumn().get(0).value()).value());

        XlsxSheet second = wb.sheets().get(1);
        assertEquals(1, second.rows().size());
        assertEquals("World", ((CellValue.Text) second.rows().get(0).cellsByColumn().get(0).value()).value());
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
        CellValue v = wb.sheets().get(0).rows().get(0).cellsByColumn().get(0).value();
        assertInstanceOf(CellValue.NumberValue.class, v);
        assertEquals(3.14, ((CellValue.NumberValue) v).value(), 1e-9);
    }

    @Test
    void inlineString() throws Exception {
        XlsxWorkbook wb = Xlsx.read(MinimalXlsxZip.inlineStringSheet());
        CellValue v = wb.sheets().get(0).rows().get(0).cellsByColumn().get(0).value();
        assertInstanceOf(CellValue.Text.class, v);
        assertEquals("inline", ((CellValue.Text) v).value());
    }

    @Test
    void cellReferenceColumnIndex() {
        assertEquals(0, io.github.minpor.excel.internal.ooxml.CellReference.columnIndex("A"));
        assertEquals(1, io.github.minpor.excel.internal.ooxml.CellReference.columnIndex("B"));
        assertEquals(25, io.github.minpor.excel.internal.ooxml.CellReference.columnIndex("Z"));
        assertEquals(26, io.github.minpor.excel.internal.ooxml.CellReference.columnIndex("AA"));
    }
}
