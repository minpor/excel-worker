package io.github.minpor.excel.internal.ooxml;

/**
 * Parses A1-style references into 0-based row and column indices (A=0, row 1 -> index 0).
 */
public final class CellReference {

    private CellReference() {}

    public static int columnIndex(String letters) {
        int col = 0;
        for (int i = 0; i < letters.length(); i++) {
            char c = letters.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("Invalid column in reference: " + letters);
            }
            col = col * 26 + (c - 'A' + 1);
        }
        return col - 1;
    }

    /**
     * @param ref cell reference e.g. {@code B3}
     * @return {@code [rowIndex0, columnIndex0]}
     */
    public static int[] parseRowCol(String ref) {
        int i = 0;
        while (i < ref.length() && Character.isLetter(ref.charAt(i))) {
            i++;
        }
        if (i == 0 || i == ref.length()) {
            throw new IllegalArgumentException("Invalid cell reference: " + ref);
        }
        String colPart = ref.substring(0, i);
        String rowPart = ref.substring(i);
        int row1 = Integer.parseInt(rowPart);
        return new int[] {row1 - 1, columnIndex(colPart)};
    }
}
