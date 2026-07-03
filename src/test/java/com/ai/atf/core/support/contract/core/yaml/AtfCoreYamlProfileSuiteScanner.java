package com.ai.atf.core.support.contract.core.yaml;

import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

final class AtfCoreYamlProfileSuiteScanner {
    private AtfCoreYamlProfileSuiteScanner() {
    }

    static List<Path> publicProfileSuiteRoots() {
        return List.of(
                AtfSourcePaths.testResource("atf/core/suites"),
                AtfSourcePaths.testResource("skills/business"),
                AtfSourcePaths.testResource("skills/codequality")
        );
    }

    static List<String> publicSuitesUnder(Path root) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (var files = Files.walk(root)) {
            return files
                    .filter(AtfSourcePaths::isXmlFile)
                    .filter(AtfCoreYamlProfileSuiteScanner::isPublicProfileSuite)
                    .map(AtfSourcePaths::projectRelative)
                    .collect(Collectors.toList());
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot scan public ATF profile suites under " + root, exception);
        }
    }

    private static boolean isPublicProfileSuite(Path path) {
        String relative = AtfSourcePaths.projectRelative(path);
        return relative.startsWith("src/test/resources/skills/business/")
                || relative.startsWith("src/test/resources/skills/codequality/")
                || relative.startsWith("src/test/resources/atf/core/suites/");
    }
}
