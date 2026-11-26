package com.ai.core.support.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ConfigManager {
    public static final String FRAMEWORK_PROPERTIES_RESOURCE = "framework.properties";

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

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, Boolean.toString(defaultValue));
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new IllegalStateException("Invalid boolean config key: " + key + "=" + value
                + "; expected true or false");
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
        String configuredFile = System.getProperty(FRAMEWORK_PROPERTIES_RESOURCE);
        if (configuredFile != null && !configuredFile.isBlank()
                && !FRAMEWORK_PROPERTIES_RESOURCE.equals(configuredFile.trim())) {
            Path file = Path.of(configuredFile.trim()).toAbsolutePath().normalize();
            if (!Files.isRegularFile(file)) {
                throw new IllegalStateException("Configuration file not found: " + file);
            }
            return Files.newInputStream(file);
        }
        InputStream is = ConfigManager.class
                    .getClassLoader()
                    .getResourceAsStream(FRAMEWORK_PROPERTIES_RESOURCE);



        if (is == null) {
            throw new IllegalStateException(
                    FRAMEWORK_PROPERTIES_RESOURCE + " not found on the classpath; set -Dframework.properties to an explicit file"
            );
        }
        return is;
    }
}
