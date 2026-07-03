package com.endava.ai.core.support.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfigManager {
    public static final String FRAMEWORK_PROPERTIES_RESOURCE = "framework.properties";
    public static final String MAIN_RESOURCES_DIRECTORY = "src/main/resources";
    public static final Path FRAMEWORK_PROPERTIES_SOURCE =
            Path.of(MAIN_RESOURCES_DIRECTORY, FRAMEWORK_PROPERTIES_RESOURCE);

    private static volatile Properties PROPERTIES;

    private ConfigManager() {
    }

    public static String get(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = properties().getProperty(key);
        }
        if (value == null) return defaultValue == null ? null : defaultValue.trim();
        value = value.trim();
        return value.isEmpty() ? (defaultValue == null ? null : defaultValue.trim()) : value;
    }

    public static String require(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            value = properties().getProperty(key);
        }
        if (value == null) {
            throw new IllegalStateException("Missing config key: " + key);
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalStateException("Config key is empty: " + key);
        }
        return value;
    }

    public static void set(String key, String value) {
        if (value == null) {
            properties().remove(key);
        } else {
            properties().setProperty(key, value.trim());
        }
    }

    public static void clear(String key) {
        properties().remove(key);
    }

    private static Properties properties() {
        if (PROPERTIES == null) {
            synchronized (ConfigManager.class) {
                if (PROPERTIES == null) {
                    PROPERTIES = load();
                }
            }
        }
        return PROPERTIES;
    }

    private static Properties load() {
        Properties props = new Properties();

        try (InputStream is = resolveConfigStream()) {
            props.load(is);
            return props;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + FRAMEWORK_PROPERTIES_RESOURCE, e);
        }
    }

    private static InputStream resolveConfigStream() throws Exception {
        InputStream is = ConfigManager.class
                    .getClassLoader()
                    .getResourceAsStream(FRAMEWORK_PROPERTIES_RESOURCE);

        if (is == null) {
            if (Files.exists(FRAMEWORK_PROPERTIES_SOURCE)) {
                is = Files.newInputStream(FRAMEWORK_PROPERTIES_SOURCE);
            }
        }

        if (is == null) {
            throw new IllegalStateException(
                    FRAMEWORK_PROPERTIES_RESOURCE + " not found. Checked classpath and " + MAIN_RESOURCES_DIRECTORY
            );
        }
        return is;
    }
}
