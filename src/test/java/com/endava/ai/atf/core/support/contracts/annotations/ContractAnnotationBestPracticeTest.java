package com.endava.ai.atf.core.support.contracts.annotations;

import com.endava.ai.core.support.contract.AtfContract;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.AtfContractSuite;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.AtfCoreCheck;
import com.endava.ai.core.support.contract.core.AtfCoreContract;
import com.endava.ai.core.support.contract.failure.AtfExpectedFailureContract;
import com.endava.ai.core.support.contract.failure.AtfExpectedFailureContractRef;
import com.endava.ai.skills.atlas.contract.AtlasContractSuite;
import com.endava.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.RequiresArtifacts;
import com.endava.ai.skills.mcp.contract.ValidatesMcpContract;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Pattern;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ANNOTATION_HYGIENE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ANNOTATION_HYGIENE_CATALOG)
@Listeners(TestListener.class)
public class ContractAnnotationBestPracticeTest {
    private static final Path MAIN_JAVA = AtfSourcePaths.MAIN_JAVA_ROOT;
    private static final Path TEST_JAVA = AtfSourcePaths.TEST_JAVA_ROOT;
    private static final Path LEGACY_MCP_CHECK = AtfSourcePaths.mainJava("com.endava.ai.skills.mcp.contract.McpCheck");
    private static final Path LEGACY_MCP_SUITE = AtfSourcePaths.mainJava("com.endava.ai.skills.mcp.contract.McpSuite");
    private static final Pattern PUBLIC_ANNOTATION_DECLARATION = Pattern.compile("(?m)^public\\s+@interface\\s+");
    private static final Pattern ATF_CONTRACT_ANNOTATION_USAGE = Pattern.compile(
            "(?m)^\\s*@(AtfContract|AtfContractRef|AtfContractSuite|AtfContractSuiteRef"
                    + "|AtfExpectedFailureContract|AtfExpectedFailureContractRef)\\b");
    private static final Pattern ATF_CONTRACT_REF_USAGE = Pattern.compile("(?m)^\\s*@AtfContractRef\\(");
    private static final Pattern ATF_CONTRACT_SUITE_REF_USAGE = Pattern.compile("(?m)^\\s*@AtfContractSuiteRef\\(");
    private static final Pattern ATF_CONTRACT_DIRECT_USAGE = Pattern.compile("(?m)^\\s*@AtfContract\\(");
    private static final Pattern ATF_CONTRACT_SUITE_DIRECT_USAGE = Pattern.compile("(?m)^\\s*@AtfContractSuite\\(");
    private static final Pattern ATF_CONTRACT_STEPS_CONTRACT_FOR_USAGE = Pattern.compile(
            "\\bAtfContractSteps\\.contractFor\\s*\\(");
    private static final Pattern INLINE_ATF_YAML_REF_USAGE = Pattern.compile(
            "@(AtfContractRef|AtfContractSuiteRef|AtfExpectedFailureContractRef)\\(\"");
    private static final int MIN_REAL_ATF_YAML_REF_USAGES = 290;
    private static final List<AnnotationSpec> CONTRACT_ANNOTATIONS = List.of(
            spec(McpContract.class, ElementType.TYPE),
            spec(ValidatesMcpContract.class, ElementType.TYPE, ElementType.METHOD),
            spec(RequiresArtifacts.class, ElementType.TYPE, ElementType.METHOD),
            spec(McpCheckRef.class, ElementType.METHOD),
            spec(McpSuiteRef.class, ElementType.TYPE),
            spec(NoFrameworkReporting.class, ElementType.TYPE),
            spec(AtlasContractSuite.class, ElementType.TYPE),
            spec(AtlasContractSuiteRef.class, ElementType.TYPE),
            spec(AtfContract.class, ElementType.TYPE, ElementType.METHOD),
            spec(AtfContractRef.class, ElementType.TYPE, ElementType.METHOD),
            spec(AtfContractSuite.class, ElementType.TYPE),
            spec(AtfContractSuiteRef.class, ElementType.TYPE),
            spec(AtfCoreContract.class, ElementType.TYPE),
            spec(AtfCoreCheck.class, ElementType.TYPE),
            spec(AtfExpectedFailureContract.class, ElementType.METHOD),
            spec(AtfExpectedFailureContractRef.class, ElementType.METHOD));

    @Test
    public void contractAnnotationsDeclareRuntimeRetentionTargetAndDocumentation() {
        for (AnnotationSpec spec : CONTRACT_ANNOTATIONS) {
            Class<? extends Annotation> type = spec.type();
            Assert.assertTrue(type.isAnnotationPresent(Documented.class), type.getName() + " must be @Documented");

            Retention retention = type.getAnnotation(Retention.class);
            Assert.assertNotNull(retention, type.getName() + " must declare @Retention");
            Assert.assertEquals(retention.value(), RetentionPolicy.RUNTIME,
                    type.getName() + " must be available to report/contract reflection");

            Target target = type.getAnnotation(Target.class);
            Assert.assertNotNull(target, type.getName() + " must declare @Target");
            Assert.assertEquals(targetValues(target), spec.targets(), type.getName() + " target drift");
        }
    }

    @Test
    public void annotationBestPracticeCatalogCoversEveryPublicContractAnnotation() {
        Set<String> declared = CONTRACT_ANNOTATIONS.stream()
                .map(spec -> spec.type().getName())
                .collect(Collectors.toUnmodifiableSet());
        Set<String> discovered = Stream.of(MAIN_JAVA, TEST_JAVA)
                .flatMap(ContractAnnotationBestPracticeTest::javaFiles)
                .filter(ContractAnnotationBestPracticeTest::containsPublicAnnotation)
                .map(ContractAnnotationBestPracticeTest::className)
                .collect(Collectors.toUnmodifiableSet());

        Assert.assertEquals(declared, discovered,
                "Every public contract annotation should be covered by this best-practice catalog.");
    }

    @Test
    public void legacyInlineMcpMetadataAnnotationsStayRemoved() {
        Assert.assertFalse(Files.exists(LEGACY_MCP_CHECK),
                "MCP check narratives must stay YAML-backed via McpCheckRef; do not restore McpCheck.java.");
        Assert.assertFalse(Files.exists(LEGACY_MCP_SUITE),
                "MCP suite narratives must stay YAML-backed via McpSuiteRef; do not restore McpSuite.java.");
    }

    @Test
    public void publicContractAnnotationsDoNotUseDeprecatedMarker() {
        List<String> deprecatedAnnotations = Stream.of(MAIN_JAVA, TEST_JAVA)
                .flatMap(ContractAnnotationBestPracticeTest::javaFiles)
                .filter(ContractAnnotationBestPracticeTest::containsPublicAnnotation)
                .filter(ContractAnnotationBestPracticeTest::containsDeprecatedMarker)
                .map(Path::toString)
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(deprecatedAnnotations.isEmpty(),
                "Public contract annotations should be removed or replaced, not kept as @Deprecated API: "
                        + deprecatedAnnotations);
    }

    @Test
    public void atfYamlRefsAreExclusiveWithInlineNarrativeAnnotations() {
        List<String> ambiguous = new ArrayList<>();
        for (Class<?> type : loadAtfContractTypes()) {
            if (type.isAnnotationPresent(AtfContract.class) && type.isAnnotationPresent(AtfContractRef.class)) {
                ambiguous.add(type.getName() + " declares both @AtfContract and @AtfContractRef");
            }
            if (type.isAnnotationPresent(AtfContractSuite.class) && type.isAnnotationPresent(AtfContractSuiteRef.class)) {
                ambiguous.add(type.getName() + " declares both @AtfContractSuite and @AtfContractSuiteRef");
            }
            for (Method method : type.getDeclaredMethods()) {
                if (method.isAnnotationPresent(AtfContract.class) && method.isAnnotationPresent(AtfContractRef.class)) {
                    ambiguous.add(type.getName() + "#" + method.getName()
                            + " declares both @AtfContract and @AtfContractRef");
                }
                if (method.isAnnotationPresent(AtfExpectedFailureContract.class)
                        && method.isAnnotationPresent(AtfExpectedFailureContractRef.class)) {
                    ambiguous.add(type.getName() + "#" + method.getName()
                            + " declares both @AtfExpectedFailureContract and @AtfExpectedFailureContractRef");
                }
            }
        }
        Assert.assertTrue(ambiguous.isEmpty(),
                "Use either inline ATF narrative annotations or YAML-backed refs, never both on the same target: "
                        + ambiguous);
    }

    @Test
    public void atfYamlRefsResolveToContractNarrativeCatalogRows() {
        List<String> missing = new ArrayList<>();
        for (Class<?> type : loadAtfContractTypes()) {
            AtfContractRef classContractRef = type.getAnnotation(AtfContractRef.class);
            if (classContractRef != null && !isKnownContractRef(classContractRef.value())) {
                missing.add(type.getName() + " -> " + classContractRef.value());
            }
            AtfContractSuiteRef suiteRef = type.getAnnotation(AtfContractSuiteRef.class);
            if (suiteRef != null && !isKnownSuiteRef(suiteRef.value())) {
                missing.add(type.getName() + " -> " + suiteRef.value());
            }
            for (Method method : type.getDeclaredMethods()) {
                AtfContractRef methodContractRef = method.getAnnotation(AtfContractRef.class);
                if (methodContractRef != null && !isKnownContractRef(methodContractRef.value())) {
                    missing.add(type.getName() + "#" + method.getName() + " -> " + methodContractRef.value());
                }
                AtfExpectedFailureContractRef expectedFailureRef =
                        method.getAnnotation(AtfExpectedFailureContractRef.class);
                if (expectedFailureRef != null && !isKnownExpectedFailureRef(expectedFailureRef.value())) {
                    missing.add(type.getName() + "#" + method.getName() + " -> " + expectedFailureRef.value());
                }
            }
        }
        Assert.assertTrue(missing.isEmpty(),
                "ATF YAML ref annotations must resolve to contract-narratives.yml rows of the correct kind: "
                        + missing);
    }

    @Test
    public void atfYamlRefsAreUsedByRealContractTestsNotOnlyFixtures() {
        List<String> realRefUsages = javaFiles(TEST_JAVA)
                .filter(path -> AtfSourcePaths.isUnderPackage(path, AtfSourcePaths.ATF_ROOT_PACKAGE))
                .filter(path -> !path.getFileName().toString().equals("AtfCoreYamlContractNarrativeManifestTest.java"))
                .flatMap(ContractAnnotationBestPracticeTest::atfYamlRefUsageRows)
                .sorted()
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(realRefUsages.size() >= MIN_REAL_ATF_YAML_REF_USAGES,
                "Move bulky @AtfContract/@AtfContractSuite narratives to contract-narratives.yml via refs; "
                        + "real ATF test refs=" + realRefUsages);
    }

    @Test
    public void atfYamlRefsUseNarrativeIdConstantsInsteadOfInlineStrings() {
        List<String> inlineRefs = javaFiles(TEST_JAVA)
                .filter(path -> AtfSourcePaths.isUnderPackage(path, AtfSourcePaths.ATF_ROOT_PACKAGE))
                .filter(path -> INLINE_ATF_YAML_REF_USAGE.matcher(readSource(path)).find())
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(inlineRefs.isEmpty(),
                "ATF YAML ref annotations must use AtfContractNarrativeIds constants, not inline strings: "
                        + inlineRefs);
    }

    @Test
    public void atfYamlBackedContractTestsUseYamlAwareStepDetails() {
        List<String> staleHelperUsages = javaFiles(TEST_JAVA)
                .filter(path -> AtfSourcePaths.isUnderPackage(path, AtfSourcePaths.ATF_ROOT_PACKAGE))
                .filter(ContractAnnotationBestPracticeTest::usesAtfYamlContractRef)
                .filter(ContractAnnotationBestPracticeTest::usesInlineOnlyContractStepLookup)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(staleHelperUsages.isEmpty(),
                "Tests using @AtfContractRef must call AtfContractSteps.contractDetailsFor(...), "
                        + "not the inline-only contractFor(...): " + staleHelperUsages);
    }

    @Test
    public void inlineAtfContractNarrativesStayFullyMigratedToYamlRefs() {
        int directContracts = countAnnotationUsages(ATF_CONTRACT_DIRECT_USAGE);
        int directSuites = countAnnotationUsages(ATF_CONTRACT_SUITE_DIRECT_USAGE);

        Assert.assertEquals(directContracts, 0,
                "Do not add bulky @AtfContract narratives; add YAML rows and use @AtfContractRef instead. "
                        + "directContracts=" + directContracts);
        Assert.assertEquals(directSuites, 0,
                "Do not add bulky @AtfContractSuite narratives; add YAML rows and use @AtfContractSuiteRef instead. "
                        + "directSuites=" + directSuites);
    }

    private static AnnotationSpec spec(Class<? extends Annotation> type, ElementType... targets) {
        return new AnnotationSpec(type, Set.of(targets));
    }

    private static Set<ElementType> targetValues(Target target) {
        return Arrays.stream(target.value()).collect(Collectors.toUnmodifiableSet());
    }

    private static Stream<Path> javaFiles(Path root) {
        try {
            return Files.walk(root)
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(path -> AtfSourcePaths.isUnderPackage(path, AtfSourcePaths.AI_ROOT_PACKAGE));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot scan Java annotation files under " + root, e);
        }
    }

    private static boolean containsPublicAnnotation(Path path) {
        return PUBLIC_ANNOTATION_DECLARATION.matcher(readSource(path)).find() && isContractAnnotationPath(path);
    }

    private static boolean containsDeprecatedMarker(Path path) {
        return readSource(path).contains("@Deprecated");
    }

    private static String readSource(Path path) {
        try {
            return Files.readString(path);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot inspect source " + path, e);
        }
    }

    private static boolean isContractAnnotationPath(Path path) {
        String normalized = AtfSourcePaths.unixPath(path);
        return normalized.contains("/com/endava/ai/skills/mcp/contract/")
                || normalized.contains("/com/endava/ai/skills/atlas/contract/")
                || normalized.contains("/com/endava/ai/core/listener/")
                || normalized.contains("/com/endava/ai/core/reporting/listener/")
                || normalized.contains("/com/endava/ai/core/support/contract/")
                || normalized.contains("/com/endava/ai/atf/contract/");
    }

    private static String className(Path path) {
        Path root = path.startsWith(MAIN_JAVA) ? MAIN_JAVA : TEST_JAVA;
        return AtfSourcePaths.className(root, path);
    }

    private static List<Class<?>> loadAtfContractTypes() {
        List<Class<?>> topLevelTypes = Stream.of(MAIN_JAVA, TEST_JAVA)
                .flatMap(ContractAnnotationBestPracticeTest::javaFiles)
                .filter(ContractAnnotationBestPracticeTest::mentionsAtfContractAnnotation)
                .map(ContractAnnotationBestPracticeTest::loadClass)
                .collect(Collectors.toUnmodifiableList());
        List<Class<?>> allTypes = new ArrayList<>();
        topLevelTypes.forEach(type -> addTypeAndNestedTypes(type, allTypes));
        return List.copyOf(allTypes);
    }

    private static boolean mentionsAtfContractAnnotation(Path path) {
        return ATF_CONTRACT_ANNOTATION_USAGE.matcher(readSource(path)).find();
    }

    private static Stream<String> atfYamlRefUsageRows(Path path) {
        try {
            String source = Files.readString(path);
            int contractRefs = (int) ATF_CONTRACT_REF_USAGE.matcher(source).results().count();
            int suiteRefs = (int) ATF_CONTRACT_SUITE_REF_USAGE.matcher(source).results().count();
            List<String> rows = new ArrayList<>();
            for (int index = 0; index < contractRefs; index++) {
                rows.add(path + " @AtfContractRef");
            }
            for (int index = 0; index < suiteRefs; index++) {
                rows.add(path + " @AtfContractSuiteRef");
            }
            return rows.stream();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot inspect ATF YAML ref source " + path, e);
        }
    }

    private static boolean usesAtfYamlContractRef(Path path) {
        String source = readSource(path);
        return ATF_CONTRACT_REF_USAGE.matcher(source).find()
                || ATF_CONTRACT_SUITE_REF_USAGE.matcher(source).find();
    }

    private static boolean usesInlineOnlyContractStepLookup(Path path) {
        return ATF_CONTRACT_STEPS_CONTRACT_FOR_USAGE.matcher(readSource(path)).find();
    }

    private static int countAnnotationUsages(Pattern pattern) {
        return javaFiles(TEST_JAVA)
                .mapToInt(path -> countMatches(path, pattern))
                .sum();
    }

    private static int countMatches(Path path, Pattern pattern) {
        return (int) pattern.matcher(readSource(path)).results().count();
    }

    private static Class<?> loadClass(Path path) {
        try {
            return loadContractClass(className(path));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load ATF contract class for " + path, e);
        }
    }

    private static Class<?> loadContractClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className, false, ContractAnnotationBestPracticeTest.class.getClassLoader());
        } catch (ClassNotFoundException firstFailure) {
            try {
                return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException secondFailure) {
                try (URLClassLoader outputLoader = new URLClassLoader(compiledOutputUrls(),
                        ContractAnnotationBestPracticeTest.class.getClassLoader())) {
                    return Class.forName(className, false, outputLoader);
                } catch (java.io.IOException outputFailure) {
                    ClassNotFoundException enriched = new ClassNotFoundException(
                            "Could not load ATF contract class from test output: " + className,
                            secondFailure);
                    enriched.addSuppressed(firstFailure);
                    enriched.addSuppressed(outputFailure);
                    throw enriched;
                }
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

    private static void addTypeAndNestedTypes(Class<?> type, List<Class<?>> target) {
        target.add(type);
        try {
            for (Class<?> nestedType : type.getDeclaredClasses()) {
                addTypeAndNestedTypes(nestedType, target);
            }
        } catch (NoClassDefFoundError missingNestedType) {
            // Some subset Surefire runs compile or expose only the top-level test class.
            // The contract catalog is top-level driven; missing private helpers must not
            // make annotation metadata checks fail before they inspect real contracts.
        }
    }

    private static boolean isKnownContractRef(String id) {
        try {
            AtfCoreYamlContractCatalog.requireContractNarrativeContract(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isKnownSuiteRef(String id) {
        try {
            AtfCoreYamlContractCatalog.requireContractNarrativeSuite(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isKnownExpectedFailureRef(String id) {
        try {
            AtfCoreYamlContractCatalog.requireContractNarrativeExpectedFailure(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
