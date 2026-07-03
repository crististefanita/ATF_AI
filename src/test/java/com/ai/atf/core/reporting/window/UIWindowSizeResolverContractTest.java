package com.ai.atf.core.reporting.window;

import com.ai.core.reporting.listener.TestListener;
import org.testng.annotations.Listeners;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.window.UIWindowConfiguration;
import com.ai.ui.core.engine.window.UIWindowSizeResolver;
import com.ai.ui.core.engine.window.WindowMode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.awt.HeadlessException;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.ai.atf.core.support.contract.AtfWindowContractAssertions.assertWindowConfiguration;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_WINDOW_SIZE_RESOLVER)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_WINDOW_SIZE_RESOLVER_CLASS)
@Listeners(TestListener.class)
public class UIWindowSizeResolverContractTest {

    private static final int DEFAULT_WIDTH = UIEngine.DEFAULT_WINDOW_WIDTH;
    private static final int DEFAULT_HEIGHT = UIEngine.DEFAULT_WINDOW_HEIGHT;
    private static final String FULLSCREEN = UiRuntimeOptions.WINDOW_MODE_FULLSCREEN;

    @DataProvider
    public Object[][] windowResolutionCases() {
        return cases(
                sizeAndModeCases(),
                zoomCases(),
                fallbackCases()
        );
    }

    private static Object[][] sizeAndModeCases() {
        return new Object[][]{
                contractCase("explicit-size-overrides-mode",
                        "1920x1080", FULLSCREEN, "67", screen(3840, 2160),
                        WindowMode.CUSTOM_SIZE, 1920, 1080, 67),
                contractCase("fullscreen-uses-system-size",
                        null, FULLSCREEN, "100", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 100),
                contractCase("invalid-size-falls-back-to-fullscreen",
                        "invalid-size", FULLSCREEN, "100", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 100),
                contractCase("missing-system-size-falls-back-to-defaults",
                        null, FULLSCREEN, "100", () -> null,
                        WindowMode.FULLSCREEN, DEFAULT_WIDTH, DEFAULT_HEIGHT, 100),
                contractCase("empty-size-is-treated-as-missing",
                        "", FULLSCREEN, "100", screen(3200, 1800),
                        WindowMode.FULLSCREEN, 3200, 1800, 100),
                contractCase("blank-size-is-treated-as-missing",
                        "   ", FULLSCREEN, "100", screen(3200, 1800),
                        WindowMode.FULLSCREEN, 3200, 1800, 100)
        };
    }

    private static Object[][] zoomCases() {
        return new Object[][]{
                contractCase("valid-zoom-is-preserved",
                        null, FULLSCREEN, "67", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 67),
                contractCase("unsupported-zoom-maps-to-nearest-native-level",
                        null, FULLSCREEN, "60", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 67),
                contractCase("unsupported-zoom-prefers-nearest-lower-native-level",
                        null, FULLSCREEN, "84", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 80),
                contractCase("invalid-zoom-falls-back-to-default",
                        null, FULLSCREEN, "abc", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 100),
                contractCase("out-of-range-zoom-falls-back-to-default",
                        null, FULLSCREEN, "5", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 100),
                contractCase("null-zoom-falls-back-to-default",
                        null, FULLSCREEN, null, screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 100),
                contractCase("blank-zoom-falls-back-to-default",
                        null, FULLSCREEN, "   ", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 100)
        };
    }

    private static Object[][] fallbackCases() {
        return new Object[][]{
                contractCase("invalid-mode-falls-back-to-fullscreen",
                        null, "unknown-mode", "100", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 100),
                contractCase("null-mode-falls-back-to-fullscreen",
                        null, null, "100", screen(3000, 2000),
                        WindowMode.FULLSCREEN, 3000, 2000, 100),
                contractCase("invalid-supplier-value-falls-back-to-defaults",
                        null, FULLSCREEN, "100", screen(-1, 2000),
                        WindowMode.FULLSCREEN, DEFAULT_WIDTH, DEFAULT_HEIGHT, 100),
                contractCase("invalid-supplier-shape-falls-back-to-defaults",
                        null, FULLSCREEN, "100", () -> new int[]{3000},
                        WindowMode.FULLSCREEN, DEFAULT_WIDTH, DEFAULT_HEIGHT, 100),
                contractCase("headless-exception-falls-back-to-defaults",
                        null, FULLSCREEN, "100", () -> {
                            throw new HeadlessException("headless");
                        },
                        WindowMode.FULLSCREEN, DEFAULT_WIDTH, DEFAULT_HEIGHT, 100)
        };
    }

    private static Object[][] cases(Object[][]... groups) {
        int total = 0;
        for (Object[][] group : groups) {
            total += group.length;
        }
        Object[][] all = new Object[total][];
        int index = 0;
        for (Object[][] group : groups) {
            for (Object[] testCase : group) {
                all[index++] = testCase;
            }
        }
        return all;
    }

    @Test(dataProvider = "windowResolutionCases")
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_WINDOW_SIZE_RESOLVER_MATRIX)
    public void window_size_resolution_contract_is_declarative(WindowResolutionCase testCase) {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "window_size_resolution_contract_is_declarative"
        );
        AtfContractSteps.expected(contract);

        UIWindowConfiguration resolved = UIWindowSizeResolver.resolve(
                testCase.windowSize,
                testCase.windowMode,
                testCase.zoomPercent,
                testCase.systemSizeSupplier
        );

        AtfContractSteps.observed(testCase.observed(resolved));
        assertWindowConfiguration(
                resolved,
                testCase.expectedMode,
                testCase.expectedWidth,
                testCase.expectedHeight,
                testCase.expectedZoomPercent
        );
        AtfContractSteps.verdict(
                "Window resolver case passed: " + testCase.name,
                contract.repairHint()
        );
    }

    private static Object[] contractCase(
            String name,
            String windowSize,
            String windowMode,
            String zoomPercent,
            Supplier<int[]> systemSizeSupplier,
            WindowMode expectedMode,
            int expectedWidth,
            int expectedHeight,
            int expectedZoomPercent
    ) {
        return new Object[]{
                new WindowResolutionCase(
                        name,
                        windowSize,
                        windowMode,
                        zoomPercent,
                        systemSizeSupplier,
                        expectedMode,
                        expectedWidth,
                        expectedHeight,
                        expectedZoomPercent
                )
        };
    }

    private static Supplier<int[]> screen(int width, int height) {
        return () -> new int[]{width, height};
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class WindowResolutionCase {
        private final String name;
        private final String windowSize;
        private final String windowMode;
        private final String zoomPercent;
        private final Supplier<int[]> systemSizeSupplier;
        private final WindowMode expectedMode;
        private final int expectedWidth;
        private final int expectedHeight;
        private final int expectedZoomPercent;

        private String observed(UIWindowConfiguration actual) {
            return "case=" + name
                    + " | input(size=" + printable(windowSize)
                    + ", mode=" + printable(windowMode)
                    + ", zoom=" + printable(zoomPercent)
                    + ") | expected(mode=" + expectedMode
                    + ", width=" + expectedWidth
                    + ", height=" + expectedHeight
                    + ", zoom=" + expectedZoomPercent
                    + ") | actual(mode=" + actual.mode()
                    + ", width=" + actual.width()
                    + ", height=" + actual.height()
                    + ", zoom=" + actual.zoomPercent()
                    + ")";
        }

        @Override
        public String toString() {
            return name;
        }

        private static String printable(String value) {
            return value == null ? "<null>" : "'" + value + "'";
        }
    }
}
