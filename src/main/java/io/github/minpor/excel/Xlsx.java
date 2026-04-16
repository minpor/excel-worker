package io.github.minpor.excel;

import io.github.minpor.excel.internal.ooxml.XlsxLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Entry point for reading .xlsx (OOXML) workbooks without reflection, using only the JDK
 * ({@link java.util.zip.ZipFile} and StAX).
 */
public final class Xlsx {

    private Xlsx() {}

    /**
     * Reads the workbook from a path (uses {@link java.util.zip.ZipFile}).
     *
     * @param path path to the .xlsx file
     * @return parsed workbook
     * @throws IOException if the file cannot be read or is not a valid package
     */
    public static XlsxWorkbook read(Path path) throws IOException {
        return XlsxLoader.load(path);
    }

    /**
     * Reads from in-memory bytes by copying to a temporary file (required for random ZIP access).
     *
     * @param xlsxBytes full .xlsx contents
     */
    public static XlsxWorkbook read(byte[] xlsxBytes) throws IOException {
        Path tmp = Files.createTempFile("excel-worker-", ".xlsx");
        try {
            Files.write(tmp, xlsxBytes);
            return read(tmp);
        } finally {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
                tmp.toFile().deleteOnExit();
            }
        }
    }

    /**
     * Same as {@link #read(byte[])} but wraps {@link IOException} in {@link UncheckedIOException}.
     */
    public static XlsxWorkbook readUnchecked(byte[] xlsxBytes) {
        try {
            return read(xlsxBytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
