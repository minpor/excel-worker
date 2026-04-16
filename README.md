# excel-worker

A small **Java 21** library for reading **Excel OOXML (`.xlsx`)** workbooks. You can use it on **any standard JVM** (HotSpot, OpenJDK, Temurin, etc.)—that is the normal, expected deployment. The implementation is also **friendly to GraalVM Native Image**: parsing uses only the **JDK** (`ZipFile` + StAX), with **no reflection** in library code and **no dependency on Apache POI** (or other heavy stacks). Those choices keep native-image setup simple, but they are **not** a requirement—you do **not** need GraalVM or native compilation to use this library.

## Goals

- Read `.xlsx` packages and expose **all sheets**, **rows**, and **typed cell values** (empty, text, number, boolean).
- Stay **GraalVM-friendly**: straightforward code paths, no reflective frameworks.
- Remain **lightweight**: zero runtime dependencies beyond the JRE.

## Non-goals (current scope)

Writing workbooks, legacy `.xls`, full formula engine, rich text, merged cells, comments, and style-based date interpretation are out of scope for the first iterations. **Excel date/time values** are often stored as numeric serials; those surface as **`NumberValue`** until optional style-aware date handling is added.

## Requirements

- **Java 21** or newer.

## Maven

The project is not yet published to **Maven Central**. Tagged releases (for example **`v0.1.0`**) match the `<version>` in `pom.xml`; clone/checkout the tag and install locally, or depend on the built JAR from your own artifact repository.

```bash
mvn clean install
```

Then depend on (adjust version to match your `pom.xml`):

```xml
<dependency>
    <groupId>io.github.minpor</groupId>
    <artifactId>excel-worker</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Gradle (Kotlin DSL)

Use the same Maven coordinates as a normal JVM dependency (Kotlin/JVM compiles to the same bytecode as Java):

```kotlin
dependencies {
    implementation("io.github.minpor:excel-worker:0.1.0")
}
```

For **Kotlin Multiplatform**, this artifact is **JVM-only** for now; call it from a `jvmMain` source set that targets the JVM.

## Usage

### Java

```java
import io.github.minpor.excel.*;

import java.nio.file.Path;

Path file = Path.of("report.xlsx");
XlsxWorkbook wb = Xlsx.read(file);

for (XlsxSheet sheet : wb.sheets()) {
    System.out.println("Sheet: " + sheet.name());
    for (XlsxRow row : sheet.rows()) {
        for (var cell : row.cellsByColumn().values()) {
            switch (cell.value()) {
                case CellValue.Empty e -> { /* empty */ }
                case CellValue.Text t -> System.out.println(t.value());
                case CellValue.NumberValue n -> System.out.println(n.value());
                case CellValue.BooleanValue b -> System.out.println(b.value());
            }
        }
    }
}
```

### Kotlin (JVM)

The API is plain Java types, so you use the same `Xlsx` entry point and map `CellValue` with an exhaustive `when`:

```kotlin
import io.github.minpor.excel.CellValue
import io.github.minpor.excel.Xlsx
import java.nio.file.Path

fun readWorkbook(path: Path) = Xlsx.read(path)

fun main() {
    val wb = Xlsx.read(Path.of("report.xlsx"))
    for (sheet in wb.sheets()) {
        println("Sheet: ${sheet.name()}")
        for (row in sheet.rows()) {
            for (cell in row.cellsByColumn().values) {
                when (val v = cell.value()) {
                    is CellValue.Empty -> { /* skip */ }
                    is CellValue.Text -> println(v.value())
                    is CellValue.NumberValue -> println(v.value())
                    is CellValue.BooleanValue -> println(v.value())
                }
            }
        }
    }
}
```

Java records (`CellValue.Text`, etc.) expose components; from Kotlin you typically call `value()` on those types (or rely on Kotlin’s Java interop for accessors).

Reading bytes is the same as in Java: `Xlsx.read(byteArray)` (uses a temporary file internally for ZIP random access).

**Maven + Kotlin:** add `excel-worker` as a dependency next to your Kotlin plugin (e.g. `kotlin-maven-plugin`); no extra Kotlin-specific artifact is required.

## GraalVM Native Image (optional)

Tests can be executed as native images using the Maven profile `native` (requires a GraalVM installation with Native Image). Typical workflow:

```bash
mvn -Pnative test
```

To run only JVM tests while the profile is active:

```bash
mvn -Pnative test -DskipNativeTests=true
```

## Building from source

```bash
mvn clean verify
```

## Project links

- Repository: [https://github.com/minpor/excel-worker](https://github.com/minpor/excel-worker)
