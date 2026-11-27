package com.ai.ui.core.engine.selenium;

import com.ai.core.support.config.UiEngineType;
import com.ai.ui.core.engine.UiEngineRegistry;
import com.ai.ui.core.engine.window.UIWindowConfiguration;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.devtools.CdpInfo;
import org.openqa.selenium.devtools.CdpVersionFinder;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;

public class SeleniumProviderTest {
    @DataProvider(name = "failedAcquisitions")
    public Object[][] failedAcquisitions() {
        return new Object[][]{{"factory", false}, {"timeouts", false}, {"window", false}, {"handles", false},
                {"factory", true}, {"timeouts", true}, {"window", true}, {"handles", true}};
    }

    @Test(dataProvider = "failedAcquisitions")
    public void failedAcquisitionClosesTheDriverAndPreservesTheFailure(String stage, boolean error) {
        Throwable primary = error ? new AssertionError("Controlled acquisition error")
                : new IllegalStateException("Controlled acquisition failure");
        Throwable secondary = new IllegalStateException("Controlled driver cleanup failure");
        AtomicInteger quitCalls = new AtomicInteger();
        WebDriver driver = driver(stage, primary, secondary, quitCalls);
        try (AtfRuntimeConfigSandbox ignored = options()) {
            Throwable observed = Assert.expectThrows(primary.getClass(), () -> new SeleniumBrowserSession(
                    UIWindowConfiguration.customSize(800, 600, 100), UiRuntimeOptions.load(), () -> {
                        failAt(stage, "factory", primary); return driver;
                    }));
            Assert.assertSame(observed, primary);
            Assert.assertEquals(quitCalls.get(), "factory".equals(stage) ? 0 : 1,
                    "Only a successfully acquired driver is closed");
            Assert.assertEquals(primary.getSuppressed(), "factory".equals(stage)
                    ? new Throwable[]{} : new Throwable[]{secondary});
        }
    }

    @Test
    public void successfulAcquisitionUsesAndClosesTheOwnedDriver() throws Exception {
        AtomicInteger quitCalls = new AtomicInteger();
        WebDriver driver = driver("none", null, null, quitCalls);
        try (AtfRuntimeConfigSandbox ignored = options()) {
            SeleniumBrowserSession session = new SeleniumBrowserSession(
                    UIWindowConfiguration.customSize(800, 600, 100), UiRuntimeOptions.load(), () -> driver);
            Assert.assertSame(session.driver(), driver);
            session.close();
            Assert.assertEquals(quitCalls.get(), 1);
        }
        for (boolean headless : new boolean[]{true, false}) {
            for (boolean fullscreen : new boolean[]{true, false}) {
                try (AtfRuntimeConfigSandbox settings = options()
                        .setConfigAndSystemProperty(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.toString(headless))) {
                    java.util.List<String> windowCalls = new java.util.ArrayList<>();
                    WebDriver.Window window = port(WebDriver.Window.class, (proxy, method, args) -> {
                        windowCalls.add(method.getName());
                        if ("setSize".equals(method.getName())) Assert.assertEquals(args[0], new org.openqa.selenium.Dimension(800, 600));
                        return null;
                    });
                    WebDriver.Timeouts timeouts = port(WebDriver.Timeouts.class, (proxy, method, args) -> {
                        Assert.assertEquals(args[0], java.time.Duration.ZERO, "Session startup must not introduce an implicit wait");
                        return proxy;
                    });
                    WebDriver.Options options = port(WebDriver.Options.class, (proxy, method, args) ->
                            "timeouts".equals(method.getName()) ? timeouts : window);
                    WebDriver configured = port(WebDriver.class, (proxy, method, args) -> {
                        if ("manage".equals(method.getName())) return options;
                        if ("getWindowHandles".equals(method.getName())) return Set.of("owned-window");
                        return null;
                    });
                    UiRuntimeOptions runtime = UiRuntimeOptions.load();
                    SeleniumBrowserSession session = new SeleniumBrowserSession(fullscreen
                            ? UIWindowConfiguration.fullscreen(800, 600, 100)
                            : UIWindowConfiguration.customSize(800, 600, 100), runtime, () -> configured);
                    Assert.assertEquals(windowCalls, java.util.List.of(fullscreen && !headless ? "maximize" : "setSize"));
                    java.lang.reflect.Method method = SeleniumChromeDriverFactory.class.getDeclaredMethod("chromeOptions");
                    method.setAccessible(true);
                    org.openqa.selenium.chrome.ChromeOptions chrome = (org.openqa.selenium.chrome.ChromeOptions)
                            method.invoke(new SeleniumChromeDriverFactory(runtime));
                    java.util.Map<?, ?> arguments = (java.util.Map<?, ?>) chrome.getCapability("goog:chromeOptions");
                    Assert.assertEquals(((java.util.List<?>) arguments.get("args")).contains("--headless=new"), headless);
                    Assert.assertNotNull(chrome.getCapability("goog:loggingPrefs"), "Network observation remains configured explicitly");
                    session.close();
                }
            }
        }
    }

    @Test
    public void aDriverCleanupFailureCannotSuppressItself() {
        Throwable primary = new IllegalStateException("Shared acquisition and cleanup failure");
        AtomicInteger quitCalls = new AtomicInteger();
        WebDriver driver = driver("window", primary, primary, quitCalls);
        try (AtfRuntimeConfigSandbox ignored = options()) {
            Assert.assertSame(Assert.expectThrows(primary.getClass(), () -> new SeleniumBrowserSession(
                    UIWindowConfiguration.customSize(800, 600, 100), UiRuntimeOptions.load(), () -> driver)), primary);
            Assert.assertEquals(primary.getSuppressed().length, 0);
            Assert.assertEquals(quitCalls.get(), 1);
        }
    }

    private static AtfRuntimeConfigSandbox options() {
        return AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_ENGINE_KEY, "selenium")
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_HEADLESS_KEY, "true")
                .setConfigAndSystemProperty(UiRuntimeOptions.CONSOLE_DETAILS_ENABLED_KEY, "false");
    }

    private static <T> T port(Class<T> type, InvocationHandler calls) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, calls));
    }

    private static WebDriver driver(String stage, Throwable primary, Throwable cleanup, AtomicInteger quitCalls) {
        WebDriver.Timeouts timeouts = port(WebDriver.Timeouts.class, (proxy, method, args) -> {
            failAt(stage, "timeouts", primary); return proxy;
        });
        WebDriver.Window window = port(WebDriver.Window.class, (proxy, method, args) -> {
            failAt(stage, "window", primary); return null;
        });
        WebDriver.Options options = port(WebDriver.Options.class, (proxy, method, args) ->
                "timeouts".equals(method.getName()) ? timeouts : window);
        return port(WebDriver.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "manage": return options;
                case "getWindowHandles": failAt(stage, "handles", primary); return Set.of("owned-window");
                case "quit": quitCalls.incrementAndGet(); rethrow(cleanup); return null;
                default: return null;
            }
        });
    }

    private static void failAt(String stage, String operation, Throwable failure) {
        if (stage.equals(operation)) rethrow(failure);
    }

    private static void rethrow(Throwable failure) {
        if (failure instanceof RuntimeException) throw (RuntimeException) failure;
        if (failure instanceof Error) throw (Error) failure;
    }

    @Test
    public void adapterClasspathDiscoversOnlyItsOwnEngineWithoutStartingBrowser() {
        Assert.assertEquals(UiEngineRegistry.defaults().supportedTypes(), Set.of(UiEngineType.SELENIUM));
        Assert.assertEquals(UiEngineRegistry.defaults().supportedValues(), "selenium");
        Assert.expectThrows(ClassNotFoundException.class,
                () -> Class.forName("com.microsoft.playwright.Page"));
    }

    @Test
    public void typedDevToolsExactlyMatchesThePinnedChromeMajorWithoutStartingBrowser() {
        Assert.assertEquals(new BuildInfo().getReleaseLabel(), "4.54.0");
        CdpInfo cdp = new CdpVersionFinder().match("158.0.8127.88")
                .orElseThrow(() -> new AssertionError("The pinned Chrome 158 requires a typed CDP provider."));
        Assert.assertEquals(cdp.getMajorVersion(), 158,
                "A nearest-version CDP fallback does not satisfy the pinned compatibility matrix.");
        Assert.assertEquals(cdp.getClass().getName(), "org.openqa.selenium.devtools.v158.v158CdpInfo");
    }
}
