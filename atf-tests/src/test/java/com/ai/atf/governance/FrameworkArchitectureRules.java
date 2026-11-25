package com.ai.atf.governance;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import org.w3c.dom.Element;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Structural guards complement native and browser behavior tests. */
final class FrameworkArchitectureRules {
    static final String UI_SPI = "com.ai.ui.core.engine.UiEngineProvider";
    static final String REPORTING_SPI = "com.ai.core.reporting.spi.ReportingBackend";
    static final List<String> MAINTAINED_SUITES = List.of(
            "framework-local.xml", "framework-aggregate.xml", "framework-browser.xml",
            "framework-allure.xml", "framework-api.xml", "framework-ui.xml", "framework-reporting.xml");
    // Exact manual-only exclusions: additions and removals require an explicit policy review.
    static final Map<String, String> SUITE_EXEMPTIONS = Map.ofEntries(
            Map.entry("com.ai.api.tests.PositiveUserTests", "manual live API writes"),
            Map.entry("com.ai.atf.api.tests.EndToEndUserLifecycleTests", "manual live API lifecycle"),
            Map.entry("com.ai.atf.api.tests.NegativeUserTests", "manual live API writes"),
            Map.entry("com.ai.atf.api.lifecycle.BeforeClass_BeforeMethod_Test", "manual live API lifecycle"),
            Map.entry("com.ai.atf.api.lifecycle.BeforeClassMethod_After", "manual live API lifecycle"),
            Map.entry("com.ai.atf.api.lifecycle.BeforeTest_BeforeMethod_Test", "manual live API lifecycle"),
            Map.entry("com.ai.atf.api.lifecycle.BeforeTest_TestOne_AfterTest", "manual live API lifecycle"),
            Map.entry("com.ai.atf.api.lifecycle.BeforeTestOne_TestOne", "manual live API lifecycle"),
            Map.entry("com.ai.atf.api.lifecycle.BeforeTestOne_TestTwo", "manual live API lifecycle"),
            Map.entry("com.ai.atf.ui.RegistrationTests", "manual live browser journey"),
            Map.entry("com.ai.ui.tests.AccountAuthenticatedAreaTests", "manual authenticated browser journey"),
            Map.entry("com.ai.ui.tests.ProfileContactAddressMemoryTests", "manual authenticated browser journey"),
            Map.entry("com.ai.atf.core.reporting.failurefixtures.AfterMethodFailureFixture", "nested failure fixture"),
            Map.entry("com.ai.atf.core.reporting.failurefixtures.BeforeMethodFailureFixture", "nested failure fixture"),
            Map.entry("com.ai.atf.core.reporting.failurefixtures.LongReferenceFailureSignatureFixture", "nested failure fixture"),
            Map.entry("com.ai.atf.core.reporting.failurefixtures.RepeatedHttpStatusFailureSignatureFixture", "nested failure fixture"),
            Map.entry("com.ai.atf.core.reporting.failurefixtures.TestBodyFailureFixture", "nested failure fixture"));
    private static final Pattern PACKAGE = Pattern.compile("\\bpackage\\s+([\\w$]+(?:\\s*\\.\\s*[\\w$]+)*)\\s*;");
    private static final Pattern IMPORT = Pattern.compile("\\bimport\\s+(?:static\\s+)?([\\w$]+(?:\\s*\\.\\s*(?:[\\w$]+|\\*))*)\\s*;");
    private static final Pattern QUALIFIED = Pattern.compile("[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*"
            + "(?:\\s*\\.\\s*[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*)+");
    private static final List<String> SDKS = List.of(
            "org.openqa.selenium", "com.microsoft.playwright", "com.aventstack.extentreports", "io.qameta.allure");
    private static final Set<String> COMPATIBILITY_SUPPORT = Set.of(
            "com.ai.ui.support.actions.UIActions", "com.ai.ui.support.actions.UiActionStep",
            "com.ai.ui.support.actions.UiActionWait", "com.ai.ui.support.actions.UiInteractionActions",
            "com.ai.ui.support.actions.UiNavigationActions", "com.ai.ui.support.actions.UiReadActions",
            "com.ai.ui.support.actions.UiNetworkActions", "com.ai.ui.support.wait.WaitUtils");
    static final Set<String> PAUSE_OWNERS = Set.of(
            "atf-core/src/main/java/com/ai/api/core/utils/ApiSleeper.java",
            "atf-core/src/main/java/com/ai/ui/core/wait/UiSleeper.java",
            "atf-core/src/main/java/com/ai/core/support/contract/wait/AtfTestWaiter.java");
    static final String CONSOLE_OWNER = "atf-core/src/main/java/com/ai/core/reporting/utils/ConsoleLogger.java";

    private FrameworkArchitectureRules() { }

    /** Structural JDK-call ownership and catch policy; behavioral wait tests provide independent evidence. */
    static List<String> validateExecution(Path root) throws Exception {
        List<String> violations = new ArrayList<>();
        Set<String> observedPauses = new HashSet<>();
        Set<String> observedConsoles = new HashSet<>();
        for (String module : GovernanceFiles.MODULES) {
            if (module.equals("atf-tests")) continue;
            for (Path file : GovernanceFiles.files(root.resolve(module).resolve("src/main/java"), ".java")) {
                String source = GovernanceFiles.relative(root, file);
                for (String event : JavaTypeReferences.executionPolicyEvents(Files.readString(file))) {
                    if (event.equals("PAUSE")) {
                        observedPauses.add(source);
                        if (!PAUSE_OWNERS.contains(source)) violations.add(source + ": EXECUTION-PAUSE use the owned API/UI/contract sleeper");
                    } else if (event.equals("CONSOLE")) {
                        observedConsoles.add(source);
                        if (!source.equals(CONSOLE_OWNER)) violations.add(source + ": EXECUTION-CONSOLE route raw console access through ConsoleLogger");
                    } else if (event.equals("INTERRUPT-RESTORE")) {
                        violations.add(source + ": EXECUTION-INTERRUPT restore the current thread flag directly in InterruptedException handling");
                    }
                }
            }
        }
        for (String owner : PAUSE_OWNERS) {
            if (!observedPauses.contains(owner)) violations.add(owner + ": EXECUTION-OWNER-STALE reviewed pause owner no longer contains a recognized pause");
        }
        if (!observedConsoles.contains(CONSOLE_OWNER)) violations.add(CONSOLE_OWNER + ": EXECUTION-OWNER-STALE reviewed console owner no longer accesses a raw channel");
        return violations.stream().distinct().collect(Collectors.toList());
    }

    /** Explicit TestNG gates must not silently omit regressions or start live application writes. */
    static List<String> validateSuites(Path root) throws Exception {
        List<String> violations = new ArrayList<>();
        Set<String> executable = executableTests(root, violations);
        Path suiteRoot = root.resolve("atf-tests/src/test/resources/suites");
        for (Path file : GovernanceFiles.files(suiteRoot, ".xml")) {
            String relative = GovernanceFiles.relative(suiteRoot, file);
            if (!MAINTAINED_SUITES.contains(relative)) violations.add(relative + ": SUITE-UNDECLARED update the maintained suite policy");
        }
        Map<String, Set<String>> selections = new LinkedHashMap<>();
        for (String name : MAINTAINED_SUITES) {
            Path file = suiteRoot.resolve(name);
            Set<String> selected = new LinkedHashSet<>();
            selections.put(name, selected);
            if (!Files.isRegularFile(file)) {
                violations.add(name + ": SUITE-MISSING maintained suite must exist");
                continue;
            }
            Element suite = suite(file);
            if (!"suite".equals(suite.getTagName())) violations.add(name + ": SUITE-XML expected TestNG suite root");
            for (String filter : List.of("packages", "methods", "groups", "method-selectors", "suite-files")) {
                if (suite.getElementsByTagName(filter).getLength() != 0) {
                    violations.add(name + ": SUITE-FILTER use explicit unfiltered class selections: " + filter);
                }
            }
            org.w3c.dom.NodeList tests = suite.getElementsByTagName("test");
            for (int i = 0; i < tests.getLength(); i++) {
                if ("false".equalsIgnoreCase(((Element) tests.item(i)).getAttribute("enabled"))) {
                    violations.add(name + ": SUITE-FILTER maintained test partition must not be disabled");
                }
            }
            org.w3c.dom.NodeList classes = suite.getElementsByTagName("class");
            for (int i = 0; i < classes.getLength(); i++) {
                String type = ((Element) classes.item(i)).getAttribute("name").strip();
                if (!selected.add(type)) violations.add(name + ": SUITE-DUPLICATE " + type);
                if (!executable.contains(type)) violations.add(name + ": SUITE-CLASS no concrete top-level TestNG test: " + type);
                if (SUITE_EXEMPTIONS.containsKey(type)) {
                    violations.add(name + ": SUITE-OPT-IN " + type + " is " + SUITE_EXEMPTIONS.get(type));
                }
            }
            if (selected.isEmpty()) violations.add(name + ": SUITE-EMPTY maintained gate must execute a class");
        }
        for (String type : SUITE_EXEMPTIONS.keySet()) {
            if (!executable.contains(type)) violations.add("SUITE-EXEMPTION-STALE remove or correct absent executable test: " + type);
        }
        Set<String> primary = new LinkedHashSet<>();
        for (String name : List.of("framework-local.xml", "framework-browser.xml", "framework-allure.xml")) primary.addAll(selections.get(name));
        for (String type : executable) {
            if (!SUITE_EXEMPTIONS.containsKey(type) && !primary.contains(type)) {
                violations.add("SUITE-OMISSION " + type + " must belong to local, browser or Allure acceptance");
            }
            if (type.startsWith("com.ai.atf.governance.") && !selections.get("framework-local.xml").contains(type)) {
                violations.add("SUITE-GOVERNANCE " + type + " must execute in the default local gate");
            }
        }
        if (!selections.get("framework-local.xml").equals(selections.get("framework-aggregate.xml"))) {
            violations.add("SUITE-AGGREGATE aggregate must retain exactly the default local class inventory");
        }
        return violations;
    }

    private static Element suite(Path file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder().parse(file.toFile()).getDocumentElement();
    }

    static Set<String> executableTests(Path root, List<String> violations) throws Exception {
        List<Path> sources = GovernanceFiles.files(root.resolve("atf-tests/src/test/java"), ".java");
        Set<String> executable = new LinkedHashSet<>();
        if (sources.isEmpty()) {
            violations.add("SUITE-SOURCE verification source inventory must not be empty");
            return executable;
        }
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Suite governance requires a JDK compiler");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null)) {
            JavacTask task = (JavacTask) compiler.getTask(null, files, diagnostics,
                    List.of("-proc:none", "--release", "11"), null, files.getJavaFileObjectsFromPaths(sources));
            for (CompilationUnitTree unit : task.parse()) {
                String namespace = unit.getPackageName() == null ? "" : unit.getPackageName().toString();
                boolean imported = unit.getImports().stream().anyMatch(value -> !value.isStatic()
                        && Set.of("org.testng.annotations.Test", "org.testng.annotations.*")
                        .contains(value.getQualifiedIdentifier().toString()));
                for (Tree declaration : unit.getTypeDecls()) {
                    if (declaration.getKind() != Tree.Kind.CLASS) continue;
                    ClassTree type = (ClassTree) declaration;
                    if (type.getModifiers().getFlags().contains(Modifier.ABSTRACT)) continue;
                    boolean visibleImport = imported && unit.getTypeDecls().stream().noneMatch(FrameworkArchitectureRules::declaresTestType)
                            && type.getMembers().stream().noneMatch(FrameworkArchitectureRules::declaresTestType);
                    boolean ownsTest = testAnnotation(type.getModifiers(), visibleImport);
                    for (Tree member : type.getMembers()) {
                        if (member instanceof MethodTree && testAnnotation(((MethodTree) member).getModifiers(), visibleImport)) ownsTest = true;
                    }
                    if (ownsTest) {
                        String name = namespace.isEmpty() ? type.getSimpleName().toString()
                                : namespace + '.' + type.getSimpleName();
                        executable.add(name);
                        if (!SUITE_EXEMPTIONS.containsKey(name)) {
                            String location = GovernanceFiles.relative(root, Path.of(unit.getSourceFile().toUri()));
                            validateTestEnabled(type.getModifiers(), visibleImport, location, name, violations);
                            for (Tree member : type.getMembers()) {
                                if (member instanceof MethodTree) {
                                    MethodTree method = (MethodTree) member;
                                    validateTestEnabled(method.getModifiers(), visibleImport, location,
                                            name + '#' + method.getName(), violations);
                                }
                            }
                        }
                    }
                }
            }
        }
        diagnostics.getDiagnostics().stream().filter(value -> value.getKind() == Diagnostic.Kind.ERROR)
                .forEach(value -> violations.add("SUITE-SOURCE " + value));
        return executable;
    }

    private static boolean testAnnotation(ModifiersTree modifiers, boolean imported) {
        return modifiers.getAnnotations().stream().anyMatch(value -> value.getAnnotationType().toString().equals("org.testng.annotations.Test")
                || imported && value.getAnnotationType().toString().equals("Test"));
    }

    private static boolean declaresTestType(Tree declaration) {
        return declaration instanceof ClassTree && ((ClassTree) declaration).getSimpleName().contentEquals("Test");
    }

    /** Maintained selection stays explicit; parse-only inspection does not resolve external constant values. */
    private static void validateTestEnabled(ModifiersTree modifiers, boolean imported, String location,
                                            String owner, List<String> violations) {
        for (com.sun.source.tree.AnnotationTree annotation : modifiers.getAnnotations()) {
            String name = annotation.getAnnotationType().toString();
            if (!name.equals("org.testng.annotations.Test") && !(imported && name.equals("Test"))) continue;
            for (ExpressionTree argument : annotation.getArguments()) {
                if (!(argument instanceof AssignmentTree)) continue;
                AssignmentTree assignment = (AssignmentTree) argument;
                if (!assignment.getVariable().toString().equals("enabled")) continue;
                ExpressionTree enabled = assignment.getExpression();
                while (enabled instanceof ParenthesizedTree) enabled = ((ParenthesizedTree) enabled).getExpression();
                if (!(enabled instanceof LiteralTree) || !Boolean.TRUE.equals(((LiteralTree) enabled).getValue())) {
                    violations.add(location + ": SUITE-DISABLED " + owner
                            + " must omit enabled or explicitly use true; observed enabled=" + assignment.getExpression());
                }
            }
        }
    }

    static List<String> validate(Path root) throws Exception {
        List<String> violations = new ArrayList<>();
        Element parent = GovernanceFiles.pom(root.resolve("pom.xml"));
        if (!"com.ai".equals(GovernanceFiles.text(parent, "groupId", ""))) {
            violations.add("pom.xml: PARENT-COORDINATE expected groupId com.ai");
        }
        List<String> declared = GovernanceFiles.children(GovernanceFiles.child(parent, "modules"), "module")
                .stream().map(value -> value.getTextContent().strip()).collect(Collectors.toList());
        if (declared.size() != 5 || !new HashSet<>(declared).equals(new HashSet<>(GovernanceFiles.MODULES))) {
            violations.add("pom.xml: MODULES expected exactly " + GovernanceFiles.MODULES + "; found " + declared);
        }
        if (!"pom".equals(GovernanceFiles.text(parent, "packaging", "jar"))) {
            violations.add("pom.xml: PARENT-PACKAGING must be pom");
        }
        for (Element context : configurations(parent)) {
            String location = "pom.xml" + profileLabel(parent, context) + ": ";
            if (!GovernanceFiles.children(GovernanceFiles.child(context, "dependencies"), "dependency").isEmpty()) {
                violations.add(location + "INHERITED-DEPENDENCIES put dependencies in their owner modules or dependencyManagement");
            }
            validateBuild(GovernanceFiles.child(context, "build"), location, violations);
        }
        if (!GovernanceFiles.files(root.resolve("src"), ".java").isEmpty()) {
            violations.add("src: ROOT-SOURCES keep all Java source in its owning module");
        }
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        for (String module : GovernanceFiles.MODULES) {
            Path pom = root.resolve(module).resolve("pom.xml");
            graph.put(module, new LinkedHashSet<>());
            if (!Files.isRegularFile(pom)) {
                violations.add(module + "/pom.xml: MISSING-MODULE");
                continue;
            }
            validatePom(module, GovernanceFiles.pom(pom), graph.get(module), violations);
        }
        for (String module : graph.keySet()) detectCycle(module, graph, new LinkedHashSet<>(), violations);
        Map<String, Source> sources = inventory(root, violations);
        for (Source source : sources.values()) validateSource(root, source, sources, violations);
        validateServices(root, sources, violations);
        return violations.stream().distinct().collect(Collectors.toList());
    }

    private static void validatePom(String module, Element pom, Set<String> productionEdges, List<String> violations) {
        String location = module + "/pom.xml: ";
        if (!module.equals(GovernanceFiles.text(pom, "artifactId", ""))) {
            violations.add(location + "ARTIFACT expected " + module);
        }
        Element parent = GovernanceFiles.child(pom, "parent");
        if (!"com.ai".equals(GovernanceFiles.text(pom, "groupId", GovernanceFiles.text(parent, "groupId", "")))) {
            violations.add(location + "MODULE-COORDINATE expected groupId com.ai");
        }
        Map<String, String> expected = new LinkedHashMap<>();
        if ("atf-tests".equals(module)) {
            for (String candidate : GovernanceFiles.MODULES) if (!candidate.equals(module)) expected.put(candidate, "test");
        } else if (!"atf-core".equals(module)) expected.put("atf-core", "compile");
        for (Element context : configurations(pom)) {
            String contextLocation = module + "/pom.xml" + profileLabel(pom, context) + ": ";
            validateBuild(GovernanceFiles.child(context, "build"), contextLocation, violations);
            Map<String, String> internal = new LinkedHashMap<>();
            for (Element dependency : GovernanceFiles.children(GovernanceFiles.child(context, "dependencies"), "dependency")) {
                String artifact = GovernanceFiles.text(dependency, "artifactId", "");
                String group = GovernanceFiles.text(dependency, "groupId", "");
                String scope = GovernanceFiles.text(dependency, "scope", "compile");
                if (artifact.contains("${") || group.contains("${")
                        && !group.equals("${project.groupId}") && !group.equals("${pom.groupId}")) {
                    violations.add(contextLocation + "DEPENDENCY-COORDINATE use explicit group/artifact identifiers for the static ownership inventory: " + group + ':' + artifact);
                }
                if (GovernanceFiles.MODULES.contains(artifact)) {
                    boolean ownGroup = group.equals("com.ai") || group.equals("${project.groupId}") || group.equals("${pom.groupId}");
                    if (!ownGroup) violations.add(contextLocation + "DEPENDENCY-COORDINATE expected com.ai:" + artifact + "; found " + group + ':' + artifact);
                    if (internal.put(artifact, scope) != null) violations.add(contextLocation + "DUPLICATE-DEPENDENCY " + artifact);
                    if (!"test".equals(scope)) productionEdges.add(artifact);
                    if (!scope.equals(expected.get(artifact))) {
                        violations.add(contextLocation + "DEPENDENCY-DIRECTION " + artifact + ':' + scope + " is outside " + expected);
                    }
                }
                if ("atf-core".equals(module) && (group.startsWith("org.seleniumhq.selenium")
                        || group.equals("com.microsoft.playwright") || group.equals("com.aventstack")
                        || group.startsWith("io.qameta.allure"))) {
                    violations.add(contextLocation + "CORE-SDK " + group + ':' + artifact + " belongs to its adapter");
                }
            }
            if (context == pom && !expected.equals(internal)) {
                violations.add(contextLocation + "DEPENDENCY-DIRECTION expected " + expected + "; found " + internal);
            }
        }
    }

    /** Every declared profile must preserve boundaries, even when inactive in this invocation. */
    private static List<Element> configurations(Element pom) {
        List<Element> contexts = new ArrayList<>();
        contexts.add(pom);
        contexts.addAll(GovernanceFiles.children(GovernanceFiles.child(pom, "profiles"), "profile"));
        return contexts;
    }

    private static String profileLabel(Element pom, Element context) {
        return pom == context ? "" : " [profile " + GovernanceFiles.text(context, "id", "unnamed") + ']';
    }

    private static void validateBuild(Element build, String location, List<String> violations) {
        if (!"src/main/java".equals(GovernanceFiles.text(build, "sourceDirectory", "src/main/java"))
                || !"src/test/java".equals(GovernanceFiles.text(build, "testSourceDirectory", "src/test/java"))) {
            violations.add(location + "SOURCE-ROOTS keep standard module-local Java source roots");
        }
        for (Element resource : GovernanceFiles.children(GovernanceFiles.child(build, "resources"), "resource")) {
            String directory = GovernanceFiles.text(resource, "directory", "").replace('\\', '/');
            if (directory.contains("src/test") || directory.contains("..")) {
                violations.add(location + "TEST-RESOURCE-LEAK production resources include " + directory);
            }
        }
        List<Element> plugins = new ArrayList<>(GovernanceFiles.children(GovernanceFiles.child(build, "plugins"), "plugin"));
        plugins.addAll(GovernanceFiles.children(GovernanceFiles.child(GovernanceFiles.child(build, "pluginManagement"), "plugins"), "plugin"));
        for (Element plugin : plugins) {
            if (!"build-helper-maven-plugin".equals(GovernanceFiles.text(plugin, "artifactId", ""))) continue;
            for (Element execution : GovernanceFiles.children(GovernanceFiles.child(plugin, "executions"), "execution")) {
                for (Element goal : GovernanceFiles.children(GovernanceFiles.child(execution, "goals"), "goal")) {
                    if (Set.of("add-source", "add-test-source", "add-resource", "add-test-resource").contains(goal.getTextContent().strip())) {
                        violations.add(location + "SOURCE-ROOTS build-helper " + goal.getTextContent().strip()
                                + " would bypass the standard source/resource ownership inventory");
                    }
                }
            }
        }
    }

    private static void detectCycle(String node, Map<String, Set<String>> graph,
                                    LinkedHashSet<String> visiting, List<String> violations) {
        if (!visiting.add(node)) {
            violations.add("pom.xml: DEPENDENCY-CYCLE " + visiting + " -> " + node);
            return;
        }
        for (String next : graph.getOrDefault(node, Set.of())) detectCycle(next, graph, visiting, violations);
        visiting.remove(node);
    }

    private static Map<String, Source> inventory(Path root, List<String> violations) throws Exception {
        Map<String, Source> sources = new LinkedHashMap<>();
        for (String module : GovernanceFiles.MODULES) {
            Path sourceRoot = root.resolve(module).resolve("src/main/java");
            List<Path> files = GovernanceFiles.files(sourceRoot, ".java");
            if (module.equals("atf-tests") && !files.isEmpty()) violations.add("atf-tests: TESTS-MAIN must have no production Java");
            for (Path file : files) {
                if (file.getFileName().toString().equals("package-info.java") || file.getFileName().toString().equals("module-info.java")) continue;
                String code = JavaSourceScanner.codeOnly(Files.readString(file));
                Matcher packageName = PACKAGE.matcher(code);
                if (!packageName.find()) {
                    violations.add(GovernanceFiles.relative(root, file) + ": PACKAGE missing named package");
                    continue;
                }
                String namespace = packageName.group(1).replaceAll("\\s", "");
                String simpleName = file.getFileName().toString().replaceFirst("\\.java$", "");
                String name = namespace + '.' + simpleName;
                Path expected = Path.of(namespace.replace('.', '/')).resolve(file.getFileName());
                if (!sourceRoot.relativize(file).equals(expected)) {
                    violations.add(GovernanceFiles.relative(root, file) + ": PACKAGE-PATH expected " + expected);
                }
                Source previous = sources.putIfAbsent(name, new Source(module, file, namespace, code));
                if (previous != null) violations.add(GovernanceFiles.relative(root, file) + ": DUPLICATE-TYPE " + name + " also in " + previous.module);
                boolean productSupport = namespace.startsWith("com.ai.ui.support") || namespace.startsWith("com.ai.api.support");
                if ((productSupport && !(module.equals("atf-core") && COMPATIBILITY_SUPPORT.contains(name)))
                        || namespace.equals("com.ai.atf") || namespace.startsWith("com.ai.atf.")
                        || namespace.startsWith("com.ai.ui.tests") || namespace.startsWith("com.ai.api.tests")) {
                    violations.add(GovernanceFiles.relative(root, file) + ": TEST-PRODUCT-LEAK move test/product support to atf-tests/src/test/java");
                }
            }
        }
        return sources;
    }

    private static void validateSource(Path root, Source source, Map<String, Source> sources, List<String> violations) {
        String location = GovernanceFiles.relative(root, source.file) + ": ";
        for (String engine : List.of("playwright", "selenium")) {
            String implementation = "com.ai.ui.core.engine." + engine;
            String module = "atf-" + engine;
            if (source.namespace.equals(implementation) || source.namespace.startsWith(implementation + '.')) {
                if (!source.module.equals(module)) violations.add(location + "PACKAGE-OWNER " + implementation + " belongs to " + module);
            } else if (source.module.equals(module)) {
                violations.add(location + "PACKAGE-OWNER " + module + " owns only " + implementation + " implementations");
            }
        }
        if (source.module.equals("atf-reporting") && !source.namespace.equals("com.ai.core.reporting")
                && !source.namespace.startsWith("com.ai.core.reporting.")) {
            violations.add(location + "PACKAGE-OWNER reporting implementation belongs under com.ai.core.reporting");
        }
        for (String sdk : SDKS) {
            boolean allowed = source.module.equals("atf-playwright") && sdk.equals("com.microsoft.playwright")
                    || source.module.equals("atf-selenium") && sdk.equals("org.openqa.selenium")
                    || source.module.equals("atf-reporting") && (sdk.startsWith("com.aventstack") || sdk.startsWith("io.qameta"));
            if (!allowed && JavaSourceScanner.containsCodeNamespace(source.code, sdk)) {
                violations.add(location + "SDK-OWNERSHIP " + sdk + " is outside " + source.module);
            }
        }
        for (String retired : List.of("com.ai.mcp", "com.ai.agent", "com.ai.agents", "com.ai.generation")) {
            if (JavaSourceScanner.containsCodeNamespace(source.code, retired)) violations.add(location + "RETIRED-GENERATION " + retired);
        }
        Matcher imports = IMPORT.matcher(source.code);
        Set<String> declarations = new HashSet<>();
        while (imports.find()) {
            if (!declarations.add(imports.group().replaceAll("\\s", ""))) {
                violations.add(location + "DUPLICATE-IMPORT " + imports.group().strip());
            }
            String name = imports.group(1).replaceAll("\\s", "");
            if (name.endsWith(".*")) {
                violations.add(location + "IMPORT-WILDCARD name each imported type or static member explicitly");
                String prefix = name.substring(0, name.length() - 2);
                for (Source owner : sources.values()) {
                    if (owner.namespace.equals(prefix) && !mayReference(source.module, owner.module)) {
                        violations.add(location + "IMPORT-OWNERSHIP " + name + " includes " + owner.module);
                        break;
                    }
                }
            }
        }
        Matcher references = QUALIFIED.matcher(source.code);
        while (references.find()) {
            String name = references.group().replaceAll("\\s", "");
            Source owner = sources.get(name);
            while (owner == null && name.contains(".")) {
                name = name.substring(0, name.lastIndexOf('.'));
                owner = sources.get(name);
            }
            if (owner != null && !mayReference(source.module, owner.module)) {
                violations.add(location + "IMPORT-OWNERSHIP " + name + " belongs to " + owner.module);
            }
        }
        List<Map.Entry<String, Source>> samePackage = sources.entrySet().stream()
                .filter(entry -> entry.getValue().namespace.equals(source.namespace))
                .filter(entry -> !mayReference(source.module, entry.getValue().module)).collect(Collectors.toList());
        if (!samePackage.isEmpty()) {
            try {
                Set<String> simpleTypes = JavaTypeReferences.simpleTypes(Files.readString(source.file));
                for (Map.Entry<String, Source> entry : samePackage) {
                    String name = entry.getKey();
                    String simple = name.substring(name.lastIndexOf('.') + 1);
                    if (simpleTypes.contains(simple) && !hasDifferentExplicitImport(source.code, simple, name)) {
                        violations.add(location + "SIMPLE-TYPE-OWNERSHIP " + simple + " in package " + source.namespace
                                + " belongs to " + entry.getValue().module);
                    }
                }
            } catch (java.io.IOException failure) {
                throw new IllegalStateException("Cannot read source for type ownership: " + source.file, failure);
            }
        }
    }

    private static boolean hasDifferentExplicitImport(String code, String simpleName, String owner) {
        Matcher imports = Pattern.compile("\\bimport\\s+(?!static\\s)([\\w$]+(?:\\s*\\.\\s*[\\w$]+)+)\\s*;").matcher(code);
        while (imports.find()) {
            String imported = imports.group(1).replaceAll("\\s", "");
            if (imported.endsWith('.' + simpleName) && !imported.equals(owner)) return true;
        }
        return false;
    }

    private static boolean mayReference(String module, String owner) {
        return module.equals(owner) || !module.equals("atf-core") && owner.equals("atf-core");
    }

    private static void validateServices(Path root, Map<String, Source> sources, List<String> violations) throws Exception {
        Map<String, String> services = Map.of("atf-playwright", UI_SPI, "atf-selenium", UI_SPI, "atf-reporting", REPORTING_SPI);
        for (Map.Entry<String, String> entry : services.entrySet()) {
            Path descriptor = root.resolve(entry.getKey()).resolve("src/main/resources/META-INF/services").resolve(entry.getValue());
            if (!Files.isRegularFile(descriptor)) {
                violations.add(GovernanceFiles.relative(root, descriptor) + ": SERVICE missing provider descriptor");
                continue;
            }
            List<String> providers = Files.readAllLines(descriptor).stream().map(line -> line.split("#", 2)[0].strip())
                    .filter(line -> !line.isEmpty()).collect(Collectors.toList());
            if (providers.size() != 1) violations.add(GovernanceFiles.relative(root, descriptor) + ": SERVICE expected exactly one provider, found " + providers);
            for (String provider : providers) {
                Source owner = sources.get(provider);
                if (owner == null || !owner.module.equals(entry.getKey())) {
                    violations.add(GovernanceFiles.relative(root, descriptor) + ": SERVICE-OWNER provider " + provider + " must be defined by " + entry.getKey());
                }
            }
        }
        for (String spi : List.of(UI_SPI, REPORTING_SPI)) {
            if (!sources.containsKey(spi) || !sources.get(spi).module.equals("atf-core")) {
                violations.add("atf-core: SPI-OWNER " + spi + " must be a Core contract");
            }
            Path descriptor = root.resolve("atf-core/src/main/resources/META-INF/services").resolve(spi);
            if (Files.exists(descriptor)) violations.add("atf-core: SERVICE Core must not register an adapter provider: " + spi);
        }
    }

    private static final class Source {
        final String module;
        final Path file;
        final String namespace;
        final String code;

        Source(String module, Path file, String namespace, String code) {
            this.module = module;
            this.file = file;
            this.namespace = namespace;
            this.code = code;
        }
    }
}
