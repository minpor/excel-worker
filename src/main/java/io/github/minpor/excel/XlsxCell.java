package io.github.minpor.excel;

/**
 * One cell at {@code columnIndex} (0-based A=0) with a {@link CellValue}.
 */
public record XlsxCell(int columnIndex, CellValue value) {}
