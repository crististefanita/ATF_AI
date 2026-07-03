package com.ai.atf.core.support.contract.core.yaml;

import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class AtfCoreYamlContractClassLoader {
    private AtfCoreYamlContractClassLoader() {
    }

    static Class<?> load(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className, false, AtfCoreYamlContractManifestTest.class.getClassLoader());
        } catch (ClassNotFoundException firstFailure) {
            return loadFromContextOrCompiledOutput(className, firstFailure);
        }
    }

    private static Class<?> loadFromContextOrCompiledOutput(
            String className,
            ClassNotFoundException firstFailure
    ) throws ClassNotFoundException {
        try {
            return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException secondFailure) {
            try (URLClassLoader outputLoader = new URLClassLoader(compiledOutputUrls(),
                    AtfCoreYamlContractManifestTest.class.getClassLoader())) {
                return Class.forName(className, false, outputLoader);
            } catch (java.io.IOException outputFailure) {
                ClassNotFoundException enriched = new ClassNotFoundException(
                        "Could not load ATF YAML contract validator from test output: " + className,
                        secondFailure);
                enriched.addSuppressed(firstFailure);
                enriched.addSuppressed(outputFailure);
                throw enriched;
            }
        }
    }

    private static URL[] compiledOutputUrls() {
        List<URL> urls = new ArrayList<>();
        addIfDirectory(urls, AtfSourcePaths.TEST_CLASSES_ROOT);
        addIfDirectory(urls, AtfSourcePaths.MAIN_CLASSES_ROOT);
        return urls.toArray(URL[]::new);
    }

    private static void addIfDirectory(List<URL> urls, Path path) {
        if (!Files.isDirectory(path)) {
            return;
        }
        try {
            urls.add(path.toUri().toURL());
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Could not add compiled output to contract classpath: " + path, exception);
        }
    }
}
