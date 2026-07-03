package com.ai.skills.atlas.tests.catalog;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.skills.atlas.validation.AtlasAuditValidation;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listeners(TestListener.class)
public class AtlasRuleCatalogContractTest {
    @Test
    public void atlasRuleCatalogMatchesExecutableValidatorRules() {
        AtlasAuditValidation.generatedAtlas().assertRuleCatalogMatchesExecutableValidatorRules();
    }

    @Test
    public void majorAtlasPackagesExposeCreditEfficientLocalReadmes() {
        List<Path> readmes = List.of(
                AtfSourcePaths.MAIN_ATLAS_ROOT.resolve("rules").resolve(AtfSourcePaths.README_FILE_NAME),
                AtfSourcePaths.MAIN_ATLAS_ROOT.resolve("validation").resolve(AtfSourcePaths.README_FILE_NAME)
        );

        List<String> violations = readmes.stream()
                .filter(path -> !hasCreditEfficientShape(path))
                .map(path -> AtfSourcePaths.unixPath(path.normalize()))
                .collect(Collectors.toList());

        Assert.assertTrue(violations.isEmpty(),
                "Major Atlas packages need local README maps with Purpose, Entry points, Change here when, "
                        + "Do not and Verification sections: " + violations);
    }

    @Test
    public void atlasMainCodeDoesNotImportMcpProductCode() throws IOException {
        List<String> violations = directImportViolations(AtfSourcePaths.MAIN_ATLAS_ROOT, "com.ai.skills.mcp");

        Assert.assertTrue(violations.isEmpty(),
                "Atlas is the business-understanding product; MCP package governance must stay artifact/YAML/report "
                        + "based, not a Java dependency: " + violations);
    }

    private static List<String> directImportViolations(Path root, String forbiddenPackage) throws IOException {
        String forbiddenImport = "import " + forbiddenPackage;
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(path -> readUnchecked(path).contains(forbiddenImport))
                    .map(path -> AtfSourcePaths.unixPath(path.normalize()))
                    .collect(Collectors.toList());
        }
    }

    private static String readUnchecked(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect Atlas source " + path, e);
        }
    }

    private static boolean hasCreditEfficientShape(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        String text = readUnchecked(path);
        return text.contains("## Purpose")
                && text.contains("## Entry points")
                && text.contains("## Change here when")
                && text.contains("## Do not")
                && text.contains("## Verification");
    }
}
