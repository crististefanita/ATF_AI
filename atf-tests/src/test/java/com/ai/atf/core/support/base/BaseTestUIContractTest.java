package com.ai.atf.core.support.base;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.ui.core.runtime.BaseTestUI;
import com.ai.ui.core.runtime.DriverManager;
import com.ai.ui.core.runtime.UiInteractionTracker;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.BASE_UI_RUNTIME)
@AtfContractRef(AtfContractNarrativeIds.Contract.BASE_UI_RUNTIME_CLASS)
@Listeners(TestListener.class)
public class BaseTestUIContractTest {
    private static final Path BASE_TEST_UI_SOURCE =
            AtfSourcePaths.mainJava(BaseTestUI.class);

    @AfterMethod
    public void cleanup() {
        DriverManager.quitEngine();
        UiInteractionTracker.clear();
        ReportingManager.reset();
        StepLogger.clear();
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_UI_RUNTIME_LISTENER_BOUNDARY)
    public void baseTestUiWiresReportingListenersButDoesNotOwnReportingLifecycle() throws Exception {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "baseTestUiWiresReportingListenersButDoesNotOwnReportingLifecycle"
        );
        AtfContractSteps.expected(contract);

        Listeners listeners = BaseTestUI.class.getAnnotation(Listeners.class);
        Assert.assertNotNull(listeners, "BaseTestUI must declare TestNG listeners");
        Assert.assertEquals(Arrays.asList(listeners.value()), List.of(TestListener.class));
        Assert.assertTrue(Arrays.asList(listeners.value()).contains(TestListener.class));

        String source = Files.readString(BASE_TEST_UI_SOURCE);
        Assert.assertFalse(source.contains("ReportingManager"), "BaseTestUI must not directly orchestrate ReportingManager");
        Assert.assertFalse(source.contains("StepLogger"), "BaseTestUI must not directly orchestrate StepLogger");

        AtfContractSteps.observed("@Listeners contains the Core TestListener facade; Allure is discovered through SPI | no ReportingManager/StepLogger source coupling");
        AtfContractSteps.verdict(
                "BaseTestUI remains a thin UI lifecycle base class, not a hidden reporting controller.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.BASE_UI_RUNTIME_LIFECYCLE)
    public void baseTestUiLifecycleHooksAreClassScopedAndDeterministic() throws Exception {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "baseTestUiLifecycleHooksAreClassScopedAndDeterministic"
        );
        AtfContractSteps.expected(contract);

        Method setup = BaseTestUI.class.getDeclaredMethod("setUp");
        Method tearDown = BaseTestUI.class.getDeclaredMethod("tearDown");
        BeforeClass beforeClass = setup.getAnnotation(BeforeClass.class);
        org.testng.annotations.AfterClass afterClass = tearDown.getAnnotation(org.testng.annotations.AfterClass.class);

        Assert.assertNotNull(beforeClass, "setUp must be @BeforeClass");
        Assert.assertTrue(beforeClass.alwaysRun(), "setUp must always run");
        Assert.assertNotNull(afterClass, "tearDown must be @AfterClass");
        Assert.assertTrue(afterClass.alwaysRun(), "tearDown must always run");

        AtfContractSteps.observed("setUp=@BeforeClass(alwaysRun=true) | tearDown=@AfterClass(alwaysRun=true)");
        AtfContractSteps.verdict(
                "UI engine lifecycle is deterministic and class-scoped.",
                contract.repairHint()
        );
    }
}
