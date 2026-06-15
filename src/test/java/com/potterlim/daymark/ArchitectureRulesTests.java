package com.potterlim.daymark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureRulesTests {

    private static final Path MAIN_SOURCE_ROOT = Path.of("src/main/java/com/potterlim/daymark");

    @Test
    void controllersShouldNotDependOnRepositories() throws IOException {
        assertPackageShouldNotImport(
            "controller",
            List.of(
                "com.potterlim.daymark.repository."
            )
        );
    }

    @Test
    void dataTransferObjectsShouldNotDependOnOuterLayers() throws IOException {
        assertPackageShouldNotImport(
            "dto",
            List.of(
                "com.potterlim.daymark.config.",
                "com.potterlim.daymark.controller.",
                "com.potterlim.daymark.repository.",
                "com.potterlim.daymark.security.",
                "com.potterlim.daymark.service."
            )
        );
    }

    @Test
    void entitiesShouldNotDependOnOuterLayers() throws IOException {
        assertPackageShouldNotImport(
            "entity",
            List.of(
                "com.potterlim.daymark.config.",
                "com.potterlim.daymark.controller.",
                "com.potterlim.daymark.dto.",
                "com.potterlim.daymark.repository.",
                "com.potterlim.daymark.security.",
                "com.potterlim.daymark.service."
            )
        );
    }

    @Test
    void identityValuesShouldNotDependOnApplicationLayers() throws IOException {
        assertPackageShouldNotImport(
            "identity",
            List.of(
                "com.potterlim.daymark.config.",
                "com.potterlim.daymark.controller.",
                "com.potterlim.daymark.dto.",
                "com.potterlim.daymark.entity.",
                "com.potterlim.daymark.repository.",
                "com.potterlim.daymark.security.",
                "com.potterlim.daymark.service.",
                "com.potterlim.daymark.support."
            )
        );
    }

    @Test
    void repositoriesShouldNotDependOnApplicationLayers() throws IOException {
        assertPackageShouldNotImport(
            "repository",
            List.of(
                "com.potterlim.daymark.config.",
                "com.potterlim.daymark.controller.",
                "com.potterlim.daymark.dto.",
                "com.potterlim.daymark.security.",
                "com.potterlim.daymark.service."
            )
        );
    }

    @Test
    void servicesShouldNotDependOnControllersOrSecurityAdapters() throws IOException {
        assertPackageShouldNotImport(
            "service",
            List.of(
                "com.potterlim.daymark.controller.",
                "com.potterlim.daymark.security."
            )
        );
    }

    @Test
    void supportValuesShouldNotDependOnApplicationLayers() throws IOException {
        assertPackageShouldNotImport(
            "support",
            List.of(
                "com.potterlim.daymark.config.",
                "com.potterlim.daymark.controller.",
                "com.potterlim.daymark.dto.",
                "com.potterlim.daymark.entity.",
                "com.potterlim.daymark.repository.",
                "com.potterlim.daymark.security.",
                "com.potterlim.daymark.service."
            )
        );
    }

    private static void assertPackageShouldNotImport(
            String packageDirectory,
            List<String> forbiddenImportPrefixes) throws IOException {
        List<String> violations = findForbiddenImports(packageDirectory, forbiddenImportPrefixes);

        assertThat(violations).isEmpty();
    }

    private static List<String> findForbiddenImports(
            String packageDirectory,
            List<String> forbiddenImportPrefixes) throws IOException {
        Path packageRoot = MAIN_SOURCE_ROOT.resolve(packageDirectory);
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> sourceFiles = Files.walk(packageRoot)) {
            List<Path> javaFiles = sourceFiles
                .filter(Files::isRegularFile)
                .filter(sourceFile -> sourceFile.toString().endsWith(".java"))
                .toList();

            for (Path javaFile : javaFiles) {
                addForbiddenImports(violations, javaFile, forbiddenImportPrefixes);
            }
        }

        return violations;
    }

    private static void addForbiddenImports(
            List<String> violations,
            Path javaFile,
            List<String> forbiddenImportPrefixes) throws IOException {
        for (String sourceLine : Files.readAllLines(javaFile)) {
            addForbiddenImportOrIgnore(violations, javaFile, sourceLine, forbiddenImportPrefixes);
        }
    }

    private static void addForbiddenImportOrIgnore(
            List<String> violations,
            Path javaFile,
            String sourceLine,
            List<String> forbiddenImportPrefixes) {
        String trimmedLine = sourceLine.trim();

        for (String forbiddenImportPrefix : forbiddenImportPrefixes) {
            if (trimmedLine.startsWith("import " + forbiddenImportPrefix)) {
                violations.add(MAIN_SOURCE_ROOT.relativize(javaFile) + " -> " + trimmedLine);
            }
        }
    }
}
