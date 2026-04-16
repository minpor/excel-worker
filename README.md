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

The library is not on **Maven Central** yet. The usual way to consume it **without any credentials** (no GitHub tokens, no `gradle.properties` secrets) is **[JitPack](https://jitpack.io)**.

## Gradle (JitPack — no credentials)

[JitPack](https://jitpack.io) builds this public GitHub repo on demand. You only declare the **`https://jitpack.io`** repository and a dependency — **no** `credentials { }`, **no** `GITHUB_TOKEN`, **no** PAT.

**Coordinates:** JitPack uses `com.github.<GitHub-username>:<repo-name>:<tag-or-commit>`. The version is a **Git tag** (e.g. `v0.1.3`) or commit hash — see [JitPack — excel-worker](https://jitpack.io/#minpor/excel-worker).

### Kotlin DSL (`build.gradle.kts`)

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.minpor:excel-worker:v0.1.3")
}
```

### Groovy (`build.gradle`)

```groovy
repositories {
    mavenCentral()
    maven { url = "https://jitpack.io" }
}

dependencies {
    implementation "com.github.minpor:excel-worker:v0.1.3"
}
```

### Repositories only in `settings.gradle.kts` (Gradle 7+)

If you centralize repositories (e.g. `dependencyResolutionManagement`):

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Kotlin Multiplatform

This artifact is **JVM-only**; add the dependency in the **JVM** target (e.g. `jvmMain` / `kotlin { jvm { } }`), not `commonMain`.

---

## Maven (JitPack — no credentials)

Same idea: add JitPack and use **`com.github.minpor`** (not `io.github.minpor`):

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.minpor</groupId>
    <artifactId>excel-worker</artifactId>
    <version>v0.1.3</version>
</dependency>
```

## GitHub Packages (optional — requires credentials)

Only use this if you intentionally depend on artifacts published to GitHub’s registry ([**Packages**](https://github.com/minpor/excel-worker/packages)). You must configure **authentication** ([PAT](https://github.com/settings/tokens) with `read:packages`, or CI secrets). This is **not** needed for normal Gradle/Maven use with JitPack above.

After each **`v*`** tag, CI may publish to `https://maven.pkg.github.com/minpor/excel-worker` with `groupId` **`io.github.minpor`** and version **`0.1.3`** (without `v`).

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/minpor/excel-worker</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.minpor</groupId>
    <artifactId>excel-worker</artifactId>
    <version>0.1.3</version>
</dependency>
```

See [Working with the Apache Maven registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry). Maintainer notes for **`401`** on publish: [publish-github-packages.yml](https://github.com/minpor/excel-worker/blob/main/.github/workflows/publish-github-packages.yml) and repo **Actions → Workflow permissions**.

## Install from a local checkout

```bash
mvn clean install
```

Then use **`io.github.minpor:excel-worker`** with the `<version>` from that checkout’s `pom.xml` (no `v` prefix).

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
