package com.endava.ai.core.support.contract.ids;

import com.endava.ai.core.support.config.ConfigManager;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AtfRuntimeConfigSandbox implements AutoCloseable {

    private final Map<String, String> configValues = new LinkedHashMap<>();
    private final Map<String, String> systemProperties = new LinkedHashMap<>();
    private boolean closed;

    private AtfRuntimeConfigSandbox() {
    }

    public static AtfRuntimeConfigSandbox open() {
        return new AtfRuntimeConfigSandbox();
    }

    public AtfRuntimeConfigSandbox setConfig(String key, String value) {
        rememberConfig(key);
        ConfigManager.set(key, value);
        return this;
    }

    public AtfRuntimeConfigSandbox clearConfig(String key) {
        rememberConfig(key);
        ConfigManager.clear(key);
        return this;
    }

    public AtfRuntimeConfigSandbox setSystemProperty(String key, String value) {
        rememberSystemProperty(key);
        System.setProperty(key, value);
        return this;
    }

    public AtfRuntimeConfigSandbox setConfigAndSystemProperty(String key, String value) {
        return setConfig(key, value)
                .setSystemProperty(key, value);
    }

    public AtfRuntimeConfigSandbox clearSystemProperty(String key) {
        rememberSystemProperty(key);
        System.clearProperty(key);
        return this;
    }

    private void rememberConfig(String key) {
        configValues.putIfAbsent(key, ConfigManager.get(key, null));
    }

    private void rememberSystemProperty(String key) {
        systemProperties.putIfAbsent(key, System.getProperty(key));
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        configValues.forEach(AtfRuntimeConfigSandbox::restoreConfig);
        systemProperties.forEach(AtfRuntimeConfigSandbox::restoreSystemProperty);
    }

    private static void restoreConfig(String key, String value) {
        if (value == null) {
            ConfigManager.clear(key);
        } else {
            ConfigManager.set(key, value);
        }
    }

    private static void restoreSystemProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
