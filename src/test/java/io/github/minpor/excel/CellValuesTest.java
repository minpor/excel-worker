package io.github.minpor.excel;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CellValuesTest {

    @Test
    void excelColumnsMatchesPublicApi() {
        assertEquals(0, ExcelColumns.columnIndex("A"));
        assertEquals(1, ExcelColumns.columnIndex("B"));
        assertEquals(25, ExcelColumns.columnIndex("Z"));
        assertEquals(26, ExcelColumns.columnIndex("AA"));
    }

    @Test
    void asTextOnlyForText() {
        assertEquals(Optional.of("x"), CellValues.asText(new CellValue.Text("x")));
        assertTrue(CellValues.asText(CellValue.EMPTY).isEmpty());
        assertTrue(CellValues.asText(new CellValue.NumberValue(1.0)).isEmpty());
        assertTrue(CellValues.asText(new CellValue.BooleanValue(true)).isEmpty());
    }

    @Test
    void asNumberOnlyForNumber() {
        assertTrue(CellValues.asNumber(new CellValue.NumberValue(3.14)).isPresent());
        assertEquals(3.14, CellValues.asNumber(new CellValue.NumberValue(3.14)).getAsDouble(), 1e-9);
        assertTrue(CellValues.asNumber(CellValue.EMPTY).isEmpty());
        assertTrue(CellValues.asNumber(new CellValue.Text("1")).isEmpty());
    }

    @Test
    void asBooleanOnlyForBoolean() {
        assertEquals(Optional.of(true), CellValues.asBoolean(new CellValue.BooleanValue(true)));
        assertTrue(CellValues.asBoolean(CellValue.EMPTY).isEmpty());
        assertTrue(CellValues.asBoolean(new CellValue.Text("true")).isEmpty());
    }
}
