package com.ai.core.reporting.spi;

import org.testng.IConfigurationListener;
import org.testng.IInvokedMethodListener;
import org.testng.ISuiteListener;
import org.testng.ITestListener;

/** Backend-neutral TestNG lifecycle contract used by the Core listener facade. */
public interface ReportingLifecycleListener
        extends ITestListener, ISuiteListener, IInvokedMethodListener, IConfigurationListener {
}
