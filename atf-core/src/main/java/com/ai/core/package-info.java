/**
 * Core ATF runtime: configuration, TestNG listener orchestration, and reporting.
 *
 * <p>The main flow is intentionally layered:</p>
 * <ol>
 *     <li>{@link com.ai.core.support.config.ConfigManager} reads runtime
 *     properties and exposes stable config keys.</li>
 *     <li>{@link com.ai.core.reporting.listener.TestListener} owns TestNG lifecycle
 *     events and keeps setup, test body, teardown, and failures in the right
 *     report section.</li>
 *     <li>{@link com.ai.core.reporting.StepLogger} is the small public
 *     facade used by tests and framework code to publish steps.</li>
 *     <li>{@link com.ai.core.reporting.ReportingManager} selects the
 *     active {@link com.ai.core.reporting.ReportLogger} adapter.</li>
 * </ol>
 *
 * <p>Core patterns used here: Facade for step logging, Adapter for Extent and
 * Allure, Composite for mirrored reporting, Listener for TestNG lifecycle, and
 * small controller classes under {@code listener.internal} for lifecycle state.</p>
 */
package com.ai.core;
