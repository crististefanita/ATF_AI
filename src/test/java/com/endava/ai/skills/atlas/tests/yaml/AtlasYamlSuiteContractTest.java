package com.endava.ai.skills.atlas.tests.yaml;

import com.endava.ai.skills.atlas.contract.AtlasContractArtifacts;
import com.endava.ai.skills.atlas.contract.AtlasContractProfile;
import com.endava.ai.skills.atlas.contract.AtlasContractSuiteMetadata;
import com.endava.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.endava.ai.skills.atlas.contract.AtlasSuiteIds;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlSuiteCatalog;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlSuiteSpec;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listeners(TestListener.class)
@AtlasContractSuiteRef(AtlasSuiteIds.YAML_SUITE_CATALOG)
public class AtlasYamlSuiteContractTest {
    private static final Path ATLAS_TEST_ROOT = AtfSourcePaths.TEST_ATLAS_TESTS_ROOT;
    private static final Set<AtlasContractProfile> KNOWN_PROFILES = EnumSet.allOf(AtlasContractProfile.class);
    private static final Path HTML_CONTRACT_OWNERSHIP = Path.of(
            "docs", "prompts", "html-contract-ownership.yml");
    private static final Pattern INLINE_ATLAS_SUITE = Pattern.compile("(?m)^\\s*@AtlasContractSuite\\(");
    private static final Pattern ATLAS_SUITE_REF_USAGE = Pattern.compile("(?m)^\\s*@AtlasContractSuiteRef\\(");
    private static final Pattern INLINE_ATLAS_SUITE_REF = Pattern.compile("@AtlasContractSuiteRef\\(\"[^\"]+\"\\)");

    @Test
    public void yamlSuiteSpecsHaveRequiredSchemaAndDefaultAtlasArtifacts() {
        Assert.assertFalse(AtlasYamlSuiteCatalog.suites().isEmpty());
        Assert.assertTrue(AtlasYamlSuiteCatalog.duplicateIds().isEmpty());

        for (AtlasYamlSuiteSpec suite : AtlasYamlSuiteCatalog.suites()) {
            Assert.assertTrue(suite.id().startsWith("atlas.suite."), "Unexpected Atlas suite id: " + suite.id());
            Assert.assertTrue(KNOWN_PROFILES.contains(suite.profile()),
                    "Invalid Atlas suite profile for " + suite.id() + ": " + suite.profile());
            Assert.assertEquals(suite.htmlOwner(), AtlasContractArtifacts.HTML_OWNER,
                    "Atlas suite htmlOwner should use the shared artifact constant: " + suite.id());
            Assert.assertEquals(suite.artifact(), AtlasContractArtifacts.ATLAS_HTML,
                    "Atlas suite artifact should use the shared artifact constant: " + suite.id());
            Assert.assertEquals(suite.sourceRoot(), AtlasContractArtifacts.INPUT_SOURCE_ROOT,
                    "Atlas suite sourceRoot should use the shared artifact constant: " + suite.id());
            Assert.assertFalse(suite.purpose().isBlank(), "Missing Atlas suite purpose for " + suite.id());
        }
    }

    @Test
    public void atlasHtmlOwnerIsDeclaredAsAtlasOwnedPromptContract() throws IOException {
        String atlasHtmlOwner = relativePromptContract(AtlasContractArtifacts.HTML_OWNER);
        Map<String, String> atlasContract = htmlOwnershipContracts().stream()
                .filter(entry -> atlasHtmlOwner.equals(entry.get("file")))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Missing Atlas HTML owner in " + HTML_CONTRACT_OWNERSHIP + ": " + atlasHtmlOwner));

        Assert.assertEquals(atlasContract.get("owner"), "Atlas",
                "business-test-atlas-reference.html must stay Atlas-owned in the prompt ownership manifest.");
        Assert.assertTrue(scalarSet(atlasContract.get("appliesTo")).contains("Atlas"),
                "Atlas prompt owner must apply to Atlas.");
        Assert.assertEquals(atlasContract.get("usage"), "referenced",
                "Atlas htmlOwner must be a referenced prompt contract, not an informational orphan.");
    }

    @Test
    public void atlasSuiteIdConstantsStaySynchronizedWithYamlCatalog() {
        Set<String> yamlIds = AtlasYamlSuiteCatalog.suites().stream()
                .map(AtlasYamlSuiteSpec::id)
                .collect(Collectors.toSet());
        Set<String> constantIds = Arrays.stream(AtlasSuiteIds.class.getDeclaredFields())
                .filter(field -> field.getType().equals(String.class))
                .filter(AtlasYamlSuiteContractTest::isPublicStaticFinal)
                .map(AtlasYamlSuiteContractTest::constantValue)
                .collect(Collectors.toSet());

        Assert.assertEquals(constantIds, yamlIds,
                "AtlasSuiteIds must stay synchronized with skills/atlas/contracts/suites.yml.");
    }

    @Test
    public void atlasContractTestSuitesUseYamlBackedSuiteRefs() throws Exception {
        List<Path> sources = atlasTestSourceList();

        List<String> inlineSuites = sources.stream()
                .filter(path -> INLINE_ATLAS_SUITE.matcher(source(path)).find())
                .map(Path::toString)
                .collect(Collectors.toList());
        Assert.assertTrue(inlineSuites.isEmpty(),
                "Atlas test suite narratives must use @AtlasContractSuiteRef + skills/atlas/contracts/suites.yml: "
                        + inlineSuites);

        List<String> missingRefs = sources.stream()
                .filter(path -> source(path).contains("extends AtlasContractTestSupport"))
                .filter(path -> !ATLAS_SUITE_REF_USAGE.matcher(source(path)).find())
                .map(Path::toString)
                .collect(Collectors.toList());
        Assert.assertTrue(missingRefs.isEmpty(), "Atlas contract test support classes missing suite refs: " + missingRefs);

        List<String> inlineSuiteRefs = sources.stream()
                .filter(path -> INLINE_ATLAS_SUITE_REF.matcher(source(path)).find())
                .map(Path::toString)
                .collect(Collectors.toList());
        Assert.assertTrue(inlineSuiteRefs.isEmpty(),
                "Atlas suite refs must use AtlasSuiteIds constants, not inline strings: " + inlineSuiteRefs);
    }

    @Test
    public void atlasSuiteMetadataResolverSupportsYamlBackedRefs() throws Exception {
        List<Class<?>> atlasSuites = atlasTestSourceList().stream()
                .filter(path -> ATLAS_SUITE_REF_USAGE.matcher(source(path)).find())
                .map(AtlasYamlSuiteContractTest::classFor)
                .collect(Collectors.toList());

        for (Class<?> suite : atlasSuites) {
            AtlasContractSuiteMetadata metadata = AtlasContractSuiteMetadata.require(suite);
            Assert.assertTrue(KNOWN_PROFILES.contains(metadata.profile()),
                    "Atlas suite metadata profile must resolve for " + suite.getName());
            Assert.assertTrue(knownAtlasSuite(metadata.suiteId()),
                    "Atlas suite metadata id must resolve through suites.yml for " + suite.getName());
            Assert.assertFalse(metadata.purpose().isBlank(),
                    "Atlas suite metadata purpose must resolve for " + suite.getName());
            Assert.assertEquals(metadata.artifact(), AtlasContractArtifacts.ATLAS_HTML,
                    "Atlas suite metadata artifact must resolve through the shared default for " + suite.getName());
        }
    }

    private static List<Path> atlasTestSourceList() throws Exception {
        try (Stream<Path> stream = Files.walk(ATLAS_TEST_ROOT)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(AtfSourcePaths::isJavaSource)
                    .collect(Collectors.toList());
        }
    }

    private static boolean knownAtlasSuite(String id) {
        try {
            AtlasYamlSuiteCatalog.require(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }

    private static String constantValue(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read Atlas suite id constant " + field.getName(), e);
        }
    }

    private static String source(Path path) {
        try {
            return Files.readString(path);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }

    private static List<Map<String, String>> htmlOwnershipContracts() throws IOException {
        return SimpleYamlContractLoader.loadText(
                Files.readString(HTML_CONTRACT_OWNERSHIP),
                HTML_CONTRACT_OWNERSHIP.toString().replace('\\', '/'));
    }

    private static String fileName(String path) {
        int slash = path.replace('\\', '/').lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String relativePromptContract(String path) {
        String normalized = path.replace('\\', '/');
        String marker = "docs/prompts/html_ro/";
        return normalized.startsWith(marker) ? normalized.substring(marker.length()) : fileName(normalized);
    }

    private static Set<String> scalarSet(String commaSeparated) {
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Class<?> classFor(Path path) {
        String className = AtfSourcePaths.className(AtfSourcePaths.TEST_JAVA_ROOT, path);
        try {
            return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load Atlas test class " + className, e);
        }
    }
}
