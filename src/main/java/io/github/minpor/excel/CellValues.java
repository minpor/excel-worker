package io.github.minpor.excel;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Strict typed extraction from {@link CellValue}: only the matching variant yields a value; coercion
 * (e.g. number to text) is not performed.
 */
public final class CellValues {

    private CellValues() {}

    /** Present only for {@link CellValue.Text}. */
    public static Optional<String> asText(CellValue value) {
        return value instanceof CellValue.Text t ? Optional.of(t.value()) : Optional.empty();
    }

    /** Present only for {@link CellValue.NumberValue}. */
    public static OptionalDouble asNumber(CellValue value) {
        return value instanceof CellValue.NumberValue n
                ? OptionalDouble.of(n.value())
                : OptionalDouble.empty();
    }

    /** Present only for {@link CellValue.BooleanValue}. */
    public static Optional<Boolean> asBoolean(CellValue value) {
        return value instanceof CellValue.BooleanValue b ? Optional.of(b.value()) : Optional.empty();
    }
}
