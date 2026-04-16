package io.github.minpor.excel;

import org.junit.jupiter.api.Test;

/**
 * Single fast check used when running {@code mvn -Pnative test} (GraalVM Native Image + JUnit Platform).
 * Full suite is also executed; this documents intent for native compatibility.
 */
class GraalVmNativeSmokeTest {

    @Test
    void readMinimalWorkbook() throws Exception {
        Xlsx.read(MinimalXlsxZip.twoSheetsMixedTypes());
    }
}
