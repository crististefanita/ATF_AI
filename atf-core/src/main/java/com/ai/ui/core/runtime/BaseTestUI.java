package com.ai.ui.core.runtime;

import com.ai.core.reporting.listener.TestListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

/**
 * Engine setup/teardown only; MUST NOT manage reporting lifecycle.
 */
@Listeners(TestListener.class)
public abstract class BaseTestUI {

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        UiInteractionTracker.reset();
        DriverManager.initEngine();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        try {
            DriverManager.quitEngine();
        } finally {
            UiInteractionTracker.clear();
        }
    }
}
