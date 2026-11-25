package com.ai.atf.core.support.config;

import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManagerFileContractTest {
    private String previousFile;
    private String previousFixture;
    private String previousUiEngine;
    private Object previousProperties;
    private Field properties;
    private Path temporaryFile;

    @BeforeMethod(alwaysRun = true)
    public void isolateConfiguration() throws Exception {
        previousFile = System.getProperty("framework.properties");
        previousFixture = System.getProperty("atf.config.fixture");
        previousUiEngine = System.getProperty("ui.engine");
        temporaryFile = null;
        properties = ConfigManager.class.getDeclaredField("PROPERTIES");
        properties.setAccessible(true);
        previousProperties = properties.get(null);
        properties.set(null, null);
    }

    @AfterMethod(alwaysRun = true)
    public void restoreConfiguration() throws Exception {
        try {
            restoreProperty("framework.properties", previousFile);
            restoreProperty("atf.config.fixture", previousFixture);
            restoreProperty("ui.engine", previousUiEngine);
            if (properties != null) properties.set(null, previousProperties);
        } finally {
            if (temporaryFile != null) Files.deleteIfExists(temporaryFile);
            temporaryFile = null;
        }
    }

    @Test
    public void defaultResourceStillLoads() {
        System.setProperty("framework.properties", "framework.properties");
        System.clearProperty("ui.engine");
        Assert.assertEquals(ConfigManager.require("ui.engine"), "playwright");
    }

    @Test
    public void explicitFileSuppliesConfiguration() throws Exception {
        System.clearProperty("atf.config.fixture");
        temporaryFile = Files.createTempFile("atf-config-", ".properties");
        Files.writeString(temporaryFile, "atf.config.fixture=from-file\n");
        System.setProperty("framework.properties", temporaryFile.toString());
        Assert.assertEquals(ConfigManager.require("atf.config.fixture"), "from-file");
    }

    @Test
    public void systemPropertyOverridesExplicitFile() throws Exception {
        temporaryFile = Files.createTempFile("atf-config-", ".properties");
        Files.writeString(temporaryFile, "atf.config.fixture=from-file\n");
        System.setProperty("framework.properties", temporaryFile.toString());
        System.setProperty("atf.config.fixture", "from-system");
        Assert.assertEquals(ConfigManager.require("atf.config.fixture"), "from-system");
    }

    @Test
    public void missingExplicitFileFailsInsteadOfUsingDefaults() {
        System.clearProperty("ui.engine");
        System.setProperty("framework.properties", Path.of(System.getProperty("java.io.tmpdir"),
                "missing-atf-config-" + java.util.UUID.randomUUID() + ".properties").toString());
        Assert.expectThrows(IllegalStateException.class, () -> ConfigManager.require("ui.engine"));
    }

    @DataProvider(name = "snapshotInitialValues")
    public Object[][] snapshotInitialValues() {
        return new Object[][] {{null, null}, {"initial-system", "initial-config"}, {"", "initial-config"}};
    }

    @Test(dataProvider = "snapshotInitialValues")
    public void sandboxRestoresTheFirstSnapshotAcrossRepeatedWritesAndClears(String initialSystem, String initialConfig) {
        String configKey = "atf.config.snapshot.runtime";
        String previousConfigOverride = System.getProperty(configKey);
        System.setProperty("framework.properties", "framework.properties");
        restoreProperty("atf.config.fixture", initialSystem);
        System.clearProperty(configKey);
        try {
            if (initialConfig == null) ConfigManager.clear(configKey);
            else ConfigManager.set(configKey, initialConfig);

            try (AtfRuntimeConfigSandbox settings = AtfRuntimeConfigSandbox.open()
                    .setSystemProperty("atf.config.fixture", "first").setConfig(configKey, "first")) {
                Assert.assertEquals(System.getProperty("atf.config.fixture"), "first");
                Assert.assertEquals(ConfigManager.get(configKey, null), "first");
                settings.clearSystemProperty("atf.config.fixture").clearConfig(configKey);
                Assert.assertNull(System.getProperty("atf.config.fixture"));
                Assert.assertNull(ConfigManager.get(configKey, null));
                settings.setSystemProperty("atf.config.fixture", "second").setConfig(configKey, "second");
                settings.clearSystemProperty("atf.config.fixture").clearConfig(configKey);
                settings.setSystemProperty("atf.config.fixture", "third").setConfig(configKey, "third");
                Assert.assertEquals(System.getProperty("atf.config.fixture"), "third");
                Assert.assertEquals(ConfigManager.get(configKey, null), "third");
            }

            Assert.assertEquals(System.getProperty("atf.config.fixture"), initialSystem,
                    "Restore the original system property exactly, including absence or an empty value.");
            Assert.assertEquals(ConfigManager.get(configKey, null), initialConfig,
                    "The original config snapshot must survive every later write and clear in the scope.");
        } finally {
            restoreProperty(configKey, previousConfigOverride);
        }
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }
}
