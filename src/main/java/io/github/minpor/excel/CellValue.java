package io.github.minpor.excel;

/**
 * Typed cell contents. Excel date/time values are often stored as serial numbers; those appear as
 * {@link NumberValue}.
 */
public sealed interface CellValue permits CellValue.Empty, CellValue.Text, CellValue.NumberValue, CellValue.BooleanValue {

    Empty EMPTY = new Empty();

    /** No value (empty cell). */
    record Empty() implements CellValue {}

    /** Text from shared strings or inline string. */
    record Text(String value) implements CellValue {}

    /** Numeric cell, including Excel date serials when stored as numbers. */
    record NumberValue(double value) implements CellValue {}

    record BooleanValue(boolean value) implements CellValue {}
}
