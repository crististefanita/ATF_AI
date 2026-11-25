package com.ai.core.reporting.spi;

import com.ai.core.reporting.ReportLogger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportingBackendLoaderTest {

    private static final AtomicInteger CONSTRUCTIONS = new AtomicInteger();

    @Test
    public void absenceIsAValidCoreOnlyClasspath() {
        Assert.assertTrue(ReportingBackendLoader.load(withDescriptor("")).isEmpty());
    }

    @Test
    public void oneProviderIsDiscoveredThroughItsServiceDescriptor() {
        ReportingBackend backend = ReportingBackendLoader.load(
                withDescriptor(FirstBackend.class.getName())).orElseThrow();

        Assert.assertEquals(backend.getClass(), FirstBackend.class);
    }

    @Test
    public void duplicateProvidersFailBeforeEitherBackendIsConstructed() {
        CONSTRUCTIONS.set(0);

        IllegalStateException failure = Assert.expectThrows(IllegalStateException.class,
                () -> ReportingBackendLoader.load(withDescriptor(
                        SecondBackend.class.getName() + "\n" + FirstBackend.class.getName())));

        Assert.assertTrue(failure.getMessage().contains(FirstBackend.class.getName()));
        Assert.assertTrue(failure.getMessage().contains(SecondBackend.class.getName()));
        Assert.assertTrue(failure.getMessage().contains("exactly one"));
        Assert.assertEquals(CONSTRUCTIONS.get(), 0,
                "An ambiguous classpath must not initialize either reporting backend.");
    }

    private static ClassLoader withDescriptor(String descriptor) {
        return new ClassLoader(ReportingBackend.class.getClassLoader()) {
            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                if (!name.equals("META-INF/services/" + ReportingBackend.class.getName())) {
                    return super.getResources(name);
                }
                URL resource = new URL(null, "memory:reporting-backends", new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL url) {
                        return new URLConnection(url) {
                            @Override
                            public void connect() {
                            }

                            @Override
                            public ByteArrayInputStream getInputStream() {
                                return new ByteArrayInputStream(descriptor.getBytes(StandardCharsets.UTF_8));
                            }
                        };
                    }
                });
                return Collections.enumeration(Collections.singletonList(resource));
            }
        };
    }

    public static final class FirstBackend extends StubBackend {
        public FirstBackend() {
            CONSTRUCTIONS.incrementAndGet();
        }
    }

    public static final class SecondBackend extends StubBackend {
        public SecondBackend() {
            CONSTRUCTIONS.incrementAndGet();
        }
    }

    public abstract static class StubBackend implements ReportingBackend {
        @Override public void beginSuite() { }
        @Override public void endSuite() { }
        @Override public ReportLogger getLogger(boolean required) { return null; }
        @Override public void setLoggerForTests(ReportLogger logger) { }
        @Override public ReportLogger testLoggerForTests() { return null; }
        @Override public void reset() { }
        @Override public boolean isSuiteActive() { return false; }
        @Override public long generation() { return 0; }
        @Override public ReportingLifecycleListener createListener() { return new ReportingLifecycleListener() { }; }
        @Override public void resetListenerStateForTests() { }
    }
}
