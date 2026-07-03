/**
 * ATF framework contract tests.
 *
 * <p>This tree is not product coverage. It proves framework behavior: listener
 * lifecycle, reporting adapters, screenshot evidence, API/UI base classes, and
 * runtime configuration boundaries.</p>
 *
 * <p>Preferred shape for new tests:</p>
 * <ol>
 *     <li>Add {@code @AtfContractSuite} on the class.</li>
 *     <li>Add class or method {@code @AtfContract} metadata.</li>
 *     <li>Use {@code @Listeners(TestListener.class)} unless the test is a
 *     self-auditing Extent renderer fixture.</li>
 *     <li>Keep live API/UI smoke tests in explicit boundary profiles.</li>
 * </ol>
 */
package com.endava.ai.atf;
