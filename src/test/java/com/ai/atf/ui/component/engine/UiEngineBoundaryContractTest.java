package com.ai.atf.ui.component.engine;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_CAPABILITY)
@Listeners(TestListener.class)
public class UiEngineBoundaryContractTest {
    private static final List<Path> LEGACY_ROOT_PATHS = List.of(
            UiEngineContractSourceSupport.uiPath("actions"),
            UiEngineContractSourceSupport.uiPath("component"),
            UiEngineContractSourceSupport.uiPath("core", "BaseTestUI.java"),
            UiEngineContractSourceSupport.uiPath("core", "DriverManager.java"),
            UiEngineContractSourceSupport.uiPath("core", "UiInteractionTracker.java"),
            UiEngineContractSourceSupport.uiPath("engine"),
            UiEngineContractSourceSupport.uiPath("factory"),
            UiEngineContractSourceSupport.uiPath("model"),
            UiEngineContractSourceSupport.uiPath("pages"),
            UiEngineContractSourceSupport.uiPath("reporting"),
            UiEngineContractSourceSupport.uiPath("service"),
            UiEngineContractSourceSupport.uiPath("support", "runtime"),
            UiEngineContractSourceSupport.uiPath("utils"),
            UiEngineContractSourceSupport.uiPath("validation")
    );

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_REPORTING_BOUNDARY)
    public void concreteEnginesDoNotDependDirectlyOnStepLogger() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "concreteEnginesDoNotDependDirectlyOnStepLogger"
        );
        AtfContractSteps.expected(contract);

        List<Path> engineSources = List.of(
                UiEngineContractSourceSupport.UI_ENGINE_ROOT.resolve("playwright/PlaywrightEngine.java"),
                UiEngineContractSourceSupport.UI_ENGINE_ROOT.resolve("selenium/SeleniumEngine.java")
        );
        List<Path> offenders = engineSources.stream()
                .filter(UiEngineContractSourceSupport::containsStepLoggerReference)
                .collect(Collectors.toList());

        AtfContractSteps.observed("engine.sources=" + engineSources + " | direct.reporting.offenders=" + offenders);
        Assert.assertTrue(
                offenders.isEmpty(),
                "Concrete UI engines must not depend directly on StepLogger: " + offenders
        );
        AtfContractSteps.verdict(
                "UI engines remain browser adapters; runtime reporting is delegated to a dedicated publisher.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_BROWSER_IMPORT_BOUNDARY)
    public void browserSpecificImportsStayInsideUiEnginePackage() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "browserSpecificImportsStayInsideUiEnginePackage"
        );
        AtfContractSteps.expected(contract);

        Path sourceRoot = AtfSourcePaths.MAIN_JAVA_ROOT;
        List<Path> offenders;
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            offenders = files
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(UiEngineContractSourceSupport::hasBrowserSpecificImport)
                    .filter(path -> !UiEngineContractSourceSupport.isUiEnginePackage(path))
                    .collect(Collectors.toList());
        }

        AtfContractSteps.observed("source.root=" + sourceRoot
                + " | browser.import.offenders=" + offenders);
        Assert.assertTrue(
                offenders.isEmpty(),
                "Browser-specific imports must stay inside com.ai.ui.core.engine: " + offenders
        );
        AtfContractSteps.verdict(
                "Browser-specific APIs are contained behind the UIEngine adapter boundary.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_PACKAGE_ARCHITECTURE)
    public void uiMainPackagesSeparateEngineFoundationFromTestSupport() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "uiMainPackagesSeparateEngineFoundationFromTestSupport"
        );
        AtfContractSteps.expected(contract);

        List<Path> legacyPaths = LEGACY_ROOT_PATHS.stream()
                .filter(Files::exists)
                .collect(Collectors.toList());

        AtfContractSteps.observed("engine.root=" + UiEngineContractSourceSupport.UI_ENGINE_ROOT
                + " | runtime.root=" + UiEngineContractSourceSupport.UI_RUNTIME_ROOT
                + " | support.root=" + UiEngineContractSourceSupport.UI_SUPPORT_ROOT
                + " | legacy.paths=" + legacyPaths);

        Assert.assertTrue(Files.isDirectory(UiEngineContractSourceSupport.UI_ENGINE_ROOT),
                "Missing UI engine foundation root: " + UiEngineContractSourceSupport.UI_ENGINE_ROOT);
        Assert.assertTrue(Files.isDirectory(UiEngineContractSourceSupport.UI_RUNTIME_ROOT),
                "Missing UI runtime foundation root: " + UiEngineContractSourceSupport.UI_RUNTIME_ROOT);
        Assert.assertTrue(Files.isDirectory(UiEngineContractSourceSupport.UI_SUPPORT_ROOT),
                "Missing UI test support root: " + UiEngineContractSourceSupport.UI_SUPPORT_ROOT);
        Assert.assertTrue(
                legacyPaths.isEmpty(),
                "Legacy root-level UI packages must be removed or moved into ui.core / ui.support: " + legacyPaths
        );
        AtfContractSteps.verdict(
                "UI package ownership is explicit: engine/runtime foundation stays separate from UI-test support.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_PACKAGE_ARCHITECTURE)
    public void uiEngineRootStaysBelowCodeQualityDefaultAfterUtilitySplit() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "uiEngineRootStaysBelowCodeQualityDefaultAfterUtilitySplit"
        );
        AtfContractSteps.expected(contract);

        long rootClasses = UiEngineContractSourceSupport.directJavaClassCount(
                UiEngineContractSourceSupport.UI_ENGINE_ROOT
        );
        Path syntaxPackage = UiEngineContractSourceSupport.UI_ENGINE_ROOT.resolve("syntax");
        Path timingPackage = UiEngineContractSourceSupport.UI_ENGINE_ROOT.resolve("timing");
        Path zoomPackage = UiEngineContractSourceSupport.UI_ENGINE_ROOT.resolve("zoom");

        AtfContractSteps.observed("ui.engine.root.classes=" + rootClasses
                + " | syntax=" + syntaxPackage
                + " | timing=" + timingPackage
                + " | zoom=" + zoomPackage);

        Assert.assertTrue(rootClasses <= 8,
                "UI engine root must stay under the Code Quality default package cap: " + rootClasses);
        Assert.assertTrue(Files.isDirectory(syntaxPackage), "Missing selector/option syntax package.");
        Assert.assertTrue(Files.isDirectory(timingPackage), "Missing timing helper package.");
        Assert.assertTrue(Files.isDirectory(zoomPackage), "Missing shared zoom vocabulary package.");

        AtfContractSteps.verdict(
                "UI engine root no longer needs a legacy package ratchet; utility concerns are split by role.",
                contract.repairHint()
        );
    }
}
