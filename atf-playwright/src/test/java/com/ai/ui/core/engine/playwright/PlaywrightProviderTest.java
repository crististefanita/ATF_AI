package com.ai.ui.core.engine.playwright;

import com.ai.core.support.config.UiEngineType;
import com.ai.ui.core.engine.UiEngineRegistry;
import com.ai.ui.core.engine.window.UIWindowConfiguration;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Set;

public class PlaywrightProviderTest {
    @Test
    public void adapterClasspathDiscoversOnlyItsOwnEngineWithoutStartingBrowser() {
        Assert.assertEquals(UiEngineRegistry.defaults().supportedTypes(), Set.of(UiEngineType.PLAYWRIGHT));
        Assert.assertEquals(UiEngineRegistry.defaults().supportedValues(), "playwright");
        Assert.expectThrows(ClassNotFoundException.class,
                () -> Class.forName("org.openqa.selenium.WebDriver"));
    }

    @DataProvider(name = "failedAcquisitions")
    public Object[][] failedAcquisitions() {
        Object[][] stages = {{"factory", List.of()}, {"launch", List.of("playwright")},
                {"context", List.of("browser", "playwright")},
                {"page", List.of("context", "browser", "playwright")},
                {"size", List.of("context", "browser", "playwright")},
                {"persistent-page", List.of("context", "playwright")}};
        Object[][] cases = new Object[stages.length * 2][3];
        for (int index = 0; index < stages.length; index++) {
            cases[index * 2] = new Object[]{stages[index][0], false, stages[index][1]};
            cases[index * 2 + 1] = new Object[]{stages[index][0], true, stages[index][1]};
        }
        return cases;
    }

    @Test(dataProvider = "failedAcquisitions")
    public void failedAcquisitionReleasesOwnedResourcesAndPreservesFailure(
            String stage, boolean error, List<String> expectedClosed) {
        Throwable primary = error ? new AssertionError("Controlled acquisition error")
                : new IllegalStateException("Controlled acquisition failure");
        Throwable secondary = new IllegalStateException("Controlled cleanup failure");
        Ports ports = new Ports(stage, primary, secondary, secondary, secondary);
        try (AtfRuntimeConfigSandbox ignored = options("persistent-page".equals(stage))) {
            Throwable observed = Assert.expectThrows(primary.getClass(), ports::open);
            Assert.assertSame(observed, primary, "Acquisition preserves the original failure instance");
            Assert.assertEquals(ports.closed, expectedClosed, "Release follows reverse ownership order exactly once");
            Assert.assertEquals(primary.getSuppressed(), expectedClosed.isEmpty()
                    ? new Throwable[]{} : new Throwable[]{secondary}, "Repeated cleanup causes are retained once");
            if (ports.profile.get() != null) {
                Assert.assertFalse(Files.exists(ports.profile.get()), "Failed persistent acquisition removes its temporary profile");
            }
        }
    }

    @Test
    public void normalCloseReleasesEverythingAndRetainsTheFirstCleanupFailure() {
        Throwable first = new IllegalStateException("Context close failure");
        Throwable second = new AssertionError("Browser close error");
        Throwable third = new IllegalStateException("Playwright close failure");
        Ports ports = new Ports("none", null, first, second, third);
        try (AtfRuntimeConfigSandbox ignored = options(false)) {
            PlaywrightBrowserSession session = ports.open();
            Throwable observed = Assert.expectThrows(first.getClass(), session::close);
            Assert.assertSame(observed, first);
            Assert.assertEquals(observed.getSuppressed(), new Throwable[]{second, third});
            Assert.assertEquals(ports.closed, List.of("context", "browser", "playwright"));
        }
    }

    @Test
    public void successfulAcquisitionAndCloseUseTheSameOwnedResources() {
        Ports ports = new Ports("none", null, null, null, null);
        try (AtfRuntimeConfigSandbox ignored = options(false)) {
            PlaywrightBrowserSession session = ports.open();
            Assert.assertSame(session.page(), ports.page);
            session.close();
            Assert.assertEquals(ports.closed, List.of("context", "browser", "playwright"));
        }
    }

    @Test
    public void aCleanupFailureCannotSuppressItself() {
        Throwable primary = new AssertionError("Shared acquisition and cleanup failure");
        Ports ports = new Ports("size", primary, primary, primary, primary);
        try (AtfRuntimeConfigSandbox ignored = options(false)) {
            Assert.assertSame(Assert.expectThrows(primary.getClass(), ports::open), primary);
            Assert.assertEquals(primary.getSuppressed().length, 0);
            Assert.assertEquals(ports.closed, List.of("context", "browser", "playwright"));
        }
    }

    private static AtfRuntimeConfigSandbox options(boolean persistent) {
        return AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_ENGINE_KEY, "playwright")
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.toString(!persistent))
                .setConfigAndSystemProperty(UiRuntimeOptions.CONSOLE_DETAILS_ENABLED_KEY, "false");
    }

    private static <T> T port(Class<T> type, InvocationHandler calls) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, calls));
    }

    private static final class Ports {
        private final String stage;
        private final Throwable primary;
        private final List<String> closed = new ArrayList<>();
        private final AtomicReference<Path> profile = new AtomicReference<>();
        private final Page page;
        private final Playwright playwright;

        private Ports(String stage, Throwable primary, Throwable contextFailure,
                      Throwable browserFailure, Throwable playwrightFailure) {
            this.stage = stage;
            this.primary = primary;
            AtomicBoolean pageCreated = new AtomicBoolean();
            page = port(Page.class, (proxy, method, args) -> {
                if ("setViewportSize".equals(method.getName())) failAt("size");
                return null;
            });
            BrowserContext context = port(BrowserContext.class, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "pages": return pageCreated.get() ? List.of(page) : List.of();
                    case "newPage":
                        failAt("page"); failAt("persistent-page");
                        pageCreated.set(true); return page;
                    case "close": close("context", contextFailure); return null;
                    default: return null;
                }
            });
            Browser browser = port(Browser.class, (proxy, method, args) -> {
                if ("newContext".equals(method.getName())) { failAt("context"); return context; }
                if ("close".equals(method.getName())) close("browser", browserFailure);
                return null;
            });
            BrowserType type = port(BrowserType.class, (proxy, method, args) -> {
                if ("launch".equals(method.getName())) { failAt("launch"); return browser; }
                if ("launchPersistentContext".equals(method.getName())) {
                    profile.set((Path) args[0]); return context;
                }
                return null;
            });
            playwright = port(Playwright.class, (proxy, method, args) -> {
                if ("chromium".equals(method.getName())) return type;
                if ("close".equals(method.getName())) close("playwright", playwrightFailure);
                return null;
            });
        }

        private PlaywrightBrowserSession open() {
            boolean persistent = "persistent-page".equals(stage);
            return new PlaywrightBrowserSession(UIWindowConfiguration.customSize(800, 600, persistent ? 67 : 100),
                    UiRuntimeOptions.load(), () -> { failAt("factory"); return playwright; });
        }

        private void failAt(String operation) {
            if (stage.equals(operation)) rethrow(primary);
        }

        private void close(String resource, Throwable failure) {
            closed.add(resource); rethrow(failure);
        }

        private static void rethrow(Throwable failure) {
            if (failure instanceof RuntimeException) throw (RuntimeException) failure;
            if (failure instanceof Error) throw (Error) failure;
        }
    }
}
