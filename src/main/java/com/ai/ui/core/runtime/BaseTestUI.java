package com.ai.ui.core.runtime;

import com.ai.core.reporting.listener.TestListener;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.annotations.*;

/**
 * Engine setup/teardown only; MUST NOT manage reporting lifecycle.
 */
@Listeners({
        AllureTestNg.class,
        TestListener.class
})
public abstract class BaseTestUI {

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        UiInteractionTracker.reset();
        DriverManager.initEngine();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitEngine();
        UiInteractionTracker.clear();
    }
}
