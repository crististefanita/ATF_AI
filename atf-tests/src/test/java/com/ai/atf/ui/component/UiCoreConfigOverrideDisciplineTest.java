package com.ai.atf.ui.component;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlUiEngineSpec;
import com.ai.core.reporting.listener.TestListener;
import com.ai.atf.governance.JavaSourceScanner;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_CORE_CONFIG_OVERRIDE)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_CORE_CONFIG_OVERRIDE_CLASS)
@Listeners(TestListener.class)
public class UiCoreConfigOverrideDisciplineTest {


    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_CORE_CONFIG_OVERRIDE_HEADLESS_RESTORE)
    public void uiHeadlessOverridesDeclareRecognizedRestoreMarkers() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "uiHeadlessOverridesDeclareRecognizedRestoreMarkers"
        );
        AtfContractSteps.expected(contract);

        List<String> violatingFiles = new ArrayList<>();
        List<String> overrideFiles = new ArrayList<>();

        for (Path sourceRoot : AtfSourcePaths.VERIFICATION_JAVA_ROOTS) {
            try (Stream<Path> files = Files.walk(sourceRoot.resolve("com/ai/atf"))) {
                files.filter(AtfSourcePaths::isJavaSource)
                        .forEach(path -> inspectFile(path, overrideFiles, violatingFiles));
            }
        }

        AtfContractSteps.observed("override.files=" + overrideFiles
                + " | violating.files=" + violatingFiles);

        Assert.assertFalse(overrideFiles.isEmpty(), "The static scan must still recognize the current override forms.");
        Assert.assertTrue(
                violatingFiles.isEmpty(),
                "Recognized ui.headless overrides without executable restoration markers: " + violatingFiles
        );
        AtfContractSteps.verdict(
                "Recognized overrides declare restoration markers in executable tokens; runtime cleanup requires behavioral checks.",
                contract.repairHint()
        );
    }

    @Test
    public void commentsAndLiteralExamplesCannotDeclareRestoreDiscipline() {
        String override = "class Candidate { void change(){ ConfigManager.set(\"ui.headless\", \"false\"); } ";
        Assert.assertTrue(overridesUiHeadless(override + '}'));
        Assert.assertFalse(hasRestoreDiscipline(override + "// AtfRuntimeConfigSandbox\n}"));
        Assert.assertFalse(hasRestoreDiscipline(override + "String example = \"AtfRuntimeConfigSandbox\"; }"));
        Assert.assertFalse(hasRestoreDiscipline(override
                + "// @AfterMethod previousUiHeadless restore(\"ui.headless\")\n}"));
        Assert.assertFalse(overridesUiHeadless("// ConfigManager.set(\"ui.headless\", \"false\");"));
    }

    @Test
    public void realLiteralArgumentsAndRecognizedRestoreTokensRemainCandidates() {
        String sandbox = "class Candidate { void change(){ try (AtfRuntimeConfigSandbox config = AtfRuntimeConfigSandbox.open()) {"
                + "config.setConfig(\"ui.headless\", \"false\"); } } }";
        Assert.assertTrue(overridesUiHeadless(sandbox), "Literal property keys must remain visible to candidate detection.");
        Assert.assertTrue(hasRestoreDiscipline(sandbox));
        String manual = "class Candidate { String previousUiHeadless; void change(){ ConfigManager.set(\"ui.headless\", \"false\"); }"
                + "@AfterMethod void restoreState(){ ConfigManager.set(\"ui.headless\", previousUiHeadless); } }";
        Assert.assertTrue(overridesUiHeadless(manual));
        Assert.assertTrue(hasRestoreDiscipline(manual));
    }

    private static void inspectFile(
            Path path,
            List<String> overrideFiles,
            List<String> violatingFiles
    ) {
        try {
            String source = Files.readString(path);
            if (!overridesUiHeadless(source)) {
                return;
            }

            String relativePath = AtfSourcePaths.projectRelative(path);
            overrideFiles.add(relativePath);

            if (!hasRestoreDiscipline(source)) {
                violatingFiles.add(relativePath);
            }
        } catch (IOException e) {
            violatingFiles.add(path + " | unreadable: " + e.getMessage());
        }
    }

    private static boolean overridesUiHeadless(String source) {
        return containsAny(source, uiEngineValues("headless-override-marker"));
    }

    private static boolean hasRestoreDiscipline(String source) {
        return JavaSourceScanner.containsExecutableMarker(source, "AtfRuntimeConfigSandbox")
                || JavaSourceScanner.containsExecutableMarker(source, "@AfterMethod")
                && JavaSourceScanner.containsExecutableMarker(source, "previousUiHeadless")
                && containsAny(source, uiEngineValues("headless-restore-marker"));
    }

    private static boolean containsAny(String source, List<String> markers) {
        return markers.stream().anyMatch(marker -> JavaSourceScanner.containsExecutableMarker(source, marker));
    }

    private static List<String> uiEngineValues(String kind) {
        return AtfCoreYamlContractCatalog.uiEngineContracts().stream()
                .filter(spec -> kind.equals(spec.kind()))
                .map(AtfCoreYamlUiEngineSpec::value)
                .collect(java.util.stream.Collectors.toUnmodifiableList());
    }
}
