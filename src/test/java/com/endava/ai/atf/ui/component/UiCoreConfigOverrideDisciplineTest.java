package com.endava.ai.atf.ui.component;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfContractSteps;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlUiEngineSpec;
import com.endava.ai.core.reporting.listener.TestListener;
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

    private static final Path ATF_TEST_ROOT = AtfSourcePaths.TEST_ATF_ROOT;

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_CORE_CONFIG_OVERRIDE_HEADLESS_RESTORE)
    public void uiHeadlessOverridesAreRestoredByContractTests() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "uiHeadlessOverridesAreRestoredByContractTests"
        );
        AtfContractSteps.expected(contract);

        List<String> violatingFiles = new ArrayList<>();
        List<String> overrideFiles = new ArrayList<>();

        try (Stream<Path> files = Files.walk(ATF_TEST_ROOT)) {
            files.filter(AtfSourcePaths::isJavaSource)
                    .forEach(path -> inspectFile(path, overrideFiles, violatingFiles));
        }

        AtfContractSteps.observed("override.files=" + overrideFiles
                + " | violating.files=" + violatingFiles);

        Assert.assertTrue(
                violatingFiles.isEmpty(),
                "ui.headless overrides without restore discipline: " + violatingFiles
        );
        AtfContractSteps.verdict(
                "Every ui.headless override remains local to its contract test and restores runtime state.",
                contract.repairHint()
        );
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

            String relativePath = ATF_TEST_ROOT.relativize(path).toString();
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
        return source.contains("AtfRuntimeConfigSandbox")
                || source.contains("@AfterMethod")
                && source.contains("previousUiHeadless")
                && containsAny(source, uiEngineValues("headless-restore-marker"));
    }

    private static boolean containsAny(String source, List<String> markers) {
        return markers.stream().anyMatch(source::contains);
    }

    private static List<String> uiEngineValues(String kind) {
        return AtfCoreYamlContractCatalog.uiEngineContracts().stream()
                .filter(spec -> kind.equals(spec.kind()))
                .map(AtfCoreYamlUiEngineSpec::value)
                .collect(java.util.stream.Collectors.toUnmodifiableList());
    }
}
